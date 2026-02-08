package com.blaybus.backend.security

import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user =
            userRepository.findByEmail(email)
                ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return CustomUserDto(
            email = user.email,
            password = user.password,
            authorities = listOf(SimpleGrantedAuthority(user.role.name)),
            user = user,
        )
    }
}

class CustomUserDto(
    email: String,
    password: String,
    authorities: Collection<SimpleGrantedAuthority>,
    val user: com.blaybus.backend.entity.User,
) : User(email, password, authorities)
