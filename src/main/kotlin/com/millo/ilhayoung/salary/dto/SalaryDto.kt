package com.millo.ilhayoung.salary.dto

import com.millo.ilhayoung.salary.domain.PayrollStatus
import com.millo.ilhayoung.salary.domain.SalaryType
import java.time.LocalDate
import java.time.YearMonth

/**
 * 스태프 급여 추정 DTO
 */
data class EstimatedSalaryDto(
    val currentMonth: YearMonth,
    val estimatedAmount: Long,
    val totalWorkDays: Int,
    val completedWorkDays: Int,
    val remainingWorkDays: Int,
    val totalWorkHours: Double,
    val averageHourlyWage: Long,
    val nextPaymentDate: LocalDate?,
    val workSummaries: List<WorkSummaryDto>
)

/**
 * 작업 요약 DTO
 */
data class WorkSummaryDto(
    val companyName: String,
    val workDays: Int,
    val workHours: Double,
    val hourlyWage: Long,
    val estimatedEarnings: Long
)

/**
 * 급여 이력 DTO
 */
data class PayrollHistoryDto(
    val id: String,
    val payrollPeriod: YearMonth,
    val companyName: String,
    val position: String,
    val jobType: String,
    val salaryType: SalaryType,
    val finalAmount: Long,
    val paymentDate: LocalDate?,
    val status: PayrollStatus
)

/**
 * 급여 상세 정보 DTO
 */
data class PayrollDetailDto(
    val id: String,
    val staffId: String,
    val staffName: String,
    val payrollPeriod: YearMonth,
    val companyName: String,
    val position: String,
    val jobType: String,
    val salaryType: SalaryType,
    val baseAmount: Long,
    val totalWorkDays: Int,
    val actualWorkDays: Int,
    val totalWorkHours: Double,
    val overtimeHours: Double,
    val bonusAmount: Long,
    val deductionAmount: Long,
    val finalAmount: Long,
    val paymentDate: LocalDate?,
    val status: PayrollStatus,
    val notes: String?
)

/**
 * 급여 계산 요청 DTO
 */
data class PayrollCalculationRequestDto(
    val staffIds: List<String>,
    val payrollPeriod: YearMonth,
    val bonusAmount: Long = 0L,
    val deductionAmount: Long = 0L,
    val notes: String? = null
)

/**
 * 급여 승인 요청 DTO
 */
data class PayrollApprovalDto(
    val payrollRecordIds: List<String>,
    val paymentDate: LocalDate
) 