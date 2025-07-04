package com.millo.ilhayoung.auth.oauth2

import com.millo.ilhayoung.auth.domain.OAuth
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Spring Security OAuth2User를 구현하는 커스텀 사용자 클래스
 * OAuth 엔티티 정보와 OAuth2 attributes를 함께 관리
 */
class CustomOAuth2User(
    private val user: OAuth,
    private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // OAuth는 순수한 인증 정보만 담으므로 기본 권한만 부여
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getName(): String = user.email

    /**
     * OAuth 엔티티 반환
     */
    fun getUser(): OAuth = user

    /**
     * 사용자 ID 반환
     */
    fun getUserId(): String = user.id!!

    /**
     * 이메일 반환
     */
    val email: String = user.email

    /**
     * OAuth 제공자 반환
     */
    val provider: String = user.provider

    /**
     * OAuth 제공자 ID 반환
     */
    val providerId: String = user.providerId

    /**
     * OAuth 이름 반환
     */
    val displayName: String = user.getDisplayName()

    companion object {
        fun create(user: OAuth, attributes: Map<String, Any>): CustomOAuth2User {
            return CustomOAuth2User(user, attributes)
        }
    }
} 