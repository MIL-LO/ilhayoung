package com.millo.ilhayoung.auth.repository

import com.millo.ilhayoung.auth.domain.OAuth
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * OAuth 도메인을 위한 Repository 인터페이스
 * 순수한 OAuth 인증 정보만 관리
 */
@Repository
interface OAuthRepository : MongoRepository<OAuth, String> {
    
    /**
     * 이메일로 OAuth 사용자를 찾는 메서드
     * 
     * @param email 이메일 주소
     * @return OAuth 사용자 정보
     */
    fun findByEmail(email: String): Optional<OAuth>
    
    /**
     * 제공자와 제공자 ID로 OAuth 사용자를 찾는 메서드
     * OAuth2 로그인 시 사용
     * 
     * @param provider OAuth2 제공자 (google, kakao, naver)
     * @param providerId 제공자에서의 사용자 ID
     * @return OAuth 사용자 정보
     */
    fun findByProviderAndProviderId(provider: String, providerId: String): Optional<OAuth>
    
    /**
     * 이메일 존재 여부 확인
     * 
     * @param email 이메일 주소
     * @return 존재 여부
     */
    fun existsByEmail(email: String): Boolean
} 