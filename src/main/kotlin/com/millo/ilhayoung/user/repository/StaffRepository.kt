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
     * 구직 상태로 Staff 목록을 찾는 메서드
     * 
     * @param isJobSeeking 구직 활성화 여부
     * @return Staff 목록
     */
    fun findByIsJobSeeking(isJobSeeking: Boolean): List<Staff>
    
    /**
     * 신뢰도 점수 범위로 Staff 목록을 찾는 메서드
     * 
     * @param minScore 최소 신뢰도 점수
     * @param maxScore 최대 신뢰도 점수
     * @return Staff 목록
     */
    fun findByTrustScoreBetween(minScore: Int, maxScore: Int): List<Staff>
    
    /**
     * 선호 업종에 해당하는 Staff 목록을 찾는 메서드
     * 
     * @param jobType 업종
     * @return Staff 목록
     */
    fun findByPreferredJobTypesContaining(jobType: String): List<Staff>
    
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