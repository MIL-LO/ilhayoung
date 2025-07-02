package com.millo.ilhayoung.auth.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Refresh Token 정보를 저장하는 도메인 클래스
 * 사용자의 Refresh Token을 관리
 */
@Document(collection = "refresh_tokens")
data class RefreshToken(
    
    /**
     * 연결된 사용자 ID
     * User 도메인의 ID를 참조
     */
    @Indexed
    val userId: String,
    
    /**
     * Refresh Token 값
     * JWT 토큰 문자열
     */
    @Indexed(unique = true)
    val token: String,
    
    /**
     * 토큰 만료 시간
     */
    val expiresAt: LocalDateTime,
    
    /**
     * 토큰 발급 시 사용된 기기/브라우저 정보
     * User-Agent 등의 정보를 저장하여 보안을 강화
     */
    val userAgent: String? = null,
    
    /**
     * 토큰 발급 시 IP 주소
     */
    val ipAddress: String? = null,
    
    /**
     * 토큰 활성화 여부
     * false로 설정하면 해당 토큰은 무효화됨
     */
    val isActive: Boolean = true
    
) : BaseDocument() {
    
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
     * 활성화되어 있고 만료되지 않았으면 유효함
     * 
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun isValid(): Boolean {
        return isActive && !isExpired() && !isDeleted
    }
    
    /**
     * 토큰을 무효화하는 메서드
     * 
     * @return 무효화된 RefreshToken 객체
     */
    fun deactivate(): RefreshToken {
        return this.copy(isActive = false)
    }
    
    companion object {
        
        /**
         * 새로운 RefreshToken을 생성하는 팩토리 메서드
         * 
         * @param userId 사용자 ID
         * @param token 토큰 값
         * @param expiresAt 만료 시간
         * @param userAgent User-Agent 정보
         * @param ipAddress IP 주소
         * @return 새로운 RefreshToken 객체
         */
        fun create(
            userId: String,
            token: String,
            expiresAt: LocalDateTime,
            userAgent: String? = null,
            ipAddress: String? = null
        ): RefreshToken {
            return RefreshToken(
                userId = userId,
                token = token,
                expiresAt = expiresAt,
                userAgent = userAgent,
                ipAddress = ipAddress
            )
        }
    }
} 