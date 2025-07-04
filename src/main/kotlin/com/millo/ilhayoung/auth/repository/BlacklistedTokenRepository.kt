package com.millo.ilhayoung.auth.repository

import com.millo.ilhayoung.auth.domain.BlacklistedToken
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * BlacklistedToken 도메인을 위한 Redis Repository 구현체
 * Redis Template을 직접 사용하여 블랙리스트된 토큰들을 관리
 */
@Repository
class BlacklistedTokenRepository(
    private val blacklistedTokenRedisTemplate: RedisTemplate<String, BlacklistedToken>,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    companion object {
        private const val BLACKLIST_PREFIX = "blacklisted_token:"
        private const val USER_PREFIX = "blacklisted_token:userId:"
        private const val TYPE_PREFIX = "blacklisted_token:tokenType:"
    }
    
    /**
     * 토큰을 블랙리스트에 저장
     * 
     * @param blacklistedToken 블랙리스트할 토큰 정보
     * @return 저장된 토큰 정보
     */
    fun save(blacklistedToken: BlacklistedToken): BlacklistedToken {
        val tokenKey = "$BLACKLIST_PREFIX${blacklistedToken.token}"
        val userKey = "$USER_PREFIX${blacklistedToken.userId}"
        val typeKey = "$TYPE_PREFIX${blacklistedToken.tokenType}"
        
        // 토큰 자체를 저장
        blacklistedTokenRedisTemplate.opsForValue().set(tokenKey, blacklistedToken)
        
        // 사용자별 인덱스에 추가
        redisTemplate.opsForSet().add(userKey, blacklistedToken.token)
        
        // 타입별 인덱스에 추가
        redisTemplate.opsForSet().add(typeKey, blacklistedToken.token)
        
        // 만료 시간 설정 (토큰의 원래 만료 시간까지)
        val expiration = blacklistedToken.originalExpiresAt
        if (expiration != null) {
            val now = java.time.LocalDateTime.now()
            val ttl = java.time.Duration.between(now, expiration).toSeconds()
            if (ttl > 0) {
                blacklistedTokenRedisTemplate.expire(tokenKey, ttl, TimeUnit.SECONDS)
                redisTemplate.expire(userKey, ttl, TimeUnit.SECONDS)
                redisTemplate.expire(typeKey, ttl, TimeUnit.SECONDS)
            }
        }
        
        return blacklistedToken
    }
    
    /**
     * 토큰 값으로 블랙리스트 여부 확인
     * 
     * @param token 토큰 값
     * @return 블랙리스트 존재 여부
     */
    fun existsByToken(token: String): Boolean {
        val tokenKey = "$BLACKLIST_PREFIX$token"
        return blacklistedTokenRedisTemplate.hasKey(tokenKey) == true
    }
    
    /**
     * 사용자 ID로 블랙리스트된 토큰 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 블랙리스트된 토큰 목록
     */
    fun findByUserId(userId: String): List<BlacklistedToken> {
        val userKey = "$USER_PREFIX$userId"
        val tokenHashes = redisTemplate.opsForSet().members(userKey) ?: emptySet()
        
        return tokenHashes.mapNotNull { tokenHash ->
            val tokenKey = "$BLACKLIST_PREFIX$tokenHash"
            blacklistedTokenRedisTemplate.opsForValue().get(tokenKey)
        }
    }
    
    /**
     * 사용자 ID로 모든 블랙리스트 토큰 삭제
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String) {
        val userKey = "$USER_PREFIX$userId"
        val tokenHashes = redisTemplate.opsForSet().members(userKey) ?: emptySet()
        
        tokenHashes.forEach { tokenHash ->
            val tokenKey = "$BLACKLIST_PREFIX$tokenHash"
            blacklistedTokenRedisTemplate.delete(tokenKey)
        }
        
        // 인덱스에서도 제거
        redisTemplate.delete(userKey)
    }
    
    /**
     * 특정 사용자의 특정 타입 토큰들을 모두 블랙리스트에서 삭제
     * 
     * @param userId 사용자 ID
     * @param tokenType 토큰 타입
     */
    fun deleteByUserIdAndTokenType(userId: String, tokenType: String) {
        val userKey = "$USER_PREFIX$userId"
        val typeKey = "$TYPE_PREFIX$tokenType"
        
        val userTokens = redisTemplate.opsForSet().members(userKey) ?: emptySet()
        val typeTokens = redisTemplate.opsForSet().members(typeKey) ?: emptySet()
        
        val commonTokens = userTokens.intersect(typeTokens)
        
        commonTokens.forEach { tokenHash ->
            val tokenKey = "$BLACKLIST_PREFIX$tokenHash"
            blacklistedTokenRedisTemplate.delete(tokenKey)
        }
        
        // 인덱스에서도 제거
        redisTemplate.opsForSet().remove(userKey, *commonTokens.toTypedArray())
        redisTemplate.opsForSet().remove(typeKey, *commonTokens.toTypedArray())
    }
    
    /**
     * 전체 블랙리스트 토큰 개수 조회
     * 
     * @return 블랙리스트된 토큰 개수
     */
    fun count(): Long {
        val pattern = "$BLACKLIST_PREFIX*"
        val keys = redisTemplate.keys(pattern) ?: emptySet()
        return keys.size.toLong()
    }
} 