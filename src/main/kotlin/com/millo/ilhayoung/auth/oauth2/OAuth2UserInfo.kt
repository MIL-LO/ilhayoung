package com.millo.ilhayoung.auth.oauth2

/**
 * OAuth2 사용자 정보 추상화 인터페이스
 */
abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract fun getId(): String
    abstract fun getName(): String
    abstract fun getEmail(): String
    abstract fun getImageUrl(): String?
}

/**
 * 구글 사용자 정보
 */
class GoogleOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {
    
    override fun getId(): String = attributes["sub"] as String
    
    override fun getName(): String = attributes["name"] as String
    
    override fun getEmail(): String = attributes["email"] as String
    
    override fun getImageUrl(): String? = attributes["picture"] as? String
}

/**
 * 카카오 사용자 정보
 */
class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {
    
    override fun getId(): String = attributes["id"].toString()
    
    override fun getName(): String {
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
        val profile = kakaoAccount?.get("profile") as? Map<String, Any>
        return profile?.get("nickname") as? String ?: ""
    }
    
    override fun getEmail(): String {
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
        return kakaoAccount?.get("email") as? String ?: ""
    }
    
    override fun getImageUrl(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
        val profile = kakaoAccount?.get("profile") as? Map<String, Any>
        return profile?.get("profile_image_url") as? String
    }
}

/**
 * 네이버 사용자 정보
 */
class NaverOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {
    
    private val response = attributes["response"] as Map<String, Any>
    
    override fun getId(): String = response["id"] as String
    
    override fun getName(): String = response["name"] as? String ?: ""
    
    override fun getEmail(): String = response["email"] as? String ?: ""
    
    override fun getImageUrl(): String? = response["profile_image"] as? String
}

/**
 * OAuth2 사용자 정보 팩토리
 */
object OAuth2UserInfoFactory {
    
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            "naver" -> NaverOAuth2UserInfo(attributes)
            else -> throw IllegalArgumentException("지원하지 않는 OAuth2 제공자: $registrationId")
        }
    }
} 