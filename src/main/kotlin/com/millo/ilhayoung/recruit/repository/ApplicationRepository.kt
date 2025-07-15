package com.millo.ilhayoung.recruit.repository

import com.millo.ilhayoung.recruit.domain.Application
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * 지원 리포지토리
 */
@Repository
interface ApplicationRepository : MongoRepository<Application, String> {

    /**
     * 특정 공고의 지원자 목록 조회
     */
    fun findByRecruitIdOrderByCreatedAtDesc(recruitId: String, pageable: Pageable): Page<Application>

    /**
     * 특정 Staff의 지원 내역 조회
     */
    fun findByStaffIdOrderByCreatedAtDesc(staffId: String, pageable: Pageable): Page<Application>

    /**
     * 특정 공고에 특정 Staff가 지원했는지 확인
     */
    fun existsByRecruitIdAndStaffId(recruitId: String, staffId: String): Boolean

    /**
     * 특정 공고의 지원자 수 조회
     */
    fun countByRecruitId(recruitId: String): Long

    /**
     * 특정 공고의 상태별 지원자 수 조회
     */
    fun countByRecruitIdAndStatus(recruitId: String, status: ApplicationStatus): Long

    /**
     * Manager의 모든 공고에 대한 지원 내역 조회
     */
    @Query("{ 'recruitId': { '\$in': ?0 } }")
    fun findByRecruitIds(recruitIds: List<String>, pageable: Pageable): Page<Application>

    /**
     * 특정 공고들의 상태별 지원자 수 조회
     */
    fun findByRecruitIdIn(recruitIds: List<String>): List<Application>

    /**
     * 특정 공고의 지원자 목록 (상태별 필터)
     */
    fun findByRecruitIdAndStatusOrderByCreatedAtDesc(recruitId: String, status: ApplicationStatus, pageable: Pageable): Page<Application>

    /**
     * Staff의 특정 상태 지원 내역
     */
    fun findByStaffIdAndStatusOrderByCreatedAtDesc(staffId: String, status: ApplicationStatus, pageable: Pageable): Page<Application>

    /**
     * 상태별 지원서 수 조회
     */
    fun countByStatus(status: ApplicationStatus): Long
    
    /**
     * Manager별 특정 상태의 지원서 수 조회
     */
    fun countByRecruitManagerIdAndStatus(managerId: String, status: ApplicationStatus): Long
    
    /**
     * 매니저별 최근 지원 기록 조회 (상위 3개)
     */
    fun findTop3ByRecruitManagerIdOrderByCreatedAtDesc(managerId: String): List<Application>
    
    /**
     * 특정 상태의 모든 지원서 조회
     */
    fun findByStatus(status: ApplicationStatus): List<Application>
} 