package com.millo.ilhayoung.user.repository

import com.millo.ilhayoung.user.domain.User
import com.millo.ilhayoung.user.domain.UserType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * User 도메인을 위한 Repository 인터페이스
 */
@Repository
interface UserRepository : MongoRepository<User, String> {
    
    /**
     * 이메일로 사용자를 찾는 메서드
     * 
     * @param email 이메일 주소
     * @return 사용자 정보
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 전화번호로 사용자를 찾는 메서드
     * 
     * @param phone 전화번호
     * @return 사용자 정보
     */
    fun findByPhone(phone: String): Optional<User>
    
    /**
     * 제공자와 제공자 ID로 사용자를 찾는 메서드
     * OAuth2 로그인 시 사용
     * 
     * @param provider OAuth2 제공자 (google, kakao, naver)
     * @param providerId 제공자에서의 사용자 ID
     * @return 사용자 정보
     */
    fun findByProviderAndProviderId(provider: String, providerId: String): Optional<User>
    
    /**
     * 사용자 타입으로 사용자 목록을 찾는 메서드
     * 
     * @param userType 사용자 타입
     * @return 사용자 목록
     */
    fun findByUserType(userType: UserType): List<User>
    
    /**
     * 활성화된 사용자인지 확인하는 메서드
     * 
     * @param id 사용자 ID
     * @param isActive 활성화 여부
     * @return 사용자 정보
     */
    fun findByIdAndIsActive(id: String, isActive: Boolean): Optional<User>
    
    /**
     * 이메일로 활성화된 사용자를 찾는 메서드
     * 
     * @param email 이메일 주소
     * @param isActive 활성화 여부
     * @return 사용자 정보
     */
    fun findByEmailAndIsActive(email: String, isActive: Boolean): Optional<User>
    
    /**
     * 추가 정보 입력이 필요한 사용자인지 확인하는 메서드
     * 
     * @param needAdditionalInfo 추가 정보 입력 필요 여부
     * @return 사용자 목록
     */
    fun findByNeedAdditionalInfo(needAdditionalInfo: Boolean): List<User>
    
    /**
     * 이메일 존재 여부 확인
     * 
     * @param email 이메일 주소
     * @return 존재 여부
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 전화번호 존재 여부 확인
     * 
     * @param phone 전화번호
     * @return 존재 여부
     */
    fun existsByPhone(phone: String): Boolean
} 