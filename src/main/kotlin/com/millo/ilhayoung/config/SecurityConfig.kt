package com.millo.ilhayoung.config

import com.millo.ilhayoung.auth.jwt.JwtAuthenticationFilter
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.oauth2.CustomOAuth2UserService
import com.millo.ilhayoung.auth.oauth2.OAuth2AuthenticationFailureHandler
import com.millo.ilhayoung.auth.oauth2.OAuth2AuthenticationSuccessHandler
import org.springframework.beans.factory.annotation.Value
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
 * Spring Security 설정을 담당하는 클래스 (모바일 최적화)
 * JWT 인증, CORS, 권한 관리 등을 설정
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Value("\${cors.allowed-origins}")
    private lateinit var allowedOrigins: String

    /**
     * Spring Security FilterChain 설정 (모바일 앱용)
     * JWT 기반 Stateless 인증과 OAuth2 Login을 함께 사용
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // CSRF 비활성화 (모바일 앱에서 불필요)
            .csrf { it.disable() }
            
            // CORS 설정 적용
            .cors { it.configurationSource(corsConfigurationSource()) }
            
            // 세션 정책: Stateless (모바일 앱용)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            
            // HTTP 요청에 대한 권한 설정
            .authorizeHttpRequests { authz ->
                authz
                    // 인증 없이 접근 가능한 엔드포인트
                    .requestMatchers(
                        "/oauth2/**",                 // OAuth2 엔드포인트
                        "/login/oauth2/**",           // OAuth2 Login 엔드포인트  
                        "/api/v1/auth/refresh",       // 토큰 재발급
                        
                        // API 문서 (개발용)
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        
                        // 헬스체크
                        "/health",
                        "/actuator/**"
                    ).permitAll()
                    
                    // 회원가입 API (OAuth2 인증 후 접근 가능)
                    .requestMatchers(
                        "/api/v1/users/staff/signup",
                        "/api/v1/users/manager/signup"
                    ).authenticated()
                    
                    // STAFF 전용 API
                    .requestMatchers(
                        "/api/v1/users/staff/**"
                    ).hasRole("STAFF")
                    
                    // MANAGER 전용 API
                    .requestMatchers(
                        "/api/v1/users/manager/**",
                        "/api/v1/recruits/**",
                        "/api/v1/attendances/register/**",
                        "/api/v1/salaries/calculate/**",
                        "/api/v1/salaries/pay/**"
                    ).hasRole("MANAGER")
                    
                    // 인증된 사용자 API
                    .requestMatchers(
                        "/api/v1/users/me",
                        "/api/v1/auth/**",
                        "/api/v1/attendances/**",
                        "/api/v1/salaries/**",
                        "/api/v1/trust-scores/**"
                    ).authenticated()
                    
                    // 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            
            // OAuth2 Login 설정 (모바일 앱용)
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOAuth2UserService())
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler())
                    .failureHandler(oAuth2AuthenticationFailureHandler())
            }
            
            // JWT 필터 추가
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            
            .build()
    }

    /**
     * CORS 설정 (모바일 앱 최적화)
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // 모바일 앱을 위한 Origin 설정 (테스트용 임시 고정값)
            allowedOriginPatterns = listOf("*", "http://localhost:*", "https://*.vercel.app")
            
            // 모바일 앱에서 사용하는 HTTP 메서드
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            
            // 모바일 앱에서 사용하는 헤더
            allowedHeaders = listOf("*")
            
            // 인증 정보 포함 허용
            allowCredentials = true
            
            // Preflight 요청 캐시 시간
            maxAge = 3600L
            
            // 모바일 앱에 노출할 헤더
            exposedHeaders = listOf("Authorization", "Content-Type")
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    /**
     * 커스텀 OAuth2 사용자 서비스
     */
    @Bean
    fun customOAuth2UserService(): CustomOAuth2UserService {
        return CustomOAuth2UserService()
    }

    /**
     * OAuth2 로그인 성공 핸들러
     */
    @Bean
    fun oAuth2AuthenticationSuccessHandler(): OAuth2AuthenticationSuccessHandler {
        return OAuth2AuthenticationSuccessHandler(jwtTokenProvider)
    }

    /**
     * OAuth2 로그인 실패 핸들러
     */
    @Bean
    fun oAuth2AuthenticationFailureHandler(): OAuth2AuthenticationFailureHandler {
        return OAuth2AuthenticationFailureHandler()
    }
} 