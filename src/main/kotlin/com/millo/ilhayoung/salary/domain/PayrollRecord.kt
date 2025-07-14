package com.millo.ilhayoung.salary.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.YearMonth

/**
 * 급여 기록 도메인
 */
@Document(collection = "payroll_records")
@CompoundIndexes(
    CompoundIndex(
        name = "staff_period_idx",
        def = "{'staffId': 1, 'payrollPeriod': 1}",
        unique = true
    )
)
data class PayrollRecord(
    val staffId: String,                  // 스태프 ID
    val managerId: String,                // 매니저 ID
    val payrollPeriod: YearMonth,         // 급여 정산 기간 (년월)
    val companyName: String,              // 회사명
    val position: String,                 // 직책
    val jobType: String,                  // 직무
    val salaryType: SalaryType,           // 급여 유형
    val baseAmount: Long,                 // 기본 급여액
    val totalWorkDays: Int,               // 총 근무일 수
    val actualWorkDays: Int,              // 실제 근무일 수
    val totalWorkHours: Double,           // 총 근무 시간
    val overtimeHours: Double = 0.0,      // 초과 근무 시간
    val bonusAmount: Long = 0L,           // 보너스
    val deductionAmount: Long = 0L,       // 공제액
    val finalAmount: Long,                // 최종 급여액
    val paymentDate: LocalDate?,          // 실제 지급일
    val status: PayrollStatus = PayrollStatus.PENDING, // 지급 상태
    val notes: String? = null,            // 특이사항
    
) : BaseDocument() 