package de.smartsquare.wecky

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.*
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.User
import org.slf4j.LoggerFactory

class NotificationService(val amazonSes: AmazonSimpleEmailService) {

    companion object Factory {
        val log = LoggerFactory.getLogger(NotificationService::class.java.simpleName)
    }

    fun notifyUser(user: User, hashedWebsite: HashedWebsite) {
        val websiteChangedUpdateMail = SendEmailRequest()
                .withDestination(Destination().withToAddresses(user.email))
                .withMessage(Message()
                        .withBody(Body()
                                .withText(Content()
                                        .withCharset("UTF-8")
                                        .withData(hashedWebsite.content)))
                        .withSubject(Content()
                                .withCharset("UTF-8")
                                .withData("${hashedWebsite.url} has changed!")))
                .withSource("smartsquaregmbh@gmail.com")
        log.info("Sending email to [${user.email}]")
        amazonSes.sendEmail(websiteChangedUpdateMail)
    }
}