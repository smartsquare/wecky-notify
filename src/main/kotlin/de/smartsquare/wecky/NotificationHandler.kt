package de.smartsquare.wecky

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.smartsquare.wecky.domain.Website
import java.io.InputStream
import java.io.OutputStream

class NotificationHandler : RequestStreamHandler {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    override fun handleRequest(websiteJson: InputStream?, output: OutputStream?, context: Context?) {
        websiteJson?.also { inputStream ->
            val website: Website = mapper.readValue(inputStream)

            val ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(Regions.EU_WEST_1)
                    .build()


            val websiteChangedUpdateMail = SendEmailRequest()
                    .withDestination(Destination().withToAddresses(website.userEmail))
                    .withMessage(Message()
                            .withBody(Body()
                                    .withText(Content()
                                            .withCharset("UTF-8")
                                            .withData("${website.url} has changed!")))
                            .withSubject(Content()
                                    .withCharset("UTF-8")
                                    .withData("Wecky Notify")))
                    .withSource("d.dierkes@outlook.de")
            ses.sendEmail(websiteChangedUpdateMail)
        }

    }

}