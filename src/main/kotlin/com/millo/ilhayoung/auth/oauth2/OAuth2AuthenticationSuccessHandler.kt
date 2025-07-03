package com.millo.ilhayoung.auth.oauth2

import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.service.AuthService
import com.millo.ilhayoung.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**
 * OAuth2 로그인 성공 처리 핸들러 (모바일 최적화)
 * JWT 토큰을 JSON 응답으로 반환
 */
@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    @Autowired
    private lateinit var authService: AuthService
    
    @Autowired
    private lateinit var userRepository: UserRepository

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        println("🔥 OAuth2 로그인 성공 - 모바일 앱용 처리 시작")
        
        try {
            when (val principal = authentication.principal) {
                is CustomOAuth2User -> {
                    val user = principal.getUser()
                    handleUserLogin(user, request, response)
                }
                is OidcUser -> {
                    handleOidcUserLogin(principal, request, response)
                }
                is OAuth2User -> {
                    handleOAuth2UserLogin(principal, authentication, request, response)
                }
                else -> {
                    sendErrorResponse(response, "지원하지 않는 인증 타입입니다.")
                }
            }
        } catch (e: Exception) {
            println("🔥 OAuth2 처리 중 오류 발생: ${e.message}")
            sendErrorResponse(response, "인증 처리 중 오류가 발생했습니다.")
        }
    }

    private fun handleUserLogin(user: com.millo.ilhayoung.user.domain.User, request: HttpServletRequest, response: HttpServletResponse) {
        println("🔥 사용자 로그인 처리: ${user.email}, 타입: ${user.userType}, 추가정보필요: ${user.needAdditionalInfo}")
        
        // JWT 토큰 생성 (항상 현재 사용자 상태로 발행)
        val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.userType, user.email)
        val refreshTokenValue = jwtTokenProvider.createRefreshToken(user.id!!)

        // Refresh Token 저장
        saveRefreshToken(user.id!!, refreshTokenValue, request)

        // 로그인 시간 업데이트
        user.updateLastLogin()
        userRepository.save(user)

        // JSON 응답 전송
        sendSuccessResponse(accessToken, refreshTokenValue, user, response)
    }

    private fun handleOidcUserLogin(oidcUser: OidcUser, request: HttpServletRequest, response: HttpServletResponse) {
        // OIDC User는 현재 CustomOAuth2UserService를 통해 처리되지 않으므로 
        // 직접 사용자 조회/생성 (Google OAuth는 일반적으로 CustomOAuth2User로 처리됨)
        val email = oidcUser.email ?: ""
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다. CustomOAuth2UserService를 통해 먼저 처리되어야 합니다.") }
        
        handleUserLogin(user, request, response)
    }

    private fun handleOAuth2UserLogin(oAuth2User: OAuth2User, authentication: Authentication, request: HttpServletRequest, response: HttpServletResponse) {
        // OAuth2User는 현재 CustomOAuth2UserService를 통해 처리되지 않으므로
        // 직접 사용자 조회 (일반적으로 CustomOAuth2User로 처리됨)
        val registrationId = getRegistrationId(authentication)
        val oAuthUserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.attributes)
        val email = oAuthUserInfo.getEmail()
        
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다. CustomOAuth2UserService를 통해 먼저 처리되어야 합니다.") }
        
        handleUserLogin(user, request, response)
    }

    private fun getRegistrationId(authentication: Authentication): String {
        return if (authentication is OAuth2AuthenticationToken) {
            authentication.authorizedClientRegistrationId
        } else {
            "google"
        }
    }

    /**
     * 성공 응답 전송 (모바일 앱용 JSON)
     */
    private fun sendSuccessResponse(
        accessToken: String, 
        refreshToken: String, 
        user: com.millo.ilhayoung.user.domain.User, 
        response: HttpServletResponse
    ) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_OK
        
        val jsonResponse = """
            {
                "success": true,
                "data": {
                    "accessToken": "$accessToken",
                    "refreshToken": "$refreshToken",
                    "userType": "${user.userType?.code ?: ""}",
                    "needAdditionalInfo": ${user.needAdditionalInfo},
                    "user": {
                        "id": "${user.id}",
                        "email": "${user.email}",
                        "oauthName": "${user.oauthName ?: ""}"
                    }
                }
            }
        """.trimIndent()
        
        response.writer.write(jsonResponse)
        response.writer.flush()
        
        println("🔥 모바일 앱용 JSON 응답 전송 완료")
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
    }

    /**
     * Refresh Token 저장 (디바이스 정보 포함)
     */
    private fun saveRefreshToken(userId: String, refreshTokenValue: String, request: HttpServletRequest) {
        val userAgent = request.getHeader("User-Agent")
        val ipAddress = getClientIpAddress(request)
        
        authService.saveRefreshTokenWithDeviceInfo(
            userId = userId,
            refreshTokenValue = refreshTokenValue,
            userAgent = userAgent,
            ipAddress = ipAddress
        )
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")
        
        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr
        }
    }
} 