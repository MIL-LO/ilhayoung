package com.millo.ilhayoung.salary.repository

import com.millo.ilhayoung.salary.domain.PayrollRecord
import com.millo.ilhayoung.salary.domain.PayrollStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.YearMonth

interface PayrollRecordRepository : MongoRepository<PayrollRecord, String> {
    
    /**
     * 스태프별 급여 이력 조회
     */
    fun findByStaffIdOrderByPayrollPeriodDesc(staffId: String): List<PayrollRecord>
    
    /**
     * 스태프별 특정 기간 급여 조회
     */
    fun findByStaffIdAndPayrollPeriod(staffId: String, payrollPeriod: YearMonth): PayrollRecord?
    
    /**
     * 매니저별 급여 기록 조회
     */
    fun findByManagerIdOrderByPayrollPeriodDesc(managerId: String): List<PayrollRecord>
    
    /**
     * 특정 기간의 급여 기록 조회
     */
    fun findByPayrollPeriod(payrollPeriod: YearMonth): List<PayrollRecord>
    
    /**
     * 특정 상태의 급여 기록 조회
     */
    fun findByStatus(status: PayrollStatus): List<PayrollRecord>
    
    /**
     * 매니저별 특정 상태의 급여 기록 조회
     */
    fun findByManagerIdAndStatus(managerId: String, status: PayrollStatus): List<PayrollRecord>
    
    /**
     * 스태프별 최근 급여 기록 조회
     */
    @Query("{ 'staffId': ?0 }")
    fun findLatestByStaffId(staffId: String): List<PayrollRecord>
    
    /**
     * 매니저별 특정 기간 급여 기록 조회
     */
    fun findByManagerIdAndPayrollPeriod(managerId: String, payrollPeriod: YearMonth): List<PayrollRecord>
} 