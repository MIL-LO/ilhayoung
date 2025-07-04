package com.millo.ilhayoung.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime

/**
 * 블랙리스트된 토큰 정보를 저장하는 도메인 클래스
 * Redis에서 무효화된 토큰들을 관리
 */
@RedisHash(value = "blacklisted_token", timeToLive = 86400) // 24시간 TTL
data class BlacklistedToken(
    
    /**
     * Redis Key로 사용될 ID (토큰 값)
     */
    @Id
    val token: String,
    
    /**
     * 토큰 소유자 사용자 ID
     */
    @Indexed
    val userId: String,
    
    /**
     * 토큰 타입 (ACCESS, REFRESH)
     */
    @Indexed
    val tokenType: String,
    
    /**
     * 블랙리스트 등록 시간
     */
    val blacklistedAt: LocalDateTime = LocalDateTime.now(),
    
    /**
     * 토큰 원본 만료 시간 (TTL 설정용)
     */
    val originalExpiresAt: LocalDateTime,
    
    /**
     * 블랙리스트 등록 사유
     */
    val reason: String = "LOGOUT"
    
) {
    
    companion object {
        
        /**
         * Access Token 블랙리스트 생성
         */
        fun createForAccessToken(
            token: String,
            userId: String,
            originalExpiresAt: LocalDateTime,
            reason: String = "LOGOUT"
        ): BlacklistedToken {
            return BlacklistedToken(
                token = token,
                userId = userId,
                tokenType = "ACCESS",
                originalExpiresAt = originalExpiresAt,
                reason = reason
            )
        }
        
        /**
         * Refresh Token 블랙리스트 생성
         */
        fun createForRefreshToken(
            token: String,
            userId: String,
            originalExpiresAt: LocalDateTime,
            reason: String = "LOGOUT"
        ): BlacklistedToken {
            return BlacklistedToken(
                token = token,
                userId = userId,
                tokenType = "REFRESH",
                originalExpiresAt = originalExpiresAt,
                reason = reason
            )
        }
    }
} 