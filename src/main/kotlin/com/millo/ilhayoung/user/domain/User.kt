package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 * 사용자 정보를 담는 기본 도메인 클래스
 * OAuth2를 통한 기본 사용자 정보와 추가 정보를 포함
 */
@Document(collection = "users")
data class User(
    
    /**
     * 사용자 실명
     * 회원가입 시 입력받는 실제 이름
     */
    val name: String? = null,
    
    /**
     * 이메일 주소
     * OAuth2 로그인을 통해 받아오는 이메일
     * 시스템 내에서 고유한 식별자 역할
     */
    @Indexed(unique = true)
    val email: String,
    
    /**
     * 생년월일
     * YYYY-MM-DD 형식
     */
    val birthDate: LocalDate? = null,
    
    /**
     * 연락처 (휴대전화 번호)
     * 010-1234-5678 형식
     */
    @Indexed(unique = true, sparse = true)
    val phone: String? = null,
    
    /**
     * 사용자 타입
     * STAFF(직원) 또는 MANAGER(관리자)
     */
    val userType: UserType? = null,
    
    /**
     * OAuth2 제공자
     * google, kakao, naver 중 하나
     */
    val provider: String,
    
    /**
     * OAuth2 제공자에서의 고유 식별자
     * 각 OAuth2 제공자에서 제공하는 사용자 ID
     */
    val providerId: String,
    
    /**
     * 프로필 이미지 URL
     * OAuth2를 통해 받아온 프로필 이미지 또는 기본 이미지
     */
    val profileImageUrl: String? = null,
    
    /**
     * 계정 활성화 여부
     * true: 활성화, false: 비활성화
     */
    val isActive: Boolean = true,
    
    /**
     * 추가 정보 입력 필요 여부
     * OAuth2 인증 후 추가 정보 입력이 완료되었는지 확인
     * true: 추가 정보 입력 필요, false: 회원가입 완료
     */
    var needAdditionalInfo: Boolean = true,
    
    /**
     * 마지막 로그인 일시
     * 사용자의 활동성 파악을 위한 필드
     */
    var lastLoginAt: LocalDate? = null
    
) : BaseDocument() {
    
    /**
     * 사용자의 나이를 계산하는 메서드
     * 
     * @return 만 나이, 생년월일이 없으면 null
     */
    fun getAge(): Int? {
        return birthDate?.let { 
            java.time.Period.between(it, LocalDate.now()).years 
        }
    }
    
    /**
     * 포맷팅된 전화번호를 반환하는 메서드
     * 
     * @return xxx-xxxx-xxxx 형식의 전화번호
     */
    fun getFormattedPhone(): String? {
        return phone?.replace(Regex("(\\d{3})(\\d{4})(\\d{4})"), "$1-$2-$3")
    }
    
    /**
     * 사용자가 Manager인지 확인하는 메서드
     * 
     * @return Manager이면 true, 그렇지 않으면 false
     */
    fun isManager(): Boolean = userType == UserType.MANAGER
    
    /**
     * 사용자가 Staff인지 확인하는 메서드
     * 
     * @return Staff이면 true, 그렇지 않으면 false
     */
    fun isStaff(): Boolean = userType == UserType.STAFF
    
    /**
     * 회원가입을 완료 처리하는 메서드
     */
    fun completeSignup() {
        needAdditionalInfo = false
    }
    
    /**
     * 로그인 일시를 업데이트하는 메서드
     */
    fun updateLastLogin() {
        lastLoginAt = LocalDate.now()
    }
} 