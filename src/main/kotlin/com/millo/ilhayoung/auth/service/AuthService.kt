package com.millo.ilhayoung.auth.service

import com.millo.ilhayoung.auth.domain.BlacklistedToken
import com.millo.ilhayoung.auth.domain.OAuth
import com.millo.ilhayoung.auth.domain.RefreshToken
import com.millo.ilhayoung.auth.dto.LogoutResponse
import com.millo.ilhayoung.auth.dto.SimpleOAuthResponse
import com.millo.ilhayoung.auth.dto.TokenResponse
import com.millo.ilhayoung.auth.dto.TokenValidationResponse
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.auth.repository.BlacklistedTokenRepository
import com.millo.ilhayoung.auth.repository.OAuthRepository
import com.millo.ilhayoung.auth.repository.RefreshTokenRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * OAuth 인증 서비스
 * 역할 선택은 OAuth 로그인 시 프론트엔드 파라미터로 처리
 */
@Service
@Transactional
class AuthService(
    private val oauthRepository: OAuthRepository,
    private val staffRepository: StaffRepository,
    private val managerRepository: ManagerRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val blacklistedTokenRepository: BlacklistedTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    /**
     * 회원가입 처리 (공통 로직)
     * 
     * @param oauth 인증된 OAuth 사용자
     * @return 사용자 정보 응답
     */
    fun processSignup(oauth: OAuth): SimpleOAuthResponse {
        // DB에 사용자 정보 저장
        oauthRepository.save(oauth)
        
        return SimpleOAuthResponse(
            success = true,
            message = "회원가입이 완료되었습니다.",
            accessToken = ""
        )
    }
    
    /**
     * Access Token과 Refresh Token 생성
     *
     * @param oauth OAuth 사용자 정보
     * @param userType 사용자 타입
     * @param status 사용자 상태
     * @return 토큰 응답
     */
    fun createTokens(oauth: OAuth, userType: String, status: String): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(
            userId = oauth.id!!,
            userType = userType,
            status = status,
            email = oauth.email
        )
        
        val refreshToken = jwtTokenProvider.createRefreshToken(oauth.id!!)
        val refreshTokenEntity = RefreshToken(
            userId = oauth.id!!,
            token = refreshToken,
            expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
        )
        refreshTokenRepository.save(refreshTokenEntity)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
    
    /**
     * Refresh Token으로 새로운 Access Token과 Refresh Token 생성 및 기존 토큰 무효화
     * 기존 AccessToken도 블랙리스트에 추가하여 보안 강화
     *
     * @param refreshToken 리프레시 토큰
     * @param request HTTP 요청 (Authorization 헤더에서 기존 AccessToken 추출용, 선택적)
     * @return 새로운 토큰 응답
     */
    fun refreshTokens(refreshToken: String, request: HttpServletRequest? = null): TokenResponse {
        // RefreshToken 형식 검증
        if (refreshToken.isBlank() || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // RefreshToken에서 사용자 ID 추출
        val userId = try {
            jwtTokenProvider.getUserId(refreshToken)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // Redis에서 저장된 RefreshToken과 비교 검증
        val storedTokenOpt = refreshTokenRepository.findByUserId(userId).firstOrNull()
        if (storedTokenOpt == null || storedTokenOpt.token != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // 만료 확인
        if (storedTokenOpt.expiresAt.isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            refreshTokenRepository.delete(storedTokenOpt)
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        }
        
        // 사용자 존재 확인
        val oauth = oauthRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 현재 사용자 상태 조회
        val (userType, status) = getCurrentUserStatus(oauth.id!!)
        
        // Authorization 헤더에서 기존 AccessToken 추출하여 블랙리스트에 추가 (요청이 있는 경우)
        request?.getHeader("Authorization")?.let { authHeader ->
            if (authHeader.startsWith("Bearer ")) {
                val accessToken = authHeader.substring(7)
                if (jwtTokenProvider.validateToken(accessToken) && jwtTokenProvider.isAccessToken(accessToken)) {
                    val tokenUserId = jwtTokenProvider.getUserId(accessToken)
                    if (tokenUserId == userId) {
                        blacklistToken(accessToken, userId, "TOKEN_REFRESH")
                    }
                }
            }
        }
        
        // 기존 RefreshToken 블랙리스트 추가 및 Redis에서 삭제
        blacklistToken(refreshToken, oauth.id!!, "TOKEN_REFRESH")
        refreshTokenRepository.delete(storedTokenOpt)
        
        // 새로운 AccessToken + RefreshToken 생성
        val newAccessToken = jwtTokenProvider.createAccessToken(
            userId = oauth.id!!,
            userType = userType,
            status = status,
            email = oauth.email
        )
        
        val newRefreshToken = jwtTokenProvider.createRefreshToken(oauth.id!!)
        
        // 새로운 RefreshToken을 Redis에 저장
        val newRefreshTokenEntity = RefreshToken(
            userId = oauth.id!!,
            token = newRefreshToken,
            expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(30)
        )
        refreshTokenRepository.save(newRefreshTokenEntity)
        
        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
    
    /**
     * Refresh Token으로 새로운 Access Token과 Refresh Token 생성 및 기존 토큰 무효화
     * 기존 AccessToken도 블랙리스트에 추가하여 보안 강화
     *
     * @param refreshToken 리프레시 토큰
     * @param request HTTP 요청 (Authorization 헤더에서 기존 AccessToken 추출용)
     * @return 새로운 토큰 응답
     */
    fun refreshTokensWithAccessToken(refreshToken: String, request: HttpServletRequest): TokenResponse {
        return refreshTokens(refreshToken, request)
    }
    
    /**
     * 로그아웃 처리
     *
     * @param userId 사용자 ID
     * @param request HTTP 요청 (Authorization 헤더에서 토큰 추출용)
     * @return 로그아웃 응답
     */
    fun logout(userId: String, request: HttpServletRequest): LogoutResponse {
        var invalidatedTokens = 0
        
        // 사용자 존재 확인
        oauthRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // Authorization 헤더에서 현재 Access Token 추출하여 블랙리스트에 추가
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val accessToken = authHeader.substring(7)
            
            // Access Token 유효성 검증 후 블랙리스트 추가
            if (jwtTokenProvider.validateToken(accessToken) && jwtTokenProvider.isAccessToken(accessToken)) {
                val tokenUserId = jwtTokenProvider.getUserId(accessToken)
                if (tokenUserId == userId) {
                    blacklistToken(accessToken, userId, "LOGOUT")
                    invalidatedTokens++
                }
            }
        }
        
        // 해당 사용자의 모든 RefreshToken 삭제
        val userRefreshTokens = refreshTokenRepository.findByUserId(userId)
        userRefreshTokens.forEach { tokenEntity ->
            refreshTokenRepository.delete(tokenEntity)
            invalidatedTokens++
        }
        
        return LogoutResponse(
            message = "로그아웃이 완료되었습니다.",
            invalidatedTokens = invalidatedTokens
        )
    }
    

    

    
    /**
     * 토큰 유효성 검증 (Flutter 앱용)
     * 
     * @param accessToken Access Token (선택적)
     * @param userId 사용자 ID
     * @return 토큰 유효성 및 사용자 정보
     */
    fun validateAccessToken(userId: String, accessToken: String? = null): TokenValidationResponse {
        // accessToken이 제공된 경우 토큰 검증 수행
        accessToken?.let { token ->
            // 토큰 기본 검증
            if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
                throw BusinessException(ErrorCode.INVALID_TOKEN)
            }
            
            // 토큰에서 사용자 ID 추출하여 일치 확인
            val tokenUserId = jwtTokenProvider.getUserId(token)
            if (tokenUserId != userId) {
                throw BusinessException(ErrorCode.INVALID_TOKEN)
            }
        }
        
        // 사용자 존재 확인
        val user = oauthRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 현재 상태 조회
        val (userType, status) = getCurrentUserStatus(userId)
        
        return TokenValidationResponse(
            valid = true,
            userId = user.id!!,
            email = user.email,
            userType = userType,
            status = status,
            oauthName = user.oauthName,
            authorities = listOf("ROLE_${userType}")
        )
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    fun getUserById(userId: String): OAuth {
        return oauthRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
    }
    
    /**
     * 토큰을 블랙리스트에 추가하는 헬퍼 메서드 (해시 기반)
     */
    private fun blacklistToken(token: String, userId: String, reason: String) {
        try {
            val tokenHash = jwtTokenProvider.hashToken(token)
            val tokenType = if (jwtTokenProvider.isAccessToken(token)) "ACCESS" else "REFRESH"
            val expiration = jwtTokenProvider.getExpirationAsLocalDateTime(token)
            
            val blacklistedToken = BlacklistedToken(
                token = tokenHash,
                userId = userId,
                tokenType = tokenType,
                originalExpiresAt = expiration,
                reason = reason
            )
            
            blacklistedTokenRepository.save(blacklistedToken)
            
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
    
    /**
     * 현재 사용자 상태 조회 (private 헬퍼 메서드)
     */
    private fun getCurrentUserStatus(userId: String): Pair<String, String> {
        val staffOpt = staffRepository.findByUserId(userId)
        val managerOpt = managerRepository.findByUserId(userId)
        
        return when {
            staffOpt.isPresent -> {
                val staff = staffOpt.get()
                Pair("STAFF", staff.status.code)
            }
            managerOpt.isPresent -> {
                val manager = managerOpt.get()
                Pair("MANAGER", manager.status.code)
            }
            else -> {
                // OAuth만 있고 회원가입이 안된 경우
                Pair("PENDING", "PENDING")
            }
        }
    }
}