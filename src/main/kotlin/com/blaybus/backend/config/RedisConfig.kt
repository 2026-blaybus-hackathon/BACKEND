package com.blaybus.backend.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories
class RedisConfig {
    @Value($$"${spring.data.redis.password}")
    private val password = ""

    @Value($$"${spring.data.redis.database}")
    private val database = -1

    @Value($$"${spring.data.redis.host}")
    private val host = ""

    @Value($$"${spring.data.redis.port}")
    private val port = -1

    @Bean
    fun redissonClient(): RedissonClient {
        var redisson: RedissonClient
        val config = Config()
        config
            .useSingleServer()
            .setAddress("redis://$host:$port")
            .setPassword(password)
            .setDatabase(database)
        redisson = Redisson.create(config)
        return redisson
    }
}
