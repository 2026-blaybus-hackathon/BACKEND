package com.blaybus.backend.repository.auth

import com.blaybus.backend.entity.auth.EmailVerificationToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisEmailVerificationTokenRepository : CrudRepository<EmailVerificationToken, Int> {
    fun findByEmail(email: String): EmailVerificationToken?
}
