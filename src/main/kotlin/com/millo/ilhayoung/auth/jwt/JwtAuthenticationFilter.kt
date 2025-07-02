package com.millo.ilhayoung.auth.jwt

import com.millo.ilhayoung.common.exception.BusinessException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 토큰을 검증하는 인증 필터
 * 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증한다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * 필터 실행 메서드
     * Authorization 헤더에서 JWT 토큰을 추출하고 검증하여 Security Context에 인증 정보를 설정
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 요청에서 JWT 토큰 추출
            val token = resolveToken(request)
            
            // 토큰이 존재하고 유효한 경우
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Access Token인지 확인
                if (jwtTokenProvider.isAccessToken(token)) {
                    // 토큰에서 인증 정보 추출하여 Security Context에 설정
                    val authentication = jwtTokenProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    // Refresh Token으로 요청한 경우 (잘못된 요청)
                    logger.warn("Access Token이 아닌 Refresh Token으로 요청: ${request.requestURI}")
                }
            }
        } catch (e: Exception) {
            // JWT 토큰 처리 중 예외 발생 시 로그 기록
            logger.error("JWT 토큰 처리 중 오류 발생: ${e.message}")
            SecurityContextHolder.clearContext()
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response)
    }

    /**
     * 요청에서 JWT 토큰을 추출하는 메서드
     * Authorization 헤더에서 "Bearer " 접두사를 제거하고 토큰만 반환한다.
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 문자열, 없으면 null
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    /**
     * 특정 요청에 대해 필터를 적용하지 않을지 결정하는 메서드
     * 현재는 모든 요청에 대해 필터를 적용
     * 
     * @param request HTTP 요청
     * @return 필터를 건너뛸지 여부 (현재는 항상 false)
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        // 특정 경로에 대해 JWT 필터를 건너뛰기
        // "/api/v1/oauth/**" 경로는 JWT 검증이 불필요
        val path = request.requestURI
        
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path == "/health" ||
               path.startsWith("/actuator")
    }
} 