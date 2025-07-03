package com.millo.ilhayoung.auth.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

/**
 * OAuth2 로그인 실패 처리 핸들러 (모바일 최적화)
 * JSON 에러 응답 반환
 */
@Component
class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        println("🔥 OAuth2 로그인 실패: ${exception.message}")
        
        sendErrorResponse(response, exception.localizedMessage ?: "OAuth2 인증에 실패했습니다.")
    }

    /**
     * 에러 응답 전송 (모바일 앱용 JSON)
     */
    private fun sendErrorResponse(response: HttpServletResponse, message: String) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_BAD_REQUEST
        
        val jsonResponse = """
            {
                "success": false,
                "error": {
                    "code": "oauth2_authentication_failed",
                    "message": "$message"
                }
            }
        """.trimIndent()
        
        response.writer.write(jsonResponse)
        response.writer.flush()
        
        println("🔥 OAuth2 실패 JSON 응답 전송 완료")
    }
} 