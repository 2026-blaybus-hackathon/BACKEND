package com.blaybus.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .components(createSecurityComponents())
            .addSecurityItem(createSecurityRequirement())
            .info(
                Info()
                    .title("Blaybus해커톤 API")
                    .description("Blaybus해커톤 명세서")
                    .version("v1.0.0"),
            )

    private fun createSecurityComponents(): Components? =
        Components()
            .addSecuritySchemes(
                HttpHeaders.AUTHORIZATION,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .`in`(SecurityScheme.In.HEADER)
                    .name(HttpHeaders.AUTHORIZATION),
            )

    private fun createSecurityRequirement(): SecurityRequirement? = SecurityRequirement().addList(HttpHeaders.AUTHORIZATION)
}
