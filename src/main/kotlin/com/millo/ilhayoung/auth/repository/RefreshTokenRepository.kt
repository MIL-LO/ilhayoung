package com.millo.ilhayoung.auth.repository

import com.millo.ilhayoung.auth.domain.RefreshToken
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * RefreshToken 도메인을 위한 Repository 인터페이스
 */
@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
    
    /**
     * 토큰 값으로 RefreshToken을 찾는 메서드
     * 
     * @param token 토큰 값
     * @return RefreshToken 정보
     */
    fun findByToken(token: String): Optional<RefreshToken>
    
    /**
     * 사용자 ID로 RefreshToken 목록을 찾는 메서드
     * 
     * @param userId 사용자 ID
     * @return RefreshToken 목록
     */
    fun findByUserId(userId: String): List<RefreshToken>
    
    /**
     * 사용자 ID와 활성화 상태로 RefreshToken 목록을 찾는 메서드
     * 
     * @param userId 사용자 ID
     * @param isActive 활성화 여부
     * @return RefreshToken 목록
     */
    fun findByUserIdAndIsActive(userId: String, isActive: Boolean): List<RefreshToken>
    
    /**
     * 만료된 토큰들을 찾는 메서드
     * 
     * @param now 현재 시간
     * @return 만료된 RefreshToken 목록
     */
    fun findByExpiresAtBefore(now: LocalDateTime): List<RefreshToken>
    
    /**
     * 사용자 ID로 모든 RefreshToken을 삭제하는 메서드
     * 로그아웃 시 해당 사용자의 모든 토큰을 무효화할 때 사용
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String)
    
    /**
     * 토큰 값으로 RefreshToken을 삭제하는 메서드
     * 
     * @param token 토큰 값
     */
    fun deleteByToken(token: String)
    
    /**
     * 만료된 토큰들을 삭제하는 메서드
     * 스케줄러에서 주기적으로 만료된 토큰을 정리할 때 사용
     * 
     * @param now 현재 시간
     */
    fun deleteByExpiresAtBefore(now: LocalDateTime)
    
    /**
     * 토큰 존재 여부 확인
     * 
     * @param token 토큰 값
     * @return 존재 여부
     */
    fun existsByToken(token: String): Boolean
    
    /**
     * 사용자의 활성화된 토큰 개수 확인
     * 
     * @param userId 사용자 ID
     * @param isActive 활성화 여부
     * @return 토큰 개수
     */
    fun countByUserIdAndIsActive(userId: String, isActive: Boolean): Long
} 