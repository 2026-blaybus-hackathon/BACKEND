package com.blaybus.backend.repository.auth

import com.blaybus.backend.entity.auth.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisRefreshTokenRepository : CrudRepository<RefreshToken, Int> {
    fun findByUserId(userId: Int): RefreshToken?
}
