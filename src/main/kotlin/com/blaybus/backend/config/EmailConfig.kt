package com.blaybus.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class EmailConfig(
    private val emailProperties: EmailProperties,
) {
    @Bean
    fun getJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = emailProperties.host
        mailSender.port = emailProperties.port

        mailSender.username = emailProperties.username
        mailSender.password = emailProperties.password

        val props: Properties = mailSender.javaMailProperties
        props["mail.smtp.auth"] = emailProperties.auth
        props["mail.smtp.starttls.enable"] = emailProperties.starttls
        props["mail.debug"] = emailProperties.debug
        props["mail.smtp.connectiontimeout"] = emailProperties.connectiontimeout

        return mailSender
    }
}
