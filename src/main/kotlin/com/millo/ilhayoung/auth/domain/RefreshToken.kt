package com.millo.ilhayoung.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime

/**
 * Refresh Token 정보를 저장하는 도메인 클래스
 * Redis에서 사용자의 Refresh Token을 관리
 */
@RedisHash(value = "refresh_token", timeToLive = 2_592_000) // 30일 TTL
data class RefreshToken(
    
    /**
     * Redis Key로 사용될 ID (토큰 값)
     */
    @Id
    val token: String,
    
    /**
     * 연결된 사용자 ID
     * User 도메인의 ID를 참조
     */
    @Indexed
    val userId: String,
    
    /**
     * 토큰 만료 시간
     */
    val expiresAt: LocalDateTime,
    
    /**
     * 토큰 생성 시간
     */
    val createdAt: LocalDateTime = LocalDateTime.now()
    
) {
    
    /**
     * 토큰이 만료되었는지 확인하는 메서드
     * 
     * @return 만료되었으면 true, 그렇지 않으면 false
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    /**
     * 토큰이 유효한지 확인하는 메서드
     * 만료되지 않았으면 유효함
     * 
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun isValid(): Boolean {
        return !isExpired()
    }
    
    companion object {
        
        /**
         * 새로운 RefreshToken을 생성하는 팩토리 메서드
         * 
         * @param token 토큰 값 (Redis Key로 사용)
         * @param userId 사용자 ID
         * @param expiresAt 만료 시간
         * @return 새로운 RefreshToken 객체
         */
        fun create(
            token: String,
            userId: String,
            expiresAt: LocalDateTime
        ): RefreshToken {
            return RefreshToken(
                token = token,
                userId = userId,
                expiresAt = expiresAt
            )
        }
    }
} 