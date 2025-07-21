package com.millo.ilhayoung.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * OAuth2 인증 정보 도메인
 * 순수하게 OAuth 인증 정보만 저장
 */
@Document(collection = "oauth_users")
@TypeAlias("OAuth")
class OAuth(
    
    /**
     * 이메일 (OAuth2 제공자에서 받은 고유 식별자)
     */
    @Indexed(unique = true)
    var email: String,
    
    /**
     * OAuth2 제공자 (google, kakao, naver)
     */
    var provider: String,
    
    /**
     * OAuth2 제공자 사용자 ID
     */
    var providerId: String,
    
    /**
     * OAuth2에서 받은 사용자 이름
     * Google: name, Kakao: nickname, Naver: name
     */
    var oauthName: String
) {
    @Id
    var id: String? = null
    
    /**
     * OAuth에서 받은 이름 또는 기본값 반환
     */
    fun getDisplayName(): String {
        return oauthName ?: "사용자"
    }
    
    companion object {
        
        /**
         * OAuth2 정보로 새로운 OAuth 생성
         */
        fun createFromOAuth(
            email: String,
            provider: String,
            providerId: String,
            oauthName: String
        ): OAuth {
            return OAuth(
                email = email,
                provider = provider,
                providerId = providerId,
                oauthName = oauthName
            )
        }
    }
} 