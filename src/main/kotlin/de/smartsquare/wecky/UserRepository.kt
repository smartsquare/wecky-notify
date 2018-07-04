package de.smartsquare.wecky

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.User
import org.slf4j.LoggerFactory


class UserRepository(val dynamoDB: AmazonDynamoDB) {

    companion object Factory {
        val log = LoggerFactory.getLogger(UserRepository::class.java.simpleName)
        val tableName = "User"
    }

    fun findUserIdBy(hashedWebsite: HashedWebsite): String? {
        val websiteId = hashedWebsite.websiteId
        log.info("Fetching website for id [$websiteId]")
        val getItemRequest = GetItemRequest()
                .withKey(mapOf("id" to AttributeValue(websiteId)))
                .withTableName("Website")
        val website = dynamoDB.getItem(getItemRequest).item
        if (website == null) {
            log.info("No website found with id [$websiteId]")
        }
        return website["userId"]?.s
    }


    fun findUserBy(userId: String): User? {
        val attrValues = mapOf(":user_id" to AttributeValue(userId))
        val scanReq = ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("userId = :user_id")
                .withExpressionAttributeValues(attrValues)

        val result = dynamoDB.scan(scanReq)

        val userRecord = result.items.firstOrNull()
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