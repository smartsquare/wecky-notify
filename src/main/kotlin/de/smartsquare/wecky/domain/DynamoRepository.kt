package de.smartsquare.wecky.domain

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import org.slf4j.LoggerFactory


class DynamoRepository(val dynamoDB: AmazonDynamoDB) {

    companion object Factory {
        val log = LoggerFactory.getLogger(DynamoRepository::class.java.simpleName)
    }

    fun findWebsiteById(websiteId: String): Website? {
        log.info("Fetching website for id [$websiteId]")
        val getItemRequest = GetItemRequest()
                .withKey(mapOf("id" to AttributeValue(websiteId)))
                .withTableName("Website")
        val record = dynamoDB.getItem(getItemRequest).item
        if (record == null) {
            log.info("No website found with id [$websiteId]")
            return null
        }
        return Website(
                record["id"]!!.s,
                record["url"]!!.s,
                record["userId"]!!.s)
    }


    fun findUserBy(userId: String): User? {
        val getItemRequest = GetItemRequest()
                .withKey(mapOf("id" to AttributeValue(userId)))
                .withTableName("User")
        val userRecord = dynamoDB.getItem(getItemRequest).item
        if (userRecord == null) {
            log.info("No user found with id [$userId]")
            return null
        }
        val user = User(userRecord.get("id")!!.s,
                userRecord.get("name")!!.s,
                userRecord.get("email")!!.s)
        log.info("User found with id [${user.id}]")
        return user
    }

}