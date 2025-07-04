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
 * OAuth2 로그인 성공 처리 핸들러 (프론트엔드 role 파라미터 기반)
 * 1. 프론트엔드에서 전달한 role 파라미터 확인
 * 2. provider+providerId로 OAuth 조회/생성
 * 3. 선택된 role 기준으로 엔터티 조회 (생성은 회원가입 시)
 * 4. 임시/최종 토큰 발급
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
            else -> throw IllegalArgumentException("Unexpected principal type: \\${principal::class}")
        }

        val email = oauth2User.email
        val provider = oauth2User.provider
        val providerId = oauth2User.providerId
        val oauthName = oauth2User.displayName
        
        // 프론트엔드에서 전달한 role 파라미터 추출
        val selectedRole = request.getParameter("role")?.uppercase() ?: "STAFF"
        
        // provider+providerId로 OAuth 조회 또는 생성
        val oauth = findOrCreateOAuth(email, provider, providerId, oauthName, selectedRole)
        
        // 회원가입 상태 확인 (role 구분 없이 통합 처리)
        handleOAuthSuccess(response, oauth)
    }

    /**
     * provider+providerId로 OAuth 조회 또는 생성
     */
    private fun findOrCreateOAuth(email: String, provider: String, providerId: String, oauthName: String, selectedRole: String): OAuth {
        // provider+providerId로 조회 (시나리오 요구사항)
        val existingOAuth = oauthRepository.findByProviderAndProviderId(provider, providerId)
        
        return if (existingOAuth.isPresent) {
            val oauth = existingOAuth.get()
            var needUpdate = false
            
            // OAuth 이름 업데이트
            if (oauth.oauthName != oauthName) {
                oauth.oauthName = oauthName
                needUpdate = true
            }
            
            // 선택된 역할 정보 업데이트 (매번 로그인할 때마다 프론트엔드에서 전달하는 role로 업데이트)
            if (oauth.selectedRole != selectedRole) {
                oauth.selectedRole = selectedRole
                needUpdate = true
            }
            
            if (needUpdate) {
                oauthRepository.save(oauth)
            }
            oauth
        } else {
            // 새로운 OAuth 생성 (선택된 역할 정보도 함께 저장)
            val newOAuth = OAuth.createFromOAuth(
                email = email,
                provider = provider,
                providerId = providerId,
                oauthName = oauthName
            )
            // 선택된 역할 정보를 OAuth에 임시 저장 (회원가입 시 사용)
            newOAuth.selectedRole = selectedRole
            oauthRepository.save(newOAuth)
        }
    }

    /**
     * OAuth 인증 성공 통합 처리
     * Staff와 Manager 구분 없이 회원가입 상태만 확인
     */
    private fun handleOAuthSuccess(response: HttpServletResponse, oauth: OAuth) {
        val staffOpt = staffRepository.findByUserId(oauth.id!!)
        val managerOpt = managerRepository.findByUserId(oauth.id!!)
        
        when {
            // Staff로 이미 회원가입 완료 → Staff 로그인
            staffOpt.isPresent && staffOpt.get().isActive() -> {
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = oauth.id!!,
                    userType = "STAFF",
                    status = "ACTIVE",
                    email = oauth.email
                )
                
                val refreshToken = jwtTokenProvider.createRefreshToken(oauth.id!!)
                val refreshTokenEntity = RefreshToken.create(
                    token = refreshToken,
                    userId = oauth.id!!,
                    expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
                )
                refreshTokenRepository.save(refreshTokenEntity)
                
                val responseBody = OAuthLoginSuccessResponse(
                    success = true,
                    message = "STAFF 로그인 성공",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
                sendResponse(response, responseBody)
            }
            
            // Manager로 이미 회원가입 완료 → Manager 로그인
            managerOpt.isPresent && managerOpt.get().isActive() -> {
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = oauth.id!!,
                    userType = "MANAGER",
                    status = "ACTIVE",
                    email = oauth.email
                )
                
                val refreshToken = jwtTokenProvider.createRefreshToken(oauth.id!!)
                val refreshTokenEntity = RefreshToken.create(
                    token = refreshToken,
                    userId = oauth.id!!,
                    expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
                )
                refreshTokenRepository.save(refreshTokenEntity)
                
                val responseBody = OAuthLoginSuccessResponse(
                    success = true,
                    message = "MANAGER 로그인 성공",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
                sendResponse(response, responseBody)
            }
            
            // 삭제된 계정들 처리
            (staffOpt.isPresent && staffOpt.get().isDeleted()) || 
            (managerOpt.isPresent && managerOpt.get().isDeleted()) -> {
                handleDeletedUser(response, "삭제된 계정입니다.")
            }
            
            // 아직 회원가입하지 않음 → 중립적인 안내 메시지
            else -> {
                val accessToken = jwtTokenProvider.createAccessToken(
                    userId = oauth.id!!,
                    userType = "PENDING", // OAuth 인증만 완료된 상태
                    status = "PENDING",
                    email = oauth.email
                )
                
                val responseBody = SimpleOAuthResponse(
                    success = true,
                    message = "OAuth 인증이 완료되었습니다. 회원가입을 진행해주세요.",
                    accessToken = accessToken
                )
                sendResponse(response, responseBody)
            }
        }
    }

    /**
     * 삭제된 사용자 처리
     */
    private fun handleDeletedUser(response: HttpServletResponse, message: String) {
        val responseBody = SimpleOAuthResponse(
            success = false,
            message = message,
            accessToken = ""
        )
        sendResponse(response, responseBody)
    }

    /**
     * 오류 처리
     */
    private fun handleError(response: HttpServletResponse, message: String) {
        val responseBody = SimpleOAuthResponse(
            success = false,
            message = message,
            accessToken = ""
        )
        sendResponse(response, responseBody)
    }

    /**
     * 응답 전송 공통 메서드
     */
    private fun sendResponse(response: HttpServletResponse, responseBody: Any) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(responseBody))
    }
} 