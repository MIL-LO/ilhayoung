package com.millo.ilhayoung.user.repository

import com.millo.ilhayoung.user.domain.Manager
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Manager 정보 저장소
 */
@Repository
interface ManagerRepository : MongoRepository<Manager, String> {
    
    /**
     * 전화번호로 Manager 조회
     */
    fun findByPhone(phone: String): Optional<Manager>
    
    /**
     * 사업자등록번호로 Manager 조회
     */
    fun findByBusinessNumber(businessNumber: String): Optional<Manager>
    
    /**
     * 전화번호로 Manager 존재 여부 확인
     */
    fun existsByPhone(phone: String): Boolean
    
    /**
     * 사업자등록번호로 Manager 존재 여부 확인
     */
    fun existsByBusinessNumber(businessNumber: String): Boolean
} 