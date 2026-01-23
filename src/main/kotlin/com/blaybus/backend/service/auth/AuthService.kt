package com.blaybus.backend.service.auth
import com.blaybus.backend.config.OAuth2Properties
import com.blaybus.backend.dto.EmailLoginRequest
import com.blaybus.backend.dto.EmailSignupRequest
import com.blaybus.backend.dto.GoogleLoginRequest
import com.blaybus.backend.dto.GoogleSignUpRequest
import com.blaybus.backend.dto.GoogleTokenResponse
import com.blaybus.backend.dto.GoogleUserInfo
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import com.blaybus.backend.entity.auth.BlackListAccessToken
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.auth.BlackListAccessTokenRepository
import com.blaybus.backend.repository.auth.RedisRefreshTokenRepository
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.security.CustomUserDto
import com.blaybus.backend.security.JwtTokenProvider
import mu.KotlinLogging
import org.apache.http.HttpHost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import tools.jackson.databind.ObjectMapper
import java.util.Date

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val objectMapper: ObjectMapper,
    private val oAuth2Properties: OAuth2Properties,
    private val refreshTokenRepository: RedisRefreshTokenRepository,
    private val blackListAccessTokenRepository: BlackListAccessTokenRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun signupWithEmail(request: EmailSignupRequest) {
        val email = jwtTokenProvider.getClaim(request.emailVerifyToken, "email", String::class.java)
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            throw CustomException(ErrorCode.REGISTERED_ALREADY)
        }
        if (!nicknameDuplicateCheck(request.nickname)) {
            throw CustomException(ErrorCode.CONFLICT_NICKNAME)
        }
        val user =
            User(
                email = email,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                nickname = request.nickname,
                provider = Provider.LOCAL,
                role = Role.USER,
                contactEmail = email,
            )
        userRepository.save(user)
    }

    @Transactional
    fun signUpOAuth(request: GoogleSignUpRequest): TokenResponse {
        val type = jwtTokenProvider.getClaim(request.socialSignUpToken, "provider", String::class.java)
        val socialAccessToken = jwtTokenProvider.getClaim(request.socialSignUpToken, "oAuth2IdToken", String::class.java)
        val tokenType = Provider.valueOf(type)
        val user: User =
            when (tokenType) {
                Provider.GOOGLE -> {
                    val oauthGoogleUserInfo = getGoogleUserInfo(socialAccessToken)
                    User(
                        nickname = request.nickname,
                        provider = Provider.GOOGLE,
                        providerId = oauthGoogleUserInfo.sub,
                        email = oauthGoogleUserInfo.email,
                        contactEmail = oauthGoogleUserInfo.email,
                        password = null,
                        name = "${oauthGoogleUserInfo.givenName} ${oauthGoogleUserInfo.familyName}",
                        role = Role.USER,
                    )
                }

                else -> {
                    throw CustomException(ErrorCode.UNSUPPORTED_SOCIAL_TYPE)
                }
            }
        val existingUser = userRepository.findByEmail(user.email)
        if (existingUser != null) {
            throw CustomException(ErrorCode.REGISTERED_ALREADY)
        }
        if (!nicknameDuplicateCheck(user.nickname)) {
            throw CustomException(ErrorCode.CONFLICT_NICKNAME)
        }
        userRepository.save(user)

        return jwtTokenProvider.getTokenResponse(user.id, jwtTokenProvider.getAuthorities(user.role), user.nickname)
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
                userDetails.userId,
                userDetails.authorities,
                userDetails.nickname,
            )
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_CREDENTIALS, e.message)
        }
    }

    @Transactional
    fun loginWithOauth2(request: GoogleLoginRequest): TokenResponse {
        val user: User?
        when (request.provider) {
            Provider.GOOGLE -> {
                val tokenResponse = getGoogleToken(request.code)
                val googleUserInfo = getGoogleUserInfo(tokenResponse.idToken)
                user = userRepository.findByProviderAndProviderId(Provider.GOOGLE, googleUserInfo.sub)
                if (user == null) {
                    return TokenResponse(
                        jwtTokenProvider.generateOAuth2Token(tokenResponse.idToken, Provider.GOOGLE),
                        null,
                        null,
                    )
                }
            }

            else -> {
                throw CustomException(ErrorCode.UNSUPPORTED_SOCIAL_TYPE)
            }
        }
        return jwtTokenProvider.getTokenResponse(user.id, jwtTokenProvider.getAuthorities(user.role), user.nickname)
    }

    fun nicknameDuplicateCheck(nickname: String): Boolean = !userRepository.existsByNickname(nickname)

    private fun getGoogleUserInfo(idToken: String): GoogleUserInfo {
        val restClient = RestClient.builder(HttpHost("oauth2.googleapis.com", 443, "https")).build()
        val request = Request("GET", "/tokeninfo")
        request.addParameter("id_token", idToken)
        try {
            val content =
                restClient
                    .performRequest(request)
                    .entity.content
                    .bufferedReader()
                    .use { it.readText() }
            return objectMapper.readValue(content, GoogleUserInfo::class.java)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_SIGNUP_TOKEN, e.message)
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

    @Transactional
    fun refresh(refreshToken: String): TokenResponse {
        val userId = jwtTokenProvider.getClaim(refreshToken, "userId", Long::class.java)
        val storedToken = refreshTokenRepository.findByUserId(userId)
        if (storedToken == null || refreshToken != storedToken.token) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val user = userRepository.getByUserId(userId)

        return jwtTokenProvider.getTokenResponse(user.id, jwtTokenProvider.getAuthorities(user.role), user.nickname)
    }

    private fun getGoogleToken(code: String): GoogleTokenResponse {
        val clientSecret: String = oAuth2Properties.clientSecret
        val redirectUri: String = oAuth2Properties.redirectUri.trim()
        val clientId: String = oAuth2Properties.clientId
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("code", code)
        params.add("client_id", clientId)
        params.add("client_secret", clientSecret)
        params.add("redirect_uri", redirectUri)
        params.add("grant_type", "authorization_code")

        val form =
            listOf(
                "code" to code,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "redirect_uri" to redirectUri,
                "grant_type" to "authorization_code",
            ).joinToString("&") { (k, v) ->
                "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
            }
        val restClient = RestClient.builder(org.apache.http.HttpHost("oauth2.googleapis.com", 443, "https")).build()
        val request = Request("POST", "/token")
        request.entity = StringEntity(form, ContentType.APPLICATION_FORM_URLENCODED)
        try {
            val response = restClient.performRequest(request)
            val content =
                EntityUtils
                    .toString(response.entity)
            return objectMapper.readValue(content, GoogleTokenResponse::class.java)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_CODE, e.message)
        } finally {
            try {
                restClient.close()
            } catch (_: Exception) {
            }
        }
    }
}
