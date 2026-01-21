package kr.weit.roadyfoody.global.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP) // Redis Key-Value 저장소 활성화
class RedisConfig {
    @Value($$"${spring.data.redis.password}")
    private val password: String? = null

    @Value($$"${spring.data.redis.database}")
    private val database = 0

    @Value($$"${spring.data.redis.host}")
    private val host: String? = null

    @Value($$"${spring.data.redis.port}")
    private val port = 0

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
