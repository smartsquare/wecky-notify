package de.smartsquare.wecky

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.smartsquare.wecky.domain.WebsiteChange
import java.io.InputStream
import java.io.OutputStream

class NotificationHandler : RequestStreamHandler {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    override fun handleRequest(websiteJson: InputStream?, output: OutputStream?, context: Context?) {
        websiteJson?.also { inputStream ->
            val websiteChange: WebsiteChange = mapper.readValue(inputStream)

            val ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(Regions.EU_WEST_1)
                    .build()

            NotificationService(ses).notifyUser(websiteChange)
        }

    }

}