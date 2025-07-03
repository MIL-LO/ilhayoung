package com.millo.ilhayoung.auth.oauth2

import com.millo.ilhayoung.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Spring Security OAuth2User를 구현하는 커스텀 사용자 클래스
 * User 엔티티 정보와 OAuth2 attributes를 함께 관리
 */
class CustomOAuth2User(
    private val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<SimpleGrantedAuthority>()
        user.userType?.let { 
            authorities.add(SimpleGrantedAuthority("ROLE_${it.code}"))
        }
        return authorities
    }

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getName(): String = user.email

    /**
     * User 엔티티 반환
     */
    fun getUser(): User = user

    /**
     * 사용자 ID 반환
     */
    fun getUserId(): String = user.id!!

    /**
     * 사용자가 STAFF인지 확인
     */
    fun isStaff(): Boolean = user.isStaff()

    /**
     * 사용자가 MANAGER인지 확인
     */
    fun isManager(): Boolean = user.isManager()

    /**
     * 추가 정보 입력이 필요한지 확인
     */
    fun needAdditionalInfo(): Boolean = user.needAdditionalInfo

    companion object {
        fun create(user: User, attributes: Map<String, Any>): CustomOAuth2User {
            return CustomOAuth2User(user, attributes)
        }
    }
} 