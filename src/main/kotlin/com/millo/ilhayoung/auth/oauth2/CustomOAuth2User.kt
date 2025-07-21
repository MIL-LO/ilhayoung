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

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getName(): String = user.email

    // Convenience properties
    val email: String = user.email
    val provider: String = user.provider
    val providerId: String = user.providerId
    val displayName: String = user.getDisplayName()

    companion object {
        fun create(user: OAuth, attributes: Map<String, Any>): CustomOAuth2User {
            return CustomOAuth2User(user, attributes)
        }
    }
} 