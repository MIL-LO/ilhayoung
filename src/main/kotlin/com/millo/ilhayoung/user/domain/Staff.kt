package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.auth.domain.OAuth
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 직원(STAFF) 정보 도메인
 * OAuth 정보가 내장되어 있음
 */
@Document(collection = "staffs")
class Staff(
    @Id
    var id: String,

    /**
     * 내장된 OAuth 사용자 정보
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
     * 거주 주소
     */
    var address: String,

    /**
     * 경력 또는 관련 경험
     */
    var experience: String,

    /**
     * 사용자 타입 (고정)
     */
    val userType: UserType = UserType.STAFF,

    /**
     * 사용자 상태
     */
    var status: UserStatus = UserStatus.PENDING,

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
                id = oauth.id!!,
                oauth = oauth,
                birthDate = birthDate,
                phone = phone,
                address = address,
                experience = experience,
                status = UserStatus.ACTIVE
            )
        }
    }
}