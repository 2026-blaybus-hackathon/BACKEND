package com.blaybus.backend.service.auth
import com.blaybus.backend.dto.EmailLoginRequest
import com.blaybus.backend.dto.EmailSignupRequest
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.entity.User
import com.blaybus.backend.entity.auth.BlackListAccessToken
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.auth.BlackListAccessTokenRepository
import com.blaybus.backend.repository.auth.RedisRefreshTokenRepository
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.security.CustomUserDto
import com.blaybus.backend.security.JwtTokenProvider
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.Date

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val refreshTokenRepository: RedisRefreshTokenRepository,
    private val blackListAccessTokenRepository: BlackListAccessTokenRepository,
    private val objectStorageRepository: ObjectStorageRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun signupWithEmail(
        request: EmailSignupRequest,
        profile: MultipartFile?,
    ) {
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) {
            throw CustomException(ErrorCode.REGISTERED_ALREADY)
        }
        val user =
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password)!!,
                name = request.name,
                role = request.role,
                originFileName = profile?.originalFilename,
                profileName =
                    profile?.let {
                        objectStorageRepository.upload(
                            ObjectStorageRepository.PROFILE_IMAGE_PATH,
                            it,
                        )
                    },
                schoolName = request.schoolName,
                grade = request.grade,
                targetSchool = request.targetSchool,
                targetDate = request.targetDate,
            )
        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun loginWithEmail(request: EmailLoginRequest): TokenResponse {
        try {
            val authentication: Authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(request.email, request.password),
                )
            val userDetails = authentication.principal as CustomUserDto
            return jwtTokenProvider.getTokenResponse(
                userDetails.authorities,
                userDetails.user,
            )
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_CREDENTIALS, e.message)
        }
    }

    fun logout(accessToken: String) {
        val userId = jwtTokenProvider.getClaim(accessToken, "userId", Long::class.java)
        val existingRefreshToken = refreshTokenRepository.findByUserId(userId)
        if (existingRefreshToken != null) {
            refreshTokenRepository.delete(existingRefreshToken) // 리프레시 토큰 삭제
            logger.info("리프레시 토큰 삭제: {}", userId)
        }
        try {
            val expiration: Date = jwtTokenProvider.getTokenExpirationTime(accessToken)
            val ttl = (expiration.time - System.currentTimeMillis()) / 1000 // 초로 변환
            if (ttl > 0) {
                blackListAccessTokenRepository.save(BlackListAccessToken(accessToken, ttl))
                logger.info("액세스 토큰 블랙리스트 추가: {}, TTL: {}초", userId, ttl)
            }
        } catch (e: Exception) {
            logger.error("액세스 토큰 블랙리스트 추가 실패: {}", e.message)
        }
    }

    fun getUserAccessToken(userId: Long): String {
        val user = userRepository.getByUserId(userId)
        return jwtTokenProvider.generateAccessToken(userId, jwtTokenProvider.getAuthorities(user.role))
    }

    @Transactional
    fun refresh(refreshToken: String): TokenResponse {
        val userId = jwtTokenProvider.getClaim(refreshToken, "userId", Long::class.java)
        val storedToken = refreshTokenRepository.findByUserId(userId)
        if (storedToken == null || refreshToken != storedToken.token) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val user = userRepository.getByUserId(userId)

        return jwtTokenProvider.getTokenResponse(jwtTokenProvider.getAuthorities(user.role), user)
    }
}
