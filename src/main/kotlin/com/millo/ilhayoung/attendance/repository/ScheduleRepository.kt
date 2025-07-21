package com.millo.ilhayoung.attendance.repository

import com.millo.ilhayoung.attendance.domain.Schedule
import com.millo.ilhayoung.attendance.domain.WorkStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime

interface ScheduleRepository : MongoRepository<Schedule, String> {
    
    /**
     * 스태프별 월별 스케줄 조회
     */
    fun findByStaffIdAndWorkDateBetween(
        staffId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Schedule>
    
    /**
     * 매니저별 월별 스케줄 조회 (관리하는 모든 스케줄)
     */
    fun findByManagerIdAndWorkDateBetween(
        managerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Schedule>
    
    /**
     * 특정 지원서의 모든 스케줄 조회
     */
    fun findByApplicationId(applicationId: String): List<Schedule>
    
    /**
     * 스태프별 특정 상태의 스케줄 개수 조회
     */
    fun countByStaffIdAndStatus(staffId: String, status: WorkStatus): Long
    
    /**
     * 스태프별 완료된 근무일 수 조회
     */
    @Query("{ 'staffId': ?0, 'status': 'COMPLETED' }")
    fun countCompletedWorkDaysByStaffId(staffId: String): Long
    
    /**
     * 스태프별 예정된 스케줄 조회
     */
    fun findByStaffIdAndStatusInOrderByWorkDateAsc(
        staffId: String,
        statuses: List<WorkStatus>
    ): List<Schedule>
    
    /**
     * 특정 날짜의 매니저 관할 스케줄 조회
     */
    fun findByManagerIdAndWorkDate(managerId: String, workDate: LocalDate): List<Schedule>
    
    /**
     * 스태프별 특정 날짜 스케줄 조회
     */
    fun findByStaffIdAndWorkDate(staffId: String, workDate: LocalDate): List<Schedule>
    
    /**
     * 특정 날짜의 모든 스케줄 조회 (전체 근로자 현황 조회용)
     */
    fun findByWorkDate(workDate: LocalDate): List<Schedule>

    /**
     * 특정 시간 이전에 종료되었어야 하지만 여전히 SCHEDULED 상태인 스케줄 조회
     */
    fun findByStatusAndEndDateTimeBefore(status: WorkStatus, dateTime: LocalDateTime): List<Schedule>
    
    /**
     * endDateTime이 null인 특정 상태의 스케줄 조회
     */
    fun findByStatusAndEndDateTimeIsNull(status: WorkStatus): List<Schedule>
    
    /**
     * 결근 상태인 스케줄 조회 (대체 근무자 찾기용)
     */
    fun findByStatusAndWorkDateAfter(status: WorkStatus, workDate: LocalDate): List<Schedule>
} 