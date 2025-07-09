package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.auth.domain.OAuth
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 직원(STAFF) 정보 도메인
 * OAuth 정보와 조인하여 사용
 */
@Document(collection = "staffs")
class Staff(
    
    /**
     * 연결된 OAuth 사용자 정보
     */
    @DBRef
    @Indexed(unique = true)
    var oauth: OAuth,
    
    /**
     * 연결된 사용자 ID (OAuth 도메인 참조)
     */
    @Indexed(unique = true)
    var userId: String,
    
    /**
     * 사용자 타입 (고정)
     */
    var userType: UserType = UserType.STAFF,
    
    /**
     * 사용자 상태
     * PENDING: OAuth 로그인만 완료, 회원가입 필요
     * ACTIVE: 회원가입 완료, 활성 상태
     * DELETED: 회원 탈퇴, 삭제된 상태
     */
    var status: UserStatus = UserStatus.PENDING,
    
    /**
     * 생년월일 (YYYY-MM-DD)
     */
    var birthDate: String,
    
    /**
     * 연락처 (010-XXXX-XXXX)
     */
    @Indexed(unique = true)
    var phone: String,
    
    /**
     * 거주 주소
     */
    var address: String,
    
    /**
     * 경력 또는 관련 경험
     * 예: "한식 주점 홀 아르바이트 3개월"
     */
    var experience: String,

    @Id
    var id: String? = null,
    
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    var updatedAt: LocalDateTime = LocalDateTime.now()
    
) : User {
    
    override fun getEmail(): String = oauth.email
    
    override fun getProvider(): String = oauth.provider
    
    override fun getProviderId(): String = oauth.providerId
    
    override fun getName(): String = oauth.getDisplayName()
    
    override fun isActive(): Boolean = status == UserStatus.ACTIVE
    
    override fun isPending(): Boolean = status == UserStatus.PENDING
    
    override fun isDeleted(): Boolean = status == UserStatus.DELETED
    
    override fun completeSignup() {
        this.status = UserStatus.ACTIVE
        this.updatedAt = LocalDateTime.now()
    }
    
    override fun delete() {
        this.status = UserStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }
    
    override fun restore() {
        this.status = UserStatus.ACTIVE
        this.updatedAt = LocalDateTime.now()
    }
    
    /**
     * 포맷팅된 전화번호 반환
     * 
     * @return xxx-xxxx-xxxx 형식의 전화번호
     */
    fun getFormattedPhone(): String {
        return phone.replace(Regex("(\\d{3})(\\d{4})(\\d{4})"), "$1-$2-$3")
    }
    
    /**
     * 나이 계산
     * 
     * @return 만 나이
     */
    fun getAge(): Int? {
        return try {
            val birth = java.time.LocalDate.parse(birthDate)
            java.time.Period.between(birth, java.time.LocalDate.now()).years
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Staff 정보 업데이트
     */
    fun update(
        phone: String? = null,
        address: String? = null,
        experience: String? = null
    ) {
        phone?.let { this.phone = it }
        address?.let { this.address = it }
        experience?.let { this.experience = it }
        this.updatedAt = LocalDateTime.now()
    }
    
    companion object {
        /**
         * OAuth와 기본 정보로 Staff 생성 (회원가입 완료)
         */
        fun create(
            oauth: OAuth,
            birthDate: String,
            phone: String,
            address: String,
            experience: String
        ): Staff {
            return Staff(
                oauth = oauth,
                userId = oauth.id!!,
                userType = UserType.STAFF,
                status = UserStatus.ACTIVE,
                birthDate = birthDate,
                phone = phone,
                address = address,
                experience = experience
            )
        }
    }
} 