package com.millo.ilhayoung.attendance.repository

import com.millo.ilhayoung.attendance.domain.AttendanceRecord
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime

interface AttendanceRecordRepository : MongoRepository<AttendanceRecord, String> {
    
    /**
     * 스케줄별 출근 기록 조회
     */
    fun findByScheduleId(scheduleId: String): AttendanceRecord?
    
    /**
     * 스태프별 출근 기록 조회
     */
    fun findByStaffId(staffId: String): List<AttendanceRecord>
    
    /**
     * 스태프별 지각 횟수 조회
     */
    @Query("{ 'staffId': ?0, 'isLate': true }")
    fun countLateRecordsByStaffId(staffId: String): Long
    
    /**
     * 스태프별 실제 근무 시간 합계 계산용 조회
     */
    @Query("{ 'staffId': ?0, 'actualStartTime': { \$ne: null }, 'actualEndTime': { \$ne: null } }")
    fun findCompletedAttendanceByStaffId(staffId: String): List<AttendanceRecord>
    
    /**
     * 스태프별 특정 날짜 범위의 출근 기록 조회 (근무시간 계산용)
     */
    @Query("{ 'staffId': ?0, 'actualStartTime': { \$gte: ?1, \$lte: ?2 } }")
    fun findByStaffIdAndDateRange(
        staffId: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<AttendanceRecord>
    
    /**
     * 특정 날짜와 상태별 출근 기록 수 조회
     */
    @Query("{ 'workDate': ?0, 'status': ?1 }")
    fun countByDateAndStatus(date: LocalDate, status: String): Long
    
    /**
     * 매니저별 특정 날짜와 상태별 출근 기록 수 조회
     */
    @Query("{ 'managerId': ?0, 'workDate': ?1, 'status': ?2 }")
    fun countByManagerIdAndDateAndStatus(managerId: String, date: LocalDate, status: String): Long
    
    /**
     * 매니저별 최근 출근 기록 조회 (상위 5개)
     */
    @Query("{ 'managerId': ?0 }")
    fun findTop5ByManagerIdOrderByCreatedAtDesc(managerId: String): List<AttendanceRecord>
    
    /**
     * 매니저별 특정 상태의 최근 기록 조회 (상위 3개)
     */
    @Query("{ 'managerId': ?0, 'status': ?1 }")
    fun findTop3ByManagerIdAndStatusOrderByUpdatedAtDesc(managerId: String, status: String): List<AttendanceRecord>
} 