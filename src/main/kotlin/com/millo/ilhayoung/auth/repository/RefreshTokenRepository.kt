package com.millo.ilhayoung.auth.repository

import com.millo.ilhayoung.auth.domain.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * RefreshToken 도메인을 위한 Redis Repository 인터페이스
 * Redis의 TTL 기능으로 만료된 토큰은 자동 삭제됨
 */
@Repository
interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    
    /**
     * 토큰 값으로 RefreshToken을 찾는 메서드
     * Redis의 경우 ID가 token이므로 findById와 동일
     * 
     * @param token 토큰 값
     * @return RefreshToken 정보
     */
    fun findByToken(token: String): Optional<RefreshToken> = findById(token)
    
    /**
     * 사용자 ID로 RefreshToken 목록을 찾는 메서드
     * @Indexed 어노테이션으로 인덱싱된 필드 조회
     * 
     * @param userId 사용자 ID
     * @return RefreshToken 목록
     */
    fun findByUserId(userId: String): List<RefreshToken>
    
    /**
     * 사용자 ID로 모든 RefreshToken을 삭제하는 메서드
     * 로그아웃 시 해당 사용자의 모든 토큰을 무효화할 때 사용
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String)
    
    /**
     * 토큰 값으로 RefreshToken을 삭제하는 메서드
     * Redis의 경우 ID가 token이므로 deleteById와 동일
     * 
     * @param token 토큰 값
     */
    fun deleteByToken(token: String) = deleteById(token)
    
    /**
     * 토큰 존재 여부 확인
     * Redis의 경우 ID가 token이므로 existsById와 동일
     * 
     * @param token 토큰 값
     * @return 존재 여부
     */
    fun existsByToken(token: String): Boolean = existsById(token)
    
    /**
     * 사용자의 토큰 개수 확인
     * 
     * @param userId 사용자 ID
     * @return 토큰 개수
     */
    fun countByUserId(userId: String): Long
} 