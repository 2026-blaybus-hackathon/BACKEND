package com.blaybus.backend.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(
    JwtProperties::class,
    EmailProperties::class,
)
@Configuration
class EnablePropertiesConfig
