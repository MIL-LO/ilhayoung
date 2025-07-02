package com.millo.ilhayoung.auth.jwt

import com.millo.ilhayoung.user.domain.UserType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security에서 사용할 사용자 인증 정보를 담는 클래스
 * UserDetails 인터페이스를 구현하여 Spring Security와 연동
 */
data class UserPrincipal(
    val userId: String,
    val email: String,
    val userType: UserType?
) : UserDetails {

    /**
     * 사용자 권한 목록 반환
     * 사용자 타입에 따라 ROLE_STAFF 또는 ROLE_MANAGER 권한을 부여한다.
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<SimpleGrantedAuthority>()
        userType?.let { 
            authorities.add(SimpleGrantedAuthority("ROLE_${it.code}"))
        }
        return authorities
    }

    /**
     * 사용자 패스워드 반환 (OAuth2 사용으로 비어있음)
     */
    override fun getPassword(): String = ""

    /**
     * 사용자 이름 반환 (userId 사용)
     */
    override fun getUsername(): String = userId

    /**
     * 계정 만료 여부 (항상 true)
     */
    override fun isAccountNonExpired(): Boolean = true

    /**
     * 계정 잠금 여부 (항상 true)
     */
    override fun isAccountNonLocked(): Boolean = true

    /**
     * 자격 증명 만료 여부 (항상 true)
     */
    override fun isCredentialsNonExpired(): Boolean = true

    /**
     * 계정 활성화 여부 (항상 true)
     */
    override fun isEnabled(): Boolean = true

    /**
     * 사용자가 STAFF인지 확인
     */
    fun isStaff(): Boolean = userType == UserType.STAFF

    /**
     * 사용자가 MANAGER인지 확인
     */
    fun isManager(): Boolean = userType == UserType.MANAGER
} 