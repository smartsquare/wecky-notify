package de.smartsquare.wecky

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.*
import de.smartsquare.wecky.domain.WebsiteChange

class NotificationService(val amazonSes: AmazonSimpleEmailService) {

    fun notifyUser(websiteChange: WebsiteChange) {
        val websiteChangedUpdateMail = SendEmailRequest()
                .withDestination(Destination().withToAddresses(websiteChange.userEmail))
                .withMessage(Message()
                        .withBody(Body()
                                .withText(Content()
                                        .withCharset("UTF-8")
                                        .withData("${websiteChange.url} has changed!")))
                        .withSubject(Content()
                                .withCharset("UTF-8")
                                .withData("Wecky Notify")))
                .withSource("d.dierkes@outlook.de")
        amazonSes.sendEmail(websiteChangedUpdateMail)
    }
}