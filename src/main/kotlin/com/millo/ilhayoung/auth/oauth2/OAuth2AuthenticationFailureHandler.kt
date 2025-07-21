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
        val errorMessage = getErrorMessage(exception.localizedMessage ?: "OAuth2 인증에 실패했습니다.")
        sendErrorResponse(response, errorMessage)
    }

    /**
     * 에러 메시지 변환 (사용자 친화적)
     */
    private fun getErrorMessage(originalMessage: String): String {
        return when {
            originalMessage.contains("authorization_request_not_found") -> 
                "인증 요청이 만료되었습니다. 다시 로그인해주세요."
            originalMessage.contains("invalid_grant") -> 
                "인증이 만료되었습니다. 다시 로그인해주세요."
            originalMessage.contains("access_denied") -> 
                "로그인이 취소되었습니다."
            originalMessage.contains("server_error") -> 
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            else -> "로그인에 실패했습니다. 다시 시도해주세요."
        }
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
                "message": "$message",
                "accessToken": null,
                "refreshToken": null
            }
        """.trimIndent()
        
        response.writer.write(jsonResponse)
        response.writer.flush()
    }
} 