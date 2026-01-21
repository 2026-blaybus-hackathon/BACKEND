package com.blaybus.backend.repository.auth

import com.blaybus.backend.entity.auth.BlackListAccessToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BlackListAccessTokenRepository : CrudRepository<BlackListAccessToken, Int> {
    fun existsByToken(token: String): Boolean
}
