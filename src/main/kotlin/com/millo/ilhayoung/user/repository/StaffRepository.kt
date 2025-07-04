package com.millo.ilhayoung.user.repository

import com.millo.ilhayoung.user.domain.Staff
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Staff 도메인을 위한 Repository 인터페이스
 */
@Repository
interface StaffRepository : MongoRepository<Staff, String> {
    
    /**
     * 사용자 ID로 Staff 정보를 찾는 메서드
     * 
     * @param userId 사용자 ID
     * @return Staff 정보
     */
    fun findByUserId(userId: String): Optional<Staff>
    
    /**
     * 전화번호 존재 여부 확인
     * 
     * @param phone 전화번호
     * @return 존재 여부
     */
    fun existsByPhone(phone: String): Boolean
    
    /**
     * 사용자 ID로 Staff 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    fun existsByUserId(userId: String): Boolean
    
    /**
     * 사용자 ID로 Staff 삭제
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String)
} 