package com.millo.ilhayoung.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.millo.ilhayoung.auth.domain.BlacklistedToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(
        @Value("\${spring.data.redis.host}") host: String,
        @Value("\${spring.data.redis.port}") port: Int,
        @Value("\${spring.data.redis.password:}") password: String,
        @Value("\${spring.data.redis.database:0}") database: Int
    ): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration(host, port).apply {
            this.database = database
            if (password.isNotBlank()) this.setPassword(password)
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(redisConnectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
    }
    
    @Bean
    fun blacklistedTokenObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
    }
    
    @Bean
    fun blacklistedTokenRedisTemplate(
        redisConnectionFactory: LettuceConnectionFactory,
        blacklistedTokenObjectMapper: ObjectMapper
    ): RedisTemplate<String, BlacklistedToken> {
        return RedisTemplate<String, BlacklistedToken>().apply {
            setConnectionFactory(redisConnectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(blacklistedTokenObjectMapper)
        }
    }
}
