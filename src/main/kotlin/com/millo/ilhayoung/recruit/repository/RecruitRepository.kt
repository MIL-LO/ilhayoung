package com.millo.ilhayoung.recruit.repository

import com.millo.ilhayoung.recruit.domain.Recruit
import com.millo.ilhayoung.recruit.domain.RecruitStatus
import com.millo.ilhayoung.recruit.domain.WorkPeriod
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * 채용공고 리포지토리
 */
@Repository
interface RecruitRepository : MongoRepository<Recruit, String> {

    /**
     * Manager ID로 공고 목록 조회
     */
    fun findByManagerIdAndStatusNotOrderByCreatedAtDesc(managerId: String, status: RecruitStatus, pageable: Pageable): Page<Recruit>

    /**
     * Manager ID로 특정 상태의 공고 목록 조회
     */
    fun findByManagerIdAndStatusIn(managerId: String, statuses: Collection<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 활성화된 공고 목록 조회 (상태별 필터)
     */
    fun findByStatusInOrderByCreatedAtDesc(statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 키워드 검색 (제목에서 검색)
     */
    fun findByTitleContainingIgnoreCaseAndStatusIn(keyword: String, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 지역별 검색
     */
    fun findByWorkLocationContainingIgnoreCaseAndStatusIn(location: String, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 근무기간별 검색
     */
    fun findByWorkScheduleWorkPeriodAndStatusIn(workPeriod: WorkPeriod, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 급여 범위로 검색
     */
    fun findBySalaryBetweenAndStatusIn(minSalary: Long, maxSalary: Long, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 직무별 검색
     */
    fun findByJobTypeContainingIgnoreCaseAndStatusIn(jobType: String, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 인기 공고 조회 (조회수 높은 순)
     */
    fun findByStatusInOrderByViewCountDescCreatedAtDesc(statuses: Collection<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * 마감 임박 공고 조회
     */
    fun findByDeadlineBetweenAndStatusIn(startDate: java.time.LocalDateTime, endDate: java.time.LocalDateTime, statuses: List<RecruitStatus>, pageable: Pageable): Page<Recruit>

    /**
     * Manager의 공고 수 조회
     */
    fun countByManagerIdAndStatus(managerId: String, status: RecruitStatus): Long
}