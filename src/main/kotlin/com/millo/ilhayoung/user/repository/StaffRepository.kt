package com.millo.ilhayoung.user.repository

import com.millo.ilhayoung.user.domain.Staff
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Staff 정보 저장소
 */
@Repository
interface StaffRepository : MongoRepository<Staff, String> {
    
    /**
     * 전화번호로 Staff 조회
     */
    fun findByPhone(phone: String): Optional<Staff>
    
    /**
     * 전화번호로 Staff 존재 여부 확인
     */
    fun existsByPhone(phone: String): Boolean
} 