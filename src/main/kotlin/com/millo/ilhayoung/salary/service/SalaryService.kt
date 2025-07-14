package com.millo.ilhayoung.salary.service

import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.salary.domain.PayrollRecord
import com.millo.ilhayoung.salary.domain.PayrollStatus
import com.millo.ilhayoung.salary.domain.SalaryType
import com.millo.ilhayoung.salary.dto.*
import com.millo.ilhayoung.salary.repository.PayrollRecordRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth

@Service
@Transactional
class SalaryService(
    private val payrollRecordRepository: PayrollRecordRepository,
    private val scheduleRepository: ScheduleRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val staffRepository: StaffRepository,
    private val recruitRepository: RecruitRepository
) {

    /**
     * 스태프 급여 추정 조회
     */
    @Transactional(readOnly = true)
    fun getEstimatedSalary(staffId: String): EstimatedSalaryDto {
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.atDay(1)
        val endDate = currentMonth.atEndOfMonth()

        // 현재 월의 모든 스케줄 조회
        val schedules = scheduleRepository.findByStaffIdAndWorkDateBetween(staffId, startDate, endDate)
        
        if (schedules.isEmpty()) {
            throw BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "이번 달 스케줄이 없습니다.")
        }

        val totalWorkDays = schedules.size
        val completedWorkDays = schedules.count { it.status == WorkStatus.COMPLETED }
        val remainingWorkDays = schedules.count { 
            it.status == WorkStatus.SCHEDULED || it.status == WorkStatus.PRESENT 
        }

        // 회사별 작업 요약 생성
        val workSummaries = schedules
            .groupBy { it.companyName }
            .map { (companyName, companySchedules) ->
                val workDays = companySchedules.size
                val workHours = calculateTotalWorkHours(companySchedules.map { it.id!! })
                val hourlyWage = companySchedules.first().hourlyWage
                
                WorkSummaryDto(
                    companyName = companyName,
                    workDays = workDays,
                    workHours = workHours,
                    hourlyWage = hourlyWage,
                    estimatedEarnings = (workHours * hourlyWage).toLong()
                )
            }

        val totalWorkHours = workSummaries.sumOf { it.workHours }
        val estimatedAmount = workSummaries.sumOf { it.estimatedEarnings }
        val averageHourlyWage = if (totalWorkHours > 0) {
            (estimatedAmount / totalWorkHours).toLong()
        } else 0L

        // 다음 지급일 계산 (채용공고의 급여 정산일 기준)
        val nextPaymentDate = calculateNextPaymentDate(schedules)

        return EstimatedSalaryDto(
            currentMonth = currentMonth,
            estimatedAmount = estimatedAmount,
            totalWorkDays = totalWorkDays,
            completedWorkDays = completedWorkDays,
            remainingWorkDays = remainingWorkDays,
            totalWorkHours = totalWorkHours,
            averageHourlyWage = averageHourlyWage,
            nextPaymentDate = nextPaymentDate,
            workSummaries = workSummaries
        )
    }

    /**
     * 스태프 급여 이력 조회
     */
    @Transactional(readOnly = true)
    fun getPayrollHistory(staffId: String): List<PayrollHistoryDto> {
        val payrollRecords = payrollRecordRepository.findByStaffIdOrderByPayrollPeriodDesc(staffId)
        
        return payrollRecords.map { record ->
            PayrollHistoryDto(
                id = record.id!!,
                payrollPeriod = record.payrollPeriod,
                companyName = record.companyName,
                position = record.position,
                jobType = record.jobType,
                salaryType = record.salaryType,
                finalAmount = record.finalAmount,
                paymentDate = record.paymentDate,
                status = record.status
            )
        }
    }

    /**
     * 급여 상세 정보 조회 (MANAGER 전용)
     */
    @Transactional(readOnly = true)
    fun getPayrollDetail(payrollRecordId: String): PayrollDetailDto {
        val payrollRecord = payrollRecordRepository.findById(payrollRecordId)
            .orElseThrow { BusinessException(ErrorCode.SALARY_NOT_FOUND, "급여 기록을 찾을 수 없습니다.") }
        
        val staff = staffRepository.findById(payrollRecord.staffId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "스태프를 찾을 수 없습니다.") }

        return PayrollDetailDto(
            id = payrollRecord.id!!,
            staffId = payrollRecord.staffId,
            staffName = staff.getName(),
            payrollPeriod = payrollRecord.payrollPeriod,
            companyName = payrollRecord.companyName,
            position = payrollRecord.position,
            jobType = payrollRecord.jobType,
            salaryType = payrollRecord.salaryType,
            baseAmount = payrollRecord.baseAmount,
            totalWorkDays = payrollRecord.totalWorkDays,
            actualWorkDays = payrollRecord.actualWorkDays,
            totalWorkHours = payrollRecord.totalWorkHours,
            overtimeHours = payrollRecord.overtimeHours,
            bonusAmount = payrollRecord.bonusAmount,
            deductionAmount = payrollRecord.deductionAmount,
            finalAmount = payrollRecord.finalAmount,
            paymentDate = payrollRecord.paymentDate,
            status = payrollRecord.status,
            notes = payrollRecord.notes
        )
    }

    /**
     * 급여 계산 및 생성 (MANAGER 전용)
     */
    fun calculatePayroll(requestDto: PayrollCalculationRequestDto): List<PayrollRecord> {
        val results = mutableListOf<PayrollRecord>()
        
        requestDto.staffIds.forEach { staffId ->
            val payrollRecord = calculateStaffPayroll(staffId, requestDto)
            results.add(payrollRecord)
        }
        
        return payrollRecordRepository.saveAll(results)
    }

    /**
     * 급여 승인 (MANAGER 전용)
     */
    fun approvePayrolls(approvalDto: PayrollApprovalDto): List<PayrollRecord> {
        val payrollRecords = payrollRecordRepository.findAllById(approvalDto.payrollRecordIds)
        
        val updatedRecords = payrollRecords.map { record ->
            record.copy(
                status = PayrollStatus.APPROVED,
                paymentDate = approvalDto.paymentDate
            )
        }
        
        return payrollRecordRepository.saveAll(updatedRecords)
    }

    /**
     * 매니저별 급여 기록 조회
     */
    @Transactional(readOnly = true)
    fun getManagerPayrollRecords(managerId: String): List<PayrollHistoryDto> {
        val payrollRecords = payrollRecordRepository.findByManagerIdOrderByPayrollPeriodDesc(managerId)
        
        return payrollRecords.map { record ->
            PayrollHistoryDto(
                id = record.id!!,
                payrollPeriod = record.payrollPeriod,
                companyName = record.companyName,
                position = record.position,
                jobType = record.jobType,
                salaryType = record.salaryType,
                finalAmount = record.finalAmount,
                paymentDate = record.paymentDate,
                status = record.status
            )
        }
    }

    /**
     * 개별 스태프 급여 계산
     */
    private fun calculateStaffPayroll(staffId: String, requestDto: PayrollCalculationRequestDto): PayrollRecord {
        val staff = staffRepository.findById(staffId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "스태프를 찾을 수 없습니다.") }

        val startDate = requestDto.payrollPeriod.atDay(1)
        val endDate = requestDto.payrollPeriod.atEndOfMonth()

        val schedules = scheduleRepository.findByStaffIdAndWorkDateBetween(staffId, startDate, endDate)
        
        if (schedules.isEmpty()) {
            throw BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "해당 기간의 스케줄이 없습니다.")
        }

        val totalWorkDays = schedules.size
        val actualWorkDays = schedules.count { it.status == WorkStatus.COMPLETED }
        val totalWorkHours = calculateTotalWorkHours(schedules.map { it.id!! })

        // 첫 번째 스케줄 기준으로 기본 정보 설정
        val firstSchedule = schedules.first()
        val baseAmount = (totalWorkHours * firstSchedule.hourlyWage).toLong()
        val finalAmount = baseAmount + requestDto.bonusAmount - requestDto.deductionAmount

        return PayrollRecord(
            staffId = staffId,
            managerId = firstSchedule.managerId,
            payrollPeriod = requestDto.payrollPeriod,
            companyName = firstSchedule.companyName,
            position = firstSchedule.position,
            jobType = firstSchedule.jobType,
            salaryType = SalaryType.HOURLY, // 기본적으로 시급으로 설정
            baseAmount = baseAmount,
            totalWorkDays = totalWorkDays,
            actualWorkDays = actualWorkDays,
            totalWorkHours = totalWorkHours,
            bonusAmount = requestDto.bonusAmount,
            deductionAmount = requestDto.deductionAmount,
            finalAmount = finalAmount,
            paymentDate = null,
            status = PayrollStatus.CALCULATED,
            notes = requestDto.notes
        )
    }

    /**
     * 총 근무 시간 계산
     */
    private fun calculateTotalWorkHours(scheduleIds: List<String>): Double {
        val attendanceRecords = scheduleIds.mapNotNull { scheduleId ->
            attendanceRecordRepository.findByScheduleId(scheduleId)
        }

        return attendanceRecords
            .filter { it.actualStartTime != null && it.actualEndTime != null }
            .sumOf { attendance ->
                Duration.between(attendance.actualStartTime, attendance.actualEndTime).toMinutes() / 60.0
            }
    }

    /**
     * 다음 지급일 계산
     */
    private fun calculateNextPaymentDate(schedules: List<com.millo.ilhayoung.attendance.domain.Schedule>): LocalDate? {
        // 첫 번째 스케줄의 채용공고에서 급여 정산일 조회
        val firstSchedule = schedules.first()
        val recruit = recruitRepository.findById(firstSchedule.recruitId).orElse(null)
        
        return recruit?.let {
            try {
                val paymentDay = it.paymentDate.toInt()
                val today = LocalDate.now()
                val thisMonthPayment = today.withDayOfMonth(paymentDay)
                
                if (today.isBefore(thisMonthPayment)) {
                    thisMonthPayment
                } else {
                    today.plusMonths(1).withDayOfMonth(paymentDay)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
} 