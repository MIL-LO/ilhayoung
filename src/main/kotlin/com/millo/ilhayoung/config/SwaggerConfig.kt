package com.millo.ilhayoung.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class SwaggerConfig(val env: Environment) {

    @Bean
    fun openAPI(): OpenAPI {
        val servers = listOf(
            "SWAGGER_SERVER_LOCAL" to "로컬",
            "SWAGGER_SERVER_PROD" to "운영",
        ).mapNotNull { (envKey, name) ->
            env.getProperty(envKey)?.let { url -> Server().url(url).description(name) }
        }
        return OpenAPI()
            .info(
                Info()
                    .title("일하영 API")
                    .description("일하영 서비스의 API 문서입니다.")
                    .version("v1.0.0")
            )
            .servers(servers)
            .components(
                Components()
                    .addSecuritySchemes(
                        "BearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("BearerAuth")
            )
    }

}
