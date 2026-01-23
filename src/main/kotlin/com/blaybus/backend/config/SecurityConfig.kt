package com.blaybus.backend.config

import com.blaybus.backend.constants.ALLOWED_HEADERS
import com.blaybus.backend.constants.ALLOWED_ORIGINS
import com.blaybus.backend.entity.Role
import com.blaybus.backend.security.JwtAuthenticationFilter
import com.blaybus.backend.security.handler.CustomAccessDeniedHandler
import com.blaybus.backend.security.handler.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter,
    private val jwtFilterAccessDeniedHandler: CustomAccessDeniedHandler,
    private val jwtFilterAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/health",
                        "/ready",
                    ).permitAll()
                it.requestMatchers("/api/v1/user/**").hasAnyAuthority(Role.USER.name, Role.ADMIN.name)
                it.requestMatchers("/api/v1/admin/**").hasAuthority(Role.ADMIN.name)
            }.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            ).exceptionHandling {
                it
                    .accessDeniedHandler(jwtFilterAccessDeniedHandler) // 권한이 없는 경우
                    .authenticationEntryPoint(jwtFilterAuthenticationEntryPoint) // 인증되지 않은 경우 }
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = ALLOWED_ORIGINS
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = ALLOWED_HEADERS
        configuration.allowCredentials = true
        configuration.setMaxAge(3600L)
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
