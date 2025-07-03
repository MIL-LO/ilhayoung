package com.millo.ilhayoung.auth.service

import com.millo.ilhayoung.auth.domain.RefreshToken
import com.millo.ilhayoung.auth.dto.*
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.repository.RefreshTokenRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.domain.User
import com.millo.ilhayoung.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 인증 관련 비즈니스 로직을 담당하는 Service 클래스 (모바일 최적화)
 * 토큰 관리, 로그아웃 기능을 제공
 */
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    
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
     * 토큰 유효성 검증 (Flutter 앱용)
     * 
     * @param accessToken Access Token
     * @param userId 사용자 ID (토큰에서 추출된 값과 비교)
     * @return 토큰 유효성 및 사용자 정보
     */
    fun validateAccessToken(accessToken: String, userId: String): TokenValidationResponse {
        // 토큰 기본 검증
        if (!jwtTokenProvider.validateToken(accessToken) || !jwtTokenProvider.isAccessToken(accessToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // 토큰에서 사용자 ID 추출하여 일치 확인
        val tokenUserId = jwtTokenProvider.getUserId(accessToken)
        if (tokenUserId != userId) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        // 사용자 존재 확인
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        return TokenValidationResponse(
            valid = true,
            userId = user.id!!,
            email = user.email,
            userType = user.userType?.code,
            needAdditionalInfo = user.needAdditionalInfo,
            oauthName = user.oauthName
        )
    }

    /**
     * 특정 디바이스/토큰만 로그아웃 (Flutter 앱용)
     * 
     * @param userId 사용자 ID
     * @param refreshToken 특정 리프레시 토큰
     * @return 로그아웃 응답
     */
    fun logoutDevice(userId: String, refreshToken: String): LogoutResponse {
        // 사용자 존재 확인
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 특정 리프레시 토큰만 삭제
        refreshTokenRepository.findByToken(refreshToken)
            .ifPresent { token ->
                if (token.userId == userId) {
                    refreshTokenRepository.deleteByToken(refreshToken)
                }
            }
        
        return LogoutResponse()
    }

    /**
     * 사용자의 모든 활성 세션 조회 (보안 강화용)
     * 
     * @param userId 사용자 ID
     * @return 활성 세션 목록
     */
    fun getActiveSessions(userId: String): List<ActiveSessionResponse> {
        val refreshTokens = refreshTokenRepository.findByUserId(userId)
        
        return refreshTokens.map { token ->
            ActiveSessionResponse(
                tokenId = token.token.takeLast(8), // 보안을 위해 마지막 8자리만
                createdAt = token.createdAt,
                expiresAt = token.expiresAt,
                userAgent = token.userAgent,
                ipAddress = token.ipAddress
            )
        }
    }

    /**
     * Refresh Token을 Redis에 저장하는 메서드 (보안 강화)
     * 
     * @param userId 사용자 ID
     * @param refreshTokenValue 리프레시 토큰 값
     * @param userAgent User-Agent 정보
     * @param ipAddress IP 주소
     */
    fun saveRefreshTokenWithDeviceInfo(
        userId: String, 
        refreshTokenValue: String,
        userAgent: String? = null,
        ipAddress: String? = null
    ) {
        // 기존 리프레시 토큰들 삭제 (단일 세션 정책)
        refreshTokenRepository.deleteByUserId(userId)
        
        // 새로운 리프레시 토큰 저장
        val expiresAt = jwtTokenProvider.getExpiration(refreshTokenValue)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        
        val refreshToken = RefreshToken.create(
            token = refreshTokenValue,
            userId = userId,
            expiresAt = expiresAt,
            userAgent = userAgent,
            ipAddress = ipAddress
        )
        
        refreshTokenRepository.save(refreshToken)
    }
    
    /**
     * 사용자 ID로 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    fun getUserById(userId: String): User {
        return userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
    }
} 