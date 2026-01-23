package com.blaybus.backend.entity.auth

import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash(value = "refreshToken")
class RefreshToken(
    @Id
    var id: Int? = null,
    @field:Indexed
    private var userId: Long,
    var token: String,
    @field:TimeToLive
    private var expirationTime: Long,
) {
    constructor(userId: Long, token: String, expirationTime: Long) : this(
        id = null,
        userId = userId,
        token = token,
        expirationTime = expirationTime,
    )
}
