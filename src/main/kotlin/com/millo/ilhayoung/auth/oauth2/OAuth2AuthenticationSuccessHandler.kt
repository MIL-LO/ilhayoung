package com.millo.ilhayoung.auth.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.millo.ilhayoung.auth.domain.OAuth
import com.millo.ilhayoung.auth.domain.RefreshToken
import com.millo.ilhayoung.auth.dto.SimpleOAuthResponse
import com.millo.ilhayoung.auth.dto.OAuthLoginSuccessResponse
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.repository.OAuthRepository
import com.millo.ilhayoung.auth.repository.RefreshTokenRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import com.millo.ilhayoung.user.repository.ManagerRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * OAuth2 로그인 성공 처리 핸들러
 * 1. provider+providerId로 OAuth 조회/생성
 * 2. 회원가입 상태 확인
 * 3. 토큰 발급
 */
@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    @Autowired
    private lateinit var oauthRepository: OAuthRepository
    
    @Autowired
    private lateinit var staffRepository: StaffRepository
    
    @Autowired
    private lateinit var managerRepository: ManagerRepository
    
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal = authentication.principal

        val oauth2User = when (principal) {
            is CustomOAuth2User -> principal
            is org.springframework.security.oauth2.core.oidc.user.OidcUser -> {
                val attributes = principal.attributes
                val email = attributes["email"] as? String ?: throw IllegalStateException("Email not found in OIDC attributes")
                val name = attributes["name"] as? String ?: ""
                val provider = "google"
                val providerId = attributes["sub"].toString()

                // OAuth 조회 또는 생성
                val user = oauthRepository.findByEmail(email).orElseGet {
                    val newUser = OAuth.createFromOAuth(
                        email = email,
                        provider = provider,
                        providerId = providerId,
                        oauthName = name
                    )
                    oauthRepository.save(newUser)
                    newUser
                }

                CustomOAuth2User.create(user, attributes)
            }
            else -> throw IllegalArgumentException("Unexpected principal type: ${principal::class}")
        }

        val email = oauth2User.email
        val provider = oauth2User.provider
        val providerId = oauth2User.providerId
        val oauthName = oauth2User.displayName
        
        // 모바일 앱 여부를 세션에 저장
        val userAgent = request.getHeader("User-Agent") ?: ""
        val isMobileRequest = userAgent.contains("Mobile", ignoreCase = true) || 
                             userAgent.contains("Android", ignoreCase = true) || 
                             userAgent.contains("iPhone", ignoreCase = true) ||
                             userAgent.contains("iPad", ignoreCase = true) ||
                             userAgent.contains("Flutter", ignoreCase = true) ||
                             userAgent.contains("WebView", ignoreCase = true) ||
                             request.getParameter("mobile") == "true"
        
        if (isMobileRequest) {
            request.session.setAttribute("isMobileApp", true)
        }
        
        // provider+providerId로 OAuth 조회 또는 생성
        val oauth = findOrCreateOAuth(email, provider, providerId, oauthName)
        
        // 회원가입 상태 확인
        handleOAuthSuccess(request, response, oauth)
        
        // 응답 완료 후 더 이상 처리하지 않음
        return
    }

    /**
     * provider+providerId로 OAuth 조회 또는 생성
     */
    private fun findOrCreateOAuth(email: String, provider: String, providerId: String, oauthName: String): OAuth {
        // provider+providerId로 조회
        val existingOAuth = oauthRepository.findByProviderAndProviderId(provider, providerId)
        
        return if (existingOAuth.isPresent) {
            val oauth = existingOAuth.get()
            
            // OAuth 이름 업데이트
            if (oauth.oauthName != oauthName) {
                oauth.oauthName = oauthName
                oauthRepository.save(oauth)
            }
            oauth
        } else {
            // 새로운 OAuth 생성
            val newOAuth = OAuth.createFromOAuth(
                email = email,
                provider = provider,
                providerId = providerId,
                oauthName = oauthName
            )
            oauthRepository.save(newOAuth)
            newOAuth
        }
    }

    /**
     * OAuth 인증 성공 통합 처리
     */
    private fun handleOAuthSuccess(request: HttpServletRequest, response: HttpServletResponse, oauth: OAuth) {
        val staffOpt = staffRepository.findByUserId(oauth.id!!)
        val managerOpt = managerRepository.findByUserId(oauth.id!!)
        
        when {
            // Staff로 이미 회원가입 완료 → Staff 로그인
            staffOpt.isPresent && staffOpt.get().isActive() -> {
                val staff = staffOpt.get()
                val staffId = staff.id ?: throw IllegalStateException("Staff ID not found")
                
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = staffId,
                    userType = "STAFF",
                    status = "ACTIVE",
                    email = oauth.email
                )
                
                val refreshToken = jwtTokenProvider.createRefreshToken(staffId)
                val refreshTokenEntity = RefreshToken.create(
                    token = refreshToken,
                    userId = staffId,
                    expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
                )
                refreshTokenRepository.save(refreshTokenEntity)
                
                val responseBody = OAuthLoginSuccessResponse(
                    success = true,
                    message = "STAFF 로그인 성공",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
                sendResponse(request, response, responseBody)
            }
            
            // Manager로 이미 회원가입 완료 → Manager 로그인
            managerOpt.isPresent && managerOpt.get().isActive() -> {
                val manager = managerOpt.get()
                val managerId = manager.id ?: throw IllegalStateException("Manager ID not found")
                
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = managerId,
                    userType = "MANAGER",
                    status = "ACTIVE",
                    email = oauth.email
                )
                
                val refreshToken = jwtTokenProvider.createRefreshToken(managerId)
                val refreshTokenEntity = RefreshToken.create(
                    token = refreshToken,
                    userId = managerId,
                    expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
                )
                refreshTokenRepository.save(refreshTokenEntity)
                
                val responseBody = OAuthLoginSuccessResponse(
                    success = true,
                    message = "MANAGER 로그인 성공",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
                sendResponse(request, response, responseBody)
            }
            
            // 삭제된 계정들 처리
            (staffOpt.isPresent && staffOpt.get().isDeleted()) || 
            (managerOpt.isPresent && managerOpt.get().isDeleted()) -> {
                handleDeletedUser(request, response, "삭제된 계정입니다.")
            }
            
            // 아직 회원가입하지 않음
            else -> {
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = oauth.id!!,
                    userType = "PENDING",
                    status = "PENDING",
                    email = oauth.email
                )
                
                val responseBody = SimpleOAuthResponse(
                    success = true,
                    message = "OAuth 인증이 완료되었습니다. 회원가입을 진행해주세요.",
                    accessToken = accessToken
                )
                sendResponse(request, response, responseBody)
            }
        }
    }

    /**
     * 삭제된 사용자 처리
     */
    private fun handleDeletedUser(request: HttpServletRequest, response: HttpServletResponse, message: String) {
        val responseBody = SimpleOAuthResponse(
            success = false,
            message = message,
            accessToken = ""
        )
        sendResponse(request, response, responseBody)
    }

    /**
     * 응답 전송 공통 메서드
     */
    private fun sendResponse(request: HttpServletRequest, response: HttpServletResponse, responseBody: Any) {
        // User-Agent 및 요청 헤더 확인
        val userAgent = request.getHeader("User-Agent") ?: ""
        val acceptHeader = request.getHeader("Accept") ?: ""
        val refererHeader = request.getHeader("Referer") ?: ""
        
        // 모바일 앱 감지 로직
        val sessionMobileFlag = request.session.getAttribute("isMobileApp") as? Boolean ?: false
        val isMobileApp = sessionMobileFlag ||
                         userAgent.contains("Mobile", ignoreCase = true) || 
                         userAgent.contains("Android", ignoreCase = true) || 
                         userAgent.contains("iPhone", ignoreCase = true) ||
                         userAgent.contains("iPad", ignoreCase = true) ||
                         userAgent.contains("Flutter", ignoreCase = true) ||
                         userAgent.contains("WebView", ignoreCase = true) ||
                         acceptHeader.contains("application/json") ||
                         request.getParameter("format") == "json" ||
                         request.getParameter("mobile") == "true"
        
        // 로깅 추가 (디버깅용)
        println("=== OAuth2 Response Debug ===")
        println("User-Agent: $userAgent")
        println("Accept: $acceptHeader")
        println("Referer: $refererHeader")
        println("Is Mobile App: $isMobileApp")
        println("Response Body: ${objectMapper.writeValueAsString(responseBody)}")
        
        if (isMobileApp) {
            // 모바일 앱: JSON 응답
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.status = HttpServletResponse.SC_OK
            response.writer.write(objectMapper.writeValueAsString(responseBody))
            response.writer.flush()
        } else {
            // 브라우저: 임시 HTML 응답
            response.contentType = "text/html"
            response.characterEncoding = "UTF-8"
            response.status = HttpServletResponse.SC_OK
            
            val htmlResponse = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>OAuth2 인증 완료</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 50px; }
                        .container { max-width: 600px; margin: 0 auto; }
                        .success { color: #28a745; }
                        .error { color: #dc3545; }
                        pre { background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>OAuth2 인증 결과</h1>
                        <div class="success">
                            <h2>✅ 인증 성공</h2>
                            <p>OAuth2 인증이 완료되었습니다.</p>
                        </div>
                        <h3>응답 데이터:</h3>
                        <pre>${objectMapper.writeValueAsString(responseBody)}</pre>
                        <p><strong>참고:</strong> 실제 모바일 앱에서는 이 JSON 데이터를 받아서 처리합니다.</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
            
            response.writer.write(htmlResponse)
            response.writer.flush()
        }
    }
} 