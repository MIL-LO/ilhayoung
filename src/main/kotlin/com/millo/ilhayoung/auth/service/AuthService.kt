package com.millo.ilhayoung.auth.service

import com.millo.ilhayoung.auth.domain.RefreshToken
import com.millo.ilhayoung.auth.dto.*
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.repository.RefreshTokenRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.domain.User
import com.millo.ilhayoung.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 인증 관련 비즈니스 로직을 담당하는 Service 클래스
 * OAuth2 로그인, 토큰 관리, 로그아웃 기능을 제공
 */
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,

    @Value("\${spring.security.oauth2.client.registration.google.client-id}") private val googleClientId: String,
    @Value("\${spring.security.oauth2.client.registration.google.redirect-uri}") private val googleRedirectUri: String,
    @Value("\${oauth.kakao.client-id:}") private val kakaoClientId: String,
    @Value("\${oauth.naver.client-id:}") private val naverClientId: String
) {
    
    /**
     * OAuth 로그인 URL 생성
     *
     * @param provider OAuth2 제공자 (google, kakao, naver)
     * @return 로그인 URL 응답
     */
    fun getLoginUrl(provider: String): LoginUrlResponse {
        val loginUrl = when (provider.lowercase()) {
            "google" -> "https://accounts.google.com/oauth/authorize?client_id=${googleClientId}&redirect_uri=${googleRedirectUri}&response_type=code&scope=openid email profile"
            "kakao" -> "https://kauth.kakao.com/oauth/authorize?client_id=${kakaoClientId}&redirect_uri=YOUR_REDIRECT_URI&response_type=code&scope=profile_nickname account_email"
            "naver" -> "https://nid.naver.com/oauth2.0/authorize?client_id=${naverClientId}&redirect_uri=YOUR_REDIRECT_URI&response_type=code&scope=name email"
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 OAuth2 제공자입니다: $provider")
        }
        
        return LoginUrlResponse(loginUrl)
    }
    
    /**
     * OAuth 콜백 처리 및 토큰 발급
     * 
     * @param provider OAuth2 제공자
     * @param request OAuth 콜백 요청 정보
     * @return 토큰 및 사용자 정보 응답
     */
    fun handleOAuthCallback(provider: String, request: OAuthCallbackRequest): OAuthCallbackResponse {
        // 인증 코드로 액세스 토큰 교환
        val oauthUserInfo = exchangeCodeForUserInfo(provider, request.code, request.redirectUri)
        
        // 사용자 조회 또는 생성
        val user = findOrCreateUser(provider, oauthUserInfo)
        
        // JWT 토큰 생성
        val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.userType, user.email)
        val refreshTokenValue = jwtTokenProvider.createRefreshToken(user.id!!)
        
        // Refresh Token 저장
        saveRefreshToken(user.id!!, refreshTokenValue)
        
        // 로그인 시간 업데이트
        user.updateLastLogin()
        userRepository.save(user)
        
        return OAuthCallbackResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            userType = user.userType?.code,
            needAdditionalInfo = user.needAdditionalInfo
        )
    }
    
    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * 
     * @param request 리프레시 토큰 요청
     * @return 새로운 액세스 토큰 응답
     */
    fun refreshAccessToken(request: RefreshTokenRequest): RefreshTokenResponse {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(request.refreshToken) || !jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // 저장된 리프레시 토큰 확인
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { BusinessException(ErrorCode.INVALID_TOKEN) }
        
        if (!refreshToken.isValid()) {
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        }
        
        // 사용자 조회
        val user = userRepository.findById(refreshToken.userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 새로운 액세스 토큰 생성
        val newAccessToken = jwtTokenProvider.createAccessToken(user.id!!, user.userType, user.email)
        
        return RefreshTokenResponse(newAccessToken)
    }
    
    /**
     * 로그아웃 처리
     * 
     * @param userId 사용자 ID
     * @return 로그아웃 응답
     */
    fun logout(userId: String): LogoutResponse {
        // 사용자 존재 확인
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 해당 사용자의 모든 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUserId(userId)
        
        return LogoutResponse()
    }
    
    /**
     * OAuth2 인증 코드를 사용자 정보로 교환하는 메서드
     * 
     * @param provider OAuth2 제공자
     * @param code 인증 코드
     * @param redirectUri 리다이렉트 URI
     * @return OAuth 사용자 정보
     */
    private fun exchangeCodeForUserInfo(provider: String, code: String, redirectUri: String): OAuthUserInfo {
        // TODO: OAuth2 API 호출 로직 구현
        return OAuthUserInfo(
            email = "test@example.com",
            name = "테스트 사용자",
            providerId = "test-provider-id",
            profileImageUrl = null
        )
    }
    
    /**
     * 사용자 조회 또는 생성하는 메서드
     * 
     * @param provider OAuth2 제공자
     * @param oauthUserInfo OAuth 사용자 정보
     * @return 사용자 도메인 객체
     */
    private fun findOrCreateUser(provider: String, oauthUserInfo: OAuthUserInfo): User {
        return userRepository.findByProviderAndProviderId(provider, oauthUserInfo.providerId)
            .orElseGet {
                // 새로운 사용자 생성
                val newUser = User(
                    email = oauthUserInfo.email,
                    provider = provider,
                    providerId = oauthUserInfo.providerId,
                    profileImageUrl = oauthUserInfo.profileImageUrl
                )
                userRepository.save(newUser)
            }
    }
    
    /**
     * Refresh Token을 데이터베이스에 저장하는 메서드
     * 
     * @param userId 사용자 ID
     * @param refreshTokenValue 리프레시 토큰 값
     */
    private fun saveRefreshToken(userId: String, refreshTokenValue: String) {
        // 기존 활성화된 리프레시 토큰이 있다면 무효화 (단일 세션 정책)
        val existingTokens = refreshTokenRepository.findByUserIdAndIsActive(userId, true)
        existingTokens.forEach { token ->
            refreshTokenRepository.save(token.deactivate())
        }
        
        // 새로운 리프레시 토큰 저장
        val expiresAt = jwtTokenProvider.getExpiration(refreshTokenValue)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        
        val refreshToken = RefreshToken.create(
            userId = userId,
            token = refreshTokenValue,
            expiresAt = expiresAt
        )
        
        refreshTokenRepository.save(refreshToken)
    }
}

/**
 * OAuth 사용자 정보를 담는 데이터 클래스
 */
data class OAuthUserInfo(
    val email: String,
    val name: String?,
    val providerId: String,
    val profileImageUrl: String?
) 