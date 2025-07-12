package com.millo.ilhayoung.attendance.repository

import com.millo.ilhayoung.attendance.domain.AttendanceRecord
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDate

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
} 