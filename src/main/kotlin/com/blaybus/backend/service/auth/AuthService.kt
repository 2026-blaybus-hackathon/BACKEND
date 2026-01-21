package com.blaybus.backend.service.auth
import com.blaybus.backend.dto.EmailLoginRequest
import com.blaybus.backend.dto.EmailSignupRequest
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.security.CustomUserDto
import com.blaybus.backend.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
) {
    @Transactional
    fun signupWithEmail(request: EmailSignupRequest) {
        val email = jwtTokenProvider.getClaim(request.emailVerifyToken, "email", String::class.java)
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            if (existingUser.provider != Provider.LOCAL) {
                throw CustomException(ErrorCode.REGISTERED_ALREADY)
            } else {
                throw CustomException(ErrorCode.REGISTERED_ANOTHER_SOCIAL)
            }
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

    /*@Transactional
    fun signupWithGoogle(request: GoogleSignUpRequest): TokenResponse {
        val email = jwtTokenProvider.getClaim(request.socialSignUpToken, "email", String::class.java)
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            if (existingUser.provider != Provider.GOOGLE) {
                throw CustomException(ErrorCode.REGISTERED_ALREADY)
            } else {
                throw CustomException(ErrorCode.REGISTERED_ANOTHER_SOCIAL)
            }
        }
        val user =
            User(
                email = request.email,
                name = request.name,
                nickname = request.nickname,
                contactEmail = request.email,
                provider = Provider.GOOGLE,
                role = Role.USER,
            )
        userRepository.save(user)

        val token = jwtTokenProvider.createToken(user.email, user.role.name)
        return TokenResponse(token)
    }*/

    @Transactional(readOnly = true)
    fun loginWithEmail(request: EmailLoginRequest): TokenResponse {
        try {
            val authentication: Authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(request.email, request.password),
                )
            val userDetails = authentication.principal as CustomUserDto
            return jwtTokenProvider.getTokenResponse(
                userDetails.userId.toInt(),
                userDetails.authorities,
                userDetails.nickname,
            )
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_CREDENTIALS, e.message)
        }
    }
}
