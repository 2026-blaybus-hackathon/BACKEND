package com.blaybus.backend.entity.auth

import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash(value = "emailVerification")
class EmailVerificationToken(
    @field:Indexed
    private val email: String,
    val verificationCode: String,
    @field:TimeToLive
    private val expirationTime: Long,
) {
    @Id
    var id: Int? = null
}
