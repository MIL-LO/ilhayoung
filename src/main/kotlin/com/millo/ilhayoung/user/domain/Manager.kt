package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.auth.domain.OAuth
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 관리자(MANAGER) 정보 도메인
 */
@Document(collection = "managers")
class Manager(
    
    /**
     * OAuth 정보
     */
    var oauth: OAuth,
    
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
     * 사업지 주소
     */
    var businessAddress: String,
    
    /**
     * 사업자등록번호 (10자리)
     */
    @Indexed(unique = true)
    var businessNumber: String,
    
    /**
     * 업종 (예: 요식업, 카페, 게스트 하우스)
     */
    var businessType: String,
    
    /**
     * 사용자 타입 (고정)
     */
    var userType: UserType = UserType.MANAGER,
    
    /**
     * 사용자 상태
     * PENDING: OAuth 로그인만 완료, 회원가입 필요
     * ACTIVE: 회원가입 완료, 활성 상태
     * DELETED: 회원 탈퇴, 삭제된 상태
     */
    var status: UserStatus = UserStatus.PENDING,

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
     * 포맷팅된 사업자등록번호 반환 (xxx-xx-xxxxx)
     */
    fun getFormattedBusinessNumber(): String {
        val numbers = businessNumber.replace("-", "")
        return if (numbers.length == 10) {
            "${numbers.substring(0, 3)}-${numbers.substring(3, 5)}-${numbers.substring(5)}"
        } else {
            businessNumber
        }
    }
    
    /**
     * 포맷팅된 연락처 반환 (010-xxxx-xxxx)
     */
    fun getFormattedPhone(): String {
        val numbers = phone.replace("-", "")
        return if (numbers.length == 11) {
            "${numbers.substring(0, 3)}-${numbers.substring(3, 7)}-${numbers.substring(7)}"
        } else {
            phone
        }
    }
    
    /**
     * Manager 정보 업데이트
     */
    fun update(
        phone: String? = null,
        businessAddress: String? = null,
        businessType: String? = null
    ) {
        phone?.let { this.phone = it }
        businessAddress?.let { this.businessAddress = it }
        businessType?.let { this.businessType = it }
        this.updatedAt = LocalDateTime.now()
    }
    
    companion object {
        /**
         * OAuth와 기본 정보로 Manager 생성 (회원가입 완료)
         */
        fun create(
            oauth: OAuth,
            birthDate: String,
            phone: String,
            businessAddress: String,
            businessNumber: String,
            businessType: String
        ): Manager {
            return Manager(
                oauth = oauth,
                birthDate = birthDate,
                phone = phone,
                businessAddress = businessAddress,
                businessNumber = businessNumber,
                businessType = businessType,
                userType = UserType.MANAGER,
                status = UserStatus.ACTIVE,
                id = oauth.id
            )
        }
    }
} 