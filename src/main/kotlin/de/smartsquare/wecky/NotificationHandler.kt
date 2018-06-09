package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

class NotificationHandler : RequestHandler<Any, String> {
    override fun handleRequest(p0: Any?, p1: Context?): String {
        return "Hello World"
    }

}