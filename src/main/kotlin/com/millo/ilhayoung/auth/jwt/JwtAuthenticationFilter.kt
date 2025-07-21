package com.millo.ilhayoung.auth.jwt

import com.millo.ilhayoung.common.exception.BusinessException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * JWT 토큰 검증 및 인증 처리
     */
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val path = request.servletPath
        
        // 필터 적용 여부 로깅
        log.debug("Processing request: ${request.method} $path")
        
        // 이미 인증이 완료된 경우 다시 처리하지 않음
        if (SecurityContextHolder.getContext().authentication != null) {
            log.debug("Authentication already exists in SecurityContext")
            filterChain.doFilter(request, response)
            return
        }
        
        // Authorization 헤더에서 토큰 추출
        val authHeader = request.getHeader("Authorization")
        log.debug("Authorization header: $authHeader")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        
        val token = authHeader.substring(7)
        log.debug("Extracted token: $token")
        
        try {
            if (jwtTokenProvider.validateToken(token)) {
                val authentication = jwtTokenProvider.getAuthentication(token)
                
                // 인증 정보 설정 로깅
                log.debug("Setting authentication: $authentication")
                
                SecurityContextHolder.getContext().authentication = authentication
                
                // SecurityContext에 저장된 인증 정보 로깅
                log.debug("Authentication set in SecurityContext: ${SecurityContextHolder.getContext().authentication}")
            }
        } catch (e: Exception) {
            log.error("JWT token validation failed", e)
        }
        
        // 필터 체인 진행 로깅
        log.debug("Proceeding with filter chain")
        filterChain.doFilter(request, response)
        
        // 필터 체인 완료 로깅
        log.debug("Filter chain completed")
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        log.debug("Authorization header: {}", bearerToken)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length)
        }
        return null
    }

    /**
     * 필터 적용 여부 확인
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        
        // 인증이 필요없는 경로는 필터 적용하지 않음
        return path.startsWith("/oauth2") ||
               path.startsWith("/login/oauth2") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path == "/api/v1/auth/refresh" ||
               path == "/api/v1/users/verify-business"
    }
} 