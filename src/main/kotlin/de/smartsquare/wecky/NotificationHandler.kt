package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import de.smartsquare.wecky.domain.DynamoRepository
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream

class NotificationHandler : RequestStreamHandler {

    companion object Factory {
        val log = LoggerFactory.getLogger(NotificationHandler::class.java.simpleName)
        val mapper = JacksonAwsMapperBuilder().build()
    }

    override fun handleRequest(inputStream: InputStream?, output: OutputStream?, context: Context?) {
        val s3Event = mapper.readValue(inputStream!!, S3EventNotification::class.java)

        for (record in s3Event.records) {
            if (record.eventName != "ObjectCreated:Put") {
                log.info("Record event not of type INSERT, aborting...")
                return
            }

            val ses = determineSes()
            val dynamoRepo = DynamoRepository(determineDynamoDB())

            val bucket = record.s3.bucket.name
            val key = record.s3.getObject().key
            val region = record.awsRegion
            val linkToScreenshot = "https://s3.$region.amazonaws.com/$bucket/$key"

            val websiteId = key.split("-")[0]
            val website = dynamoRepo.findWebsiteById(websiteId)

            website
                    ?.let { dynamoRepo.findUserBy(it.userId) }
                    ?.let { NotificationService(ses).notifyUser(it, website, linkToScreenshot) }
        }
    }

    private fun getEnv(name: String) = System.getenv(name) ?: System.getProperty(name)

    private fun endpointConfiguration(local: String?) = AwsClientBuilder.EndpointConfiguration(local, "eu-west-1")

    private fun determineSes(): AmazonSimpleEmailService {
        val sesLocal = getEnv("SES_LOCAL")

        return if (sesLocal?.isNotEmpty() == true) {
            log.info("Triggered local dev mode using local SES at [$sesLocal]")
            AmazonSimpleEmailServiceClientBuilder.standard()
                    .withEndpointConfiguration(endpointConfiguration(sesLocal))
                    .build()
        } else {
            log.info("Using production SES at eu-west-1")
            AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_1)
                    .build()
        }
    }

    private fun determineDynamoDB(): AmazonDynamoDB {
        val dyndbLocal = getEnv("DYNDB_LOCAL")
        return if (dyndbLocal?.isNotEmpty() == true) {
            log.info("Triggered local dev mode using local DynamoDB at [$dyndbLocal]")
            System.setProperty("aws.accessKeyId", "key")
            System.setProperty("aws.secretKey", "key2")
            AmazonDynamoDBClient.builder()
                    .withEndpointConfiguration(endpointConfiguration(dyndbLocal))
                    .build()
        } else {
            log.info("Using production DynamoDB at eu-central-1")
            AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build()
        }
    }

}