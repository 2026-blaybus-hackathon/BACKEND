package com.blaybus.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val secretKey: String,
    val accessTokenExpirationTime: Long,
    val refreshTokenExpirationTime: Long,
    val emailVerificationTokenExpirationTime: Long,
    val oauth2AccessTokenExpirationTime: Long,
)

@ConfigurationProperties(prefix = "spring.mail")
class EmailProperties(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val auth: Boolean,
    val starttls: Boolean,
    val debug: Boolean,
    val connectiontimeout: Int,
)

@ConfigurationProperties(prefix = "oauth2.google")
class OAuth2Properties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val tokenUri: String,
)
