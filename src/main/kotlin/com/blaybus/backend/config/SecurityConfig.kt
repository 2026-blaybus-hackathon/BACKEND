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
                        "/api/v1/test/**",
                    ).permitAll()
                it.requestMatchers("/api/v1/mentor/**").hasAuthority(Role.MENTOR.name)
                it.requestMatchers("/api/v1/daily-planner/{dailyPlannerId}/feedback").hasAuthority(Role.MENTOR.name)
                it.requestMatchers("/api/v1/mentees/**").hasAuthority(Role.MENTOR.name)

                // 2. 멘토(MENTOR) 전용 기능
                // - 과제 할당 (tasks/assignment)
                // - 멘티 과제 조회 (tasks/mentee/**)
                // - 내 멘티 목록 조회 (users/**)
                it.requestMatchers("/api/v1/tasks/assignment", "/api/v1/tasks/mentee/**").hasAuthority(Role.MENTOR.name)
                it
                    .requestMatchers("/api/v1/users/**")
                    .hasAuthority(Role.MENTOR.name) //  TODO : 멘티도 자기 프로필을 users/mentees로 조회하도록 구현해주셔서 뺴야할 듯 합니다.

                // 3. 멘티(MENTEE) 전용 기능
                // - 위의 멘토 전용 URL을 제외한 나머지 tasks 관련 기능은 멘티가 사용
                // - (주의: 이 줄이 멘토 설정보다 아래에 있어야 함!)
                it.requestMatchers("/api/v1/tasks/**").hasAuthority(Role.MENTEE.name)
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
        configuration.maxAge = 3600L
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
