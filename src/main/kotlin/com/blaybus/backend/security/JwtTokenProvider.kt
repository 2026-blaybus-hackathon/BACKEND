package com.blaybus.backend.security

import com.blaybus.backend.config.JwtProperties
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.auth.RefreshToken
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.auth.RedisRefreshTokenRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val refreshTokenRepository: RedisRefreshTokenRepository,
    jwtProperties: JwtProperties,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    private val refreshTokenExpirationTime = jwtProperties.refreshTokenExpirationTime
    private val accessTokenExpirationTime = jwtProperties.accessTokenExpirationTime
    val emailVerificationTokenExpirationTime = jwtProperties.emailVerificationTokenExpirationTime
    val oauth2AccessTokenExpirationTime = jwtProperties.oauth2AccessTokenExpirationTime

    fun createToken(
        vararg claims: Pair<String, Any>,
        expirationMinutes: Long,
    ): String {
        val claimsMap = mapOf(*claims)

        return Jwts
            .builder()
            .apply {
                claimsMap.forEach { (key, value) -> claim(key, value) }
            }.signWith(key)
            .expiration(Date.from(ZonedDateTime.now().plusSeconds(expirationMinutes).toInstant()))
            .compact()
    }

    fun getAuthentication(token: String): List<GrantedAuthority> =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .get("authorities", List::class.java)
            .map { SimpleGrantedAuthority(it as String) }
            .toList()

    fun <T> getClaim(
        token: String,
        claimName: String,
        type: Class<T>,
    ): T {
        try {
            val payload =
                Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            val raw = payload[claimName] ?: throw Exception("해당 클레임이 존재하지 않습니다: $claimName")

            if (type == Long::class.java) {
                return when (raw) {
                    is Number -> raw.toLong() as T
                    else -> throw Exception("unsupported claim type for Long conversion: ${raw::class}")
                }
            }

            if (type == Int::class.java) {
                return when (raw) {
                    is Number -> raw.toInt() as T
                    else -> throw Exception("unsupported claim type for Int conversion: ${raw::class}")
                }
            }

            if (type == String::class.java) {
                return raw.toString() as T
            }

            if (type.isAssignableFrom(raw::class.java)) {
                return raw as T
            }

            throw Exception("unsupported claim type: requested=$type, actual=${raw::class}")
        } catch (e: ExpiredJwtException) {
            handleExpiredJwtException(e)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_TOKEN, e.message)
        }
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
            return !claims.payload.expiration.before(Date()) // 토큰의 만료시간이 현재 시간 이전인지 확인
        } catch (e: ExpiredJwtException) {
            handleExpiredJwtException(e)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_TOKEN, e.message)
        }
    }

    fun getTokenResponse(
        userId: Long,
        authorities: Collection<GrantedAuthority>,
        nickname: String,
    ): TokenResponse {
        val accessToken =
            createToken(
                "userId" to userId,
                "authorities" to authorities.map { it.authority },
                expirationMinutes = accessTokenExpirationTime,
            )
        val refreshToken =
            createToken(
                "userId" to userId,
                expirationMinutes = refreshTokenExpirationTime,
            )
        val existingRefreshToken: RefreshToken? = refreshTokenRepository.findByUserId(userId)
        if (existingRefreshToken != null) {
            refreshTokenRepository.delete(existingRefreshToken)
        }
        refreshTokenRepository.save(RefreshToken(userId, refreshToken, refreshTokenExpirationTime))
        return TokenResponse(accessToken, refreshToken, nickname)
    }

    fun generateOAuth2Token(
        oAuth2IdToken: String,
        provider: Provider,
    ): String =
        createToken(
            "oAuth2IdToken" to oAuth2IdToken,
            "provider" to provider,
            expirationMinutes = oauth2AccessTokenExpirationTime,
        )

    fun getTokenExpirationTime(token: String): Date =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .expiration

    fun getAuthorities(role: Role): MutableList<GrantedAuthority> = mutableListOf(SimpleGrantedAuthority(role.name))

    fun generateEmailVerificationToken(email: String): String =
        createToken(
            "email" to email,
            "tokenType" to TokenType.EMAIL_VERIFICATION,
            expirationMinutes = emailVerificationTokenExpirationTime,
        )

    private fun handleExpiredJwtException(e: ExpiredJwtException): Nothing {
        val tokenType: TokenType =
            try {
                TokenType.valueOf(e.claims.get("tokenType", String::class.java))
            } catch (ex: Exception) {
                TokenType.UNKNOWN
            }

        when (tokenType) {
            TokenType.EMAIL_VERIFICATION -> throw CustomException(ErrorCode.EXPIRED_EMAIL_VERIFICATION_TOKEN)
            TokenType.SOCIAL_SIGNUP -> throw CustomException(ErrorCode.EXPIRED_SOCIAL_SIGNUP_TOKEN)
            else -> throw CustomException(ErrorCode.EXPIRED_TOKEN)
        }
    }

    enum class TokenType {
        EMAIL_VERIFICATION,
        SOCIAL_SIGNUP,
        UNKNOWN,
    }
}
