package com.millo.ilhayoung.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * CORS 설정을 위한 Config
 */
@Configuration
class CorsConfig(val env: Environment) {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val allowedOrigins = env.getProperty("CORS_ALLOWED_ORIGINS")?.split(",")?.map { it.trim() } ?: emptyList()


        val config = CorsConfiguration().apply {
            allowedOriginPatterns = allowedOrigins
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }


}
