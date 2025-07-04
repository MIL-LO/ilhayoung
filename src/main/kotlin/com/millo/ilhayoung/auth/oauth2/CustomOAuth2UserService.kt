package com.millo.ilhayoung.auth.oauth2

import com.millo.ilhayoung.auth.domain.OAuth
import com.millo.ilhayoung.auth.repository.OAuthRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * Spring Security OAuth2 사용자 정보 처리 서비스
 * OAuth2 로그인 완료 후 사용자 정보를 가져와서 User 엔티티와 연동
 */
@Service
class CustomOAuth2UserService : DefaultOAuth2UserService() {
    
    @Autowired
    private lateinit var oauthRepository: OAuthRepository

    /**
     * OAuth2 사용자 정보를 로드하고 User 엔티티와 연동
     */
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        // 기본 OAuth2UserService를 통해 사용자 정보 조회
        val oAuth2User = super.loadUser(userRequest)
        
        // OAuth2 제공자 정보
        val registrationId = userRequest.clientRegistration.registrationId
        
        // 사용자 정보 추출
        val oAuthUserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.attributes)
        
        // 사용자 조회 또는 생성
        val user = findOrCreateUser(oAuthUserInfo, registrationId)
        
        // 커스텀 OAuth2User 반환
        return CustomOAuth2User.create(user, oAuth2User.attributes)
    }

    /**
     * OAuth2 사용자 정보로 User 찾기 또는 생성
     */
    private fun findOrCreateUser(oAuthUserInfo: OAuth2UserInfo, provider: String): OAuth {
        // 이메일로 기존 사용자 조회
        val existingUser = oauthRepository.findByEmail(oAuthUserInfo.getEmail())
        
        return if (existingUser.isPresent) {
            // 기존 사용자 반환 (OAuth 이름 업데이트 로직 추가)
            val user = existingUser.get()
            
            // OAuth 이름이 없거나 변경된 경우 업데이트
            val oauthName = oAuthUserInfo.getName()
            if (user.oauthName != oauthName) {
                user.oauthName = oauthName
                oauthRepository.save(user)
                user
            } else {
                // 기존 사용자 그대로 반환
                user
            }
        } else {
            // OAuth 이름 가져오기
            val oauthName = oAuthUserInfo.getName()
            
            // 새로운 사용자 생성
            val newUser = OAuth.createFromOAuth(
                email = oAuthUserInfo.getEmail(),
                provider = provider,
                providerId = oAuthUserInfo.getId(),
                oauthName = oauthName
            )
            oauthRepository.save(newUser)
        }
    }
} 