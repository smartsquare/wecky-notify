package de.smartsquare.wecky

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.OperationType
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.UserRepository
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

class NotificationHandler : RequestStreamHandler {

    companion object Factory {
        val log = LoggerFactory.getLogger(NotificationHandler::class.java.simpleName)
        val mapper = JacksonAwsMapperBuilder().build()
    }

    override fun handleRequest(inputStream: InputStream?, output: OutputStream?, context: Context?) {
        val dyndbEvent = mapper.readValue(inputStream!!, DynamodbEvent::class.java)

        for (record in dyndbEvent.records) {
            if (record.eventName != OperationType.INSERT.toString()) {
                log.info("Record event not of type INSERT, aborting...")
                return
            }

            val item = record.dynamodb.newImage
            val hashedWebsite = HashedWebsite(
                    item.get("websiteId")!!.s,
                    item.get("url")!!.s,
                    item.get("content")!!.s,
                    item.get("hashValue")!!.s.toInt(),
                    Instant.ofEpochMilli(item.get("crawlDate")!!.s.toLong()))

            val ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(Regions.EU_WEST_1)
                    .build()
            val userRepo = UserRepository(determineDynamoDB())

            userRepo.findUserIdBy(hashedWebsite)
                    ?.let { userRepo.findUserBy(it) }
                    ?.let { NotificationService(ses).notifyUser(it, hashedWebsite) }
        }
    }

    private fun determineDynamoDB(): AmazonDynamoDB {
        val dyndbLocal = System.getenv("DYNDB_LOCAL")
        return if (dyndbLocal?.isNotEmpty() == true) {
            log.info("Triggered local dev mode using local DynamoDB at [$dyndbLocal]")
            System.setProperty("aws.accessKeyId", "key")
            System.setProperty("aws.secretKey", "key2")
            AmazonDynamoDBClient.builder()
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "eu-central-1"))
                    .build()
        } else {
            log.info("Using production DynamoDB at eu-central-1")
            AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build()
        }
    }

}