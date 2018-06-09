package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.smartsquare.wecky.domain.Website
import java.io.InputStream
import java.io.OutputStream

class NotificationHandler : RequestStreamHandler {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    override fun handleRequest(websiteJson: InputStream?, output: OutputStream?, context: Context?) {
        websiteJson?.let {
            val website = mapper.readValue<Website>(it)
            print(website)
        }
    }

}