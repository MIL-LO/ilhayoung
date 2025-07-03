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
     * 업종으로 Manager 목록을 찾는 메서드
     * 
     * @param businessType 업종
     * @return Manager 목록
     */
    fun findByBusinessType(businessType: String): List<Manager>
    
    /**
     * 모집 활성화 상태로 Manager 목록을 찾는 메서드
     * 
     * @param isRecruitingActive 모집 활성화 여부
     * @return Manager 목록
     */
    fun findByIsRecruitingActive(isRecruitingActive: Boolean): List<Manager>
    
    /**
     * 사업자등록번호 검증 상태로 Manager 목록을 찾는 메서드
     * 
     * @param isBusinessNumberVerified 사업자등록번호 검증 상태
     * @return Manager 목록
     */
    fun findByIsBusinessNumberVerified(isBusinessNumberVerified: Boolean): List<Manager>
    
    /**
     * 신뢰도 점수 범위로 Manager 목록을 찾는 메서드
     * 
     * @param minScore 최소 신뢰도 점수
     * @param maxScore 최대 신뢰도 점수
     * @return Manager 목록
     */
    fun findByTrustScoreBetween(minScore: Double, maxScore: Double): List<Manager>
    
    /**
     * 사업자등록번호 존재 여부 확인
     * 
     * @param businessNumber 사업자등록번호
     * @return 존재 여부
     */
    fun existsByBusinessNumber(businessNumber: String): Boolean
    
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