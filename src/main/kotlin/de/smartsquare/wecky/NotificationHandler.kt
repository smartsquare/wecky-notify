package de.smartsquare.wecky

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.smartsquare.wecky.domain.HashedWebsite
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream

class NotificationHandler : RequestStreamHandler {

    companion object Factory {
        val log = LoggerFactory.getLogger(NotificationHandler::class.java.simpleName)
        val mapper = jacksonObjectMapper()
    }

    override fun handleRequest(websiteJson: InputStream?, output: OutputStream?, context: Context?) {
        websiteJson?.also { inputStream ->
            val dyndbLocal = System.getenv("DYNDB_LOCAL")
            val amazonDynamoDB =
                    if (dyndbLocal?.isNotEmpty() ?: false) {
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

            val hashedWebsite: HashedWebsite = mapper.readValue(inputStream)
            val userRepo = UserRepository(amazonDynamoDB)
            val user = userRepo.findUserBy(hashedWebsite.id)

            val ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(Regions.EU_WEST_1)
                    .build()

            if (user != null) {
                NotificationService(ses).notifyUser(user, hashedWebsite)
            } else {
                log.info("No user found for website [${hashedWebsite.id}]")
            }
        }

    }

}