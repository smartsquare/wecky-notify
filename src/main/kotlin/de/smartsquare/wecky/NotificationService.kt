package de.smartsquare.wecky

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.*
import de.smartsquare.wecky.domain.User
import de.smartsquare.wecky.domain.Website
import org.slf4j.LoggerFactory

class NotificationService(val amazonSes: AmazonSimpleEmailService) {

    companion object Factory {
        val log = LoggerFactory.getLogger(NotificationService::class.java.simpleName)
    }

    fun notifyUser(user: User, website: Website, linkToScreenshot: String) {
        val websiteChangedUpdateMail = SendEmailRequest()
                .withDestination(Destination().withToAddresses(user.email))
                .withMessage(Message()
                        .withBody(Body()
                                .withHtml(Content()
                                        .withCharset("UTF-8")
                                        .withData("""
                                            <h2>Hello ${user.name}!<h2>
                                            <p>I was supposed to watch ${website.url} for changes. I am very excited to tell you, that it has changed indeed!</p>
                                            <p>I even captured a screenshot. You can find it attached to this email. :-)</p>
                                            <p>Yours,<br/>
                                            Wecky</p>
                                            <img src="$linkToScreenshot"/>
                                        """.trimIndent())
                                ))
                        .withSubject(Content()
                                .withCharset("UTF-8")
                                .withData("${website.url} has changed!")))
                .withSource("smartsquaregmbh@gmail.com")
        log.info("Sending email to [${user.email}]")
        amazonSes.sendEmail(websiteChangedUpdateMail)
    }
}