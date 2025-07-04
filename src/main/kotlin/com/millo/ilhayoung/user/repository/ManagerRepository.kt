package com.millo.ilhayoung.user.repository

import com.millo.ilhayoung.user.domain.Manager
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Manager 도메인을 위한 Repository 인터페이스
 */
@Repository
interface ManagerRepository : MongoRepository<Manager, String> {
    
    /**
     * 사용자 ID로 Manager 정보를 찾는 메서드
     * 
     * @param userId 사용자 ID
     * @return Manager 정보
     */
    fun findByUserId(userId: String): Optional<Manager>
    
    /**
     * 사업자등록번호로 Manager 정보를 찾는 메서드
     * 
     * @param businessNumber 사업자등록번호
     * @return Manager 정보
     */
    fun findByBusinessNumber(businessNumber: String): Optional<Manager>
    
    /**
     * 사업자등록번호 존재 여부 확인
     * 
     * @param businessNumber 사업자등록번호
     * @return 존재 여부
     */
    fun existsByBusinessNumber(businessNumber: String): Boolean
    
    /**
     * 전화번호 존재 여부 확인
     * 
     * @param phone 전화번호
     * @return 존재 여부
     */
    fun existsByPhone(phone: String): Boolean
    
    /**
     * 사용자 ID로 Manager 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    fun existsByUserId(userId: String): Boolean
    
    /**
     * 사용자 ID로 Manager 삭제
     * 
     * @param userId 사용자 ID
     */
    fun deleteByUserId(userId: String)
} 