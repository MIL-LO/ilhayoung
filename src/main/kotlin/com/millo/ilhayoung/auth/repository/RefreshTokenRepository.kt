package com.millo.ilhayoung.auth.repository

import com.millo.ilhayoung.auth.domain.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * RefreshToken 도메인을 위한 Repository 인터페이스
 * Redis 기반 저장소 사용
 */
@Repository
interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    
    /**
     * 사용자 ID로 RefreshToken 목록 조회
     * 
     * @param userId 사용자 ID
     * @return RefreshToken 목록
     */
    fun findByUserId(userId: String): List<RefreshToken>
    
    /**
     * 사용자 ID로 모든 RefreshToken 삭제
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String)
} 