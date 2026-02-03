package com.blaybus.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val secretKey: String,
    val accessTokenExpirationTime: Long,
    val refreshTokenExpirationTime: Long,
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

@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
data class ObjectStorageProperties(
    val bucket: String,
    val cdnUrl: String,
)
