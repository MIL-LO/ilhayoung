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

                val user = findOrCreateOAuth(email, provider, providerId, name)
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
        println("=== OAuth2 성공 핸들러 디버깅 ===")
        println("OAuth ID: ${oauth.id}")
        println("OAuth Email: ${oauth.email}")
        
        val staffOpt = staffRepository.findById(oauth.id!!)
        val managerOpt = managerRepository.findById(oauth.id!!)
        
        println("Staff 존재 여부: ${staffOpt.isPresent}")
        println("Manager 존재 여부: ${managerOpt.isPresent}")
        
        if (staffOpt.isPresent) {
            val staff = staffOpt.get()
            println("Staff 상태: ${staff.status}, 삭제됨: ${staff.isDeleted()}")
        }
        
        if (managerOpt.isPresent) {
            val manager = managerOpt.get()
            println("Manager 상태: ${manager.status}, 삭제됨: ${manager.isDeleted()}")
        }
        
        // 요청된 역할 확인 (세션에서 먼저 확인, 없으면 URL 파라미터에서)
        val requestedRole = request.session.getAttribute("requestedRole") as? String 
            ?: request.getParameter("role") 
            ?: "STAFF"
        println("요청된 역할: $requestedRole")
        
        // 세션에서 role 제거 (한 번만 사용)
        request.session.removeAttribute("requestedRole")
        
        when {
            // Staff로 이미 회원가입 완료
            staffOpt.isPresent && staffOpt.get().isActive() -> {
                println("✅ Staff로 이미 가입됨 - Staff 로그인 처리")
                val staff = staffOpt.get()
                
                // 요청된 역할이 Manager인 경우 경고 메시지와 함께 Staff로 로그인
                if (requestedRole == "MANAGER") {
                    println("⚠️ Manager로 요청했지만 Staff로 가입됨 - 로그인 거부 및 안내")
                    val responseBody = SimpleOAuthResponse(
                        success = false,
                        message = "이미 STAFF로 가입된 계정입니다. 다른 계정으로 시도하거나 STAFF로 로그인하세요.",
                        accessToken = ""
                    )
                    sendResponse(request, response, responseBody)
                    return
                } else {
                    // 정상적인 Staff 로그인
                    println("✅ 정상적인 Staff 로그인")
                    val accessToken = jwtTokenProvider.createAccessToken(
                        userId = staff.id,
                        userType = "STAFF",
                        status = "ACTIVE",
                        email = oauth.email
                    )
                    
                    val refreshToken = jwtTokenProvider.createRefreshToken(staff.id)
                    val refreshTokenEntity = RefreshToken.create(
                        token = refreshToken,
                        userId = staff.id,
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
            }
            
            // Manager로 이미 회원가입 완료
            managerOpt.isPresent && managerOpt.get().isActive() -> {
                println("✅ Manager로 이미 가입됨 - Manager 로그인 처리")
                val manager = managerOpt.get()
                
                // 요청된 역할이 Staff인 경우 로그인 거부(에러 반환)
                if (requestedRole == "STAFF") {
                    println("❌ Manager 계정은 STAFF로 로그인할 수 없습니다. 로그인 거부!")
                    val responseBody = SimpleOAuthResponse(
                        success = false,
                        message = "이미 Manager로 가입된 계정입니다. STAFF로 로그인할 수 없습니다.",
                        accessToken = ""
                    )
                    sendResponse(request, response, responseBody)
                    return
                } else {
                    // 정상적인 Manager 로그인
                    println("✅ 정상적인 Manager 로그인")
                    val accessToken = jwtTokenProvider.createAccessToken(
                        userId = manager.id!!,
                        userType = "MANAGER",
                        status = "ACTIVE",
                        email = oauth.email
                    )
                    
                    val refreshToken = jwtTokenProvider.createRefreshToken(manager.id!!)
                    val refreshTokenEntity = RefreshToken.create(
                        token = refreshToken,
                        userId = manager.id!!,
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
            }
            
            // 삭제된 계정들 처리
            (staffOpt.isPresent && staffOpt.get().isDeleted()) || 
            (managerOpt.isPresent && managerOpt.get().isDeleted()) -> {
                println("❌ 삭제된 계정")
                handleDeletedUser(request, response, "삭제된 계정입니다.")
            }
            
            // 아직 회원가입하지 않음
            else -> {
                println("❓ 아직 회원가입하지 않음 - PENDING 상태")
                
                // 요청된 역할에 따라 적절한 userType 설정
                val userTypeForToken = when (requestedRole) {
                    "MANAGER" -> "MANAGER"
                    else -> "STAFF" // 기본값은 STAFF
                }
                
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = oauth.id!!,
                    userType = userTypeForToken,
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