package com.millo.ilhayoung.config

import com.millo.ilhayoung.auth.jwt.JwtAuthenticationFilter
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 설정을 담당하는 클래스
 * JWT 인증, CORS, 권한 관리 등을 설정
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * Spring Security FilterChain 설정
     * JWT 기반 인증을 사용하며 세션은 사용하지 않음
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf { it.disable() }
            
            // CORS 설정 적용
            .cors { it.configurationSource(corsConfigurationSource()) }
            
            // 세션 사용 안함 (JWT 사용)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            
            // HTTP 요청에 대한 권한 설정
            .authorizeHttpRequests { authz ->
                authz
                    // 인증 없이 접근 가능한 엔드포인트
                    .requestMatchers(
                        "/api/v1/oauth/**",           // OAuth 로그인
                        "/api/v1/auth/refresh",       // 토큰 재발급
                        "/api/v1/external/**",        // 외부 API (사업자등록번호 조회 등)
                        
                        // Swagger 관련
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        
                        // 기타
                        "/health",
                        "/actuator/**"
                    ).permitAll()
                    
                    // STAFF 전용 엔드포인트
                    .requestMatchers(
                        "/api/v1/users/staff/**"
                    ).hasRole("STAFF")
                    
                    // MANAGER 전용 엔드포인트
                    .requestMatchers(
                        "/api/v1/users/manager/**",
                        "/api/v1/recruits/**",
                        "/api/v1/attendances/register/**",
                        "/api/v1/salaries/calculate/**",
                        "/api/v1/salaries/pay/**"
                    ).hasRole("MANAGER")
                    
                    // 인증된 사용자만 접근 가능한 엔드포인트
                    .requestMatchers(
                        "/api/v1/users/me",
                        "/api/v1/auth/logout",
                        "/api/v1/attendances/**",
                        "/api/v1/salaries/**",
                        "/api/v1/trust-scores/**"
                    ).authenticated()
                    
                    // 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            
            // JWT 필터 추가
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            
            .build()
    }

    /**
     * CORS 설정
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // 허용할 Origin (프론트엔드 도메인)
            allowedOriginPatterns = listOf("*")
            
            // 허용할 HTTP 메서드
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            
            // 허용할 헤더
            allowedHeaders = listOf("*")
            
            // 인증 정보 포함 허용
            allowCredentials = true
            
            // Preflight 요청 캐시 시간 (초)
            maxAge = 3600L
            
            // 응답 헤더에 노출할 헤더
            exposedHeaders = listOf("Authorization", "Content-Type")
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
} 