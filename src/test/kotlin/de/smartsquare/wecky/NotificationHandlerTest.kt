package de.smartsquare.wecky

import cloud.localstack.docker.LocalstackDocker
import cloud.localstack.docker.LocalstackDockerExtension
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest
import de.smartsquare.wecky.domain.DynamoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import java.io.ByteArrayOutputStream
import java.util.*

@ExtendWith(LocalstackDockerExtension::class)
@DisabledIfSystemProperty(named = "ci-server", matches = "true")
class NotificationHandlerTest {

    lateinit var handler: NotificationHandler
    lateinit var repo: DynamoRepository
    lateinit var ses: AmazonSimpleEmailService

    @BeforeEach
    fun setUp() {
        handler = NotificationHandler()
        val dyndbLocal = LocalstackDocker.INSTANCE.endpointDynamoDB
        System.setProperty("DYNDB_LOCAL", dyndbLocal)
        System.setProperty("aws.accessKeyId", "key")
        System.setProperty("aws.secretKey", "key2")
        val dyndbClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "eu-central-1"))
                .build()
        repo = DynamoRepository(dyndbClient)

        createTable(dyndbClient, "User")
        createTable(dyndbClient, "Website")
        val userId = UUID.randomUUID().toString()
        val user = mapOf(
                "id" to AttributeValue(userId),
                "name" to AttributeValue("Foo Bar"),
                "email" to AttributeValue("foo@bar.de"))
        dyndbClient.putItem("User", user)
        val website = mapOf(
                "id" to AttributeValue("TIME"),
                "url" to AttributeValue("https://time.is"),
                "userId" to AttributeValue(userId))
        dyndbClient.putItem("Website", website)

        val endpointSES = LocalstackDocker.INSTANCE.endpointSES
        System.setProperty("SES_LOCAL", endpointSES)
        ses = AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpointSES, "eu-west-1"))
                .build()
        ses.verifyEmailIdentity(VerifyEmailIdentityRequest().withEmailAddress("smartsquaregmbh@gmail.com"))
    }

    private fun createTable(dyndbClient: AmazonDynamoDB, tableName: String) {
        try {
            val request = CreateTableRequest()
                    .withAttributeDefinitions(AttributeDefinition("id", ScalarAttributeType.S))
                    .withKeySchema(KeySchemaElement("id", KeyType.HASH))
                    .withProvisionedThroughput(ProvisionedThroughput(
                            10, 10))
                    .withTableName(tableName)
            dyndbClient.createTable(request)
        } catch (ex: ResourceInUseException) {
            // ignore existing table
        }
    }

    @Test
    fun no_previous_hashes() {
        val inputStream = javaClass.classLoader.getResourceAsStream("event.json")

        handler.handleRequest(inputStream, ByteArrayOutputStream(), null)

        val sendQuota = ses.sendQuota
        assertEquals(1.0, sendQuota.sentLast24Hours)
    }
}