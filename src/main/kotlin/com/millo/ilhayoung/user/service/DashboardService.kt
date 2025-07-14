package com.millo.ilhayoung.user.service

import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
import com.millo.ilhayoung.recruit.domain.RecruitStatus
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.salary.repository.PayrollRecordRepository
import com.millo.ilhayoung.user.dto.*
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DashboardService(
    private val scheduleRepository: ScheduleRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val staffRepository: StaffRepository,
    private val recruitRepository: RecruitRepository,
    private val applicationRepository: ApplicationRepository,
    private val payrollRecordRepository: PayrollRecordRepository
) {
    fun getSummary(managerId: String): DashboardSummaryDto {
        val today = LocalDate.now()
        val thisMonth = YearMonth.now()

        // 1. 오늘 출근 인원 (PRESENT, LATE)
        val schedulesToday = scheduleRepository.findByManagerIdAndWorkDate(managerId, today)
        val todayAttendance = schedulesToday.count { it.status == WorkStatus.PRESENT || it.status == WorkStatus.LATE }

        // 2. 전체 근무자 수
        val totalStaff = staffRepository.findAll().count { it.isActive() && schedulesToday.any { s -> s.staffId == it.id } }

        // 3. 활성 공고 수
        val activeJobs = recruitRepository.findByManagerIdAndStatusIn(managerId, listOf(RecruitStatus.ACTIVE), org.springframework.data.domain.Pageable.unpaged()).totalElements

        // 4. 대기 중인 지원서 수 (모든 ACTIVE 공고의 REVIEWING 상태 지원서)
        val activeRecruitIds = recruitRepository.findByManagerIdAndStatusIn(managerId, listOf(RecruitStatus.ACTIVE), org.springframework.data.domain.Pageable.unpaged()).content.mapNotNull { it.id }
        val pendingApplications = if (activeRecruitIds.isNotEmpty()) {
            applicationRepository.findByRecruitIdIn(activeRecruitIds).count { it.status == ApplicationStatus.REVIEWING }
        } else 0

        // 5. 이번 주 매출/급여 (이번 달 지급된 급여 총합) - YearMonth 대신 전체 조회 후 필터링
        val allPayrolls = payrollRecordRepository.findByManagerIdOrderByPayrollPeriodDesc(managerId)
        val payrolls = allPayrolls.filter { it.payrollPeriod == thisMonth }
        val thisWeekSales = payrolls.sumOf { it.finalAmount }
        val thisWeekWages = payrolls.sumOf { it.finalAmount } // 실제 매출/급여 분리 필요시 로직 분리

        // 6. 출근율
        val attendanceRate = if (totalStaff > 0) todayAttendance.toDouble() / totalStaff else 0.0

        return DashboardSummaryDto(
            todayAttendance = todayAttendance,
            totalStaff = totalStaff,
            activeJobs = activeJobs.toInt(),
            pendingApplications = pendingApplications,
            thisWeekSales = thisWeekSales.toInt(),
            thisWeekWages = thisWeekWages.toInt(),
            attendanceRate = attendanceRate
        )
    }

    fun getActivities(managerId: String): List<DashboardActivityDto> {
        val activities = mutableListOf<DashboardActivityDto>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        
        // 1. 최근 출근 기록 (오늘)
        val today = LocalDate.now()
        val todaySchedules = scheduleRepository.findByManagerIdAndWorkDate(managerId, today)
        todaySchedules.forEach { schedule ->
            val attendanceRecord = attendanceRecordRepository.findByScheduleId(schedule.id!!)
            if (attendanceRecord?.actualStartTime != null) {
                val staff = staffRepository.findById(schedule.staffId).orElse(null)
                val staffName = staff?.oauth?.getDisplayName() ?: "알 수 없음"
                activities.add(
                    DashboardActivityDto(
                        activity = "$staffName 출근체크 완료",
                        time = attendanceRecord.actualStartTime!!.format(formatter),
                        type = "attendance"
                    )
                )
            }
        }
        
        // 2. 최근 지원 내역 (최근 7일)
        val recentApplications = applicationRepository.findByRecruitIdIn(
            recruitRepository.findByManagerIdAndStatusIn(
                managerId, 
                listOf(RecruitStatus.ACTIVE), 
                org.springframework.data.domain.Pageable.unpaged()
            ).content.mapNotNull { it.id }
        ).filter { it.createdAt!!.isAfter(LocalDateTime.now().minusDays(7)) }
        
        recentApplications.forEach { application ->
            val recruit = recruitRepository.findById(application.recruitId).orElse(null)
            val title = recruit?.title ?: "알 수 없는 공고"
            activities.add(
                DashboardActivityDto(
                    activity = title + "에 신규 지원서 도착",
                    time = application.createdAt!!.format(formatter),
                    type = "application"
                )
            )
        }
        
        // 3. 최근 공고 생성 (최근 7일)
        val recentRecruits = recruitRepository.findByManagerIdAndStatusIn(
            managerId,
            listOf(RecruitStatus.ACTIVE),
            org.springframework.data.domain.Pageable.unpaged()
        ).content.filter { it.createdAt!!.isAfter(LocalDateTime.now().minusDays(7)) }
        
        recentRecruits.forEach { recruit ->
            activities.add(
                DashboardActivityDto(
                    activity = "새 공고 등록: ${recruit.title}",
                    time = recruit.createdAt!!.format(formatter),
                    type = "recruit"
                )
            )
        }
        
        // 시간순 정렬 (최신순)
        return activities.sortedByDescending { it.time }.take(10)
    }

    fun getTasks(managerId: String): List<DashboardTaskDto> {
        val tasks = mutableListOf<DashboardTaskDto>()
        
        // 1. 대기 중인 지원서 검토
        val pendingApplications = applicationRepository.findByRecruitIdIn(
            recruitRepository.findByManagerIdAndStatusIn(
                managerId, 
                listOf(RecruitStatus.ACTIVE), 
                org.springframework.data.domain.Pageable.unpaged()
            ).content.mapNotNull { it.id }
        ).filter { it.status == ApplicationStatus.REVIEWING }
        
        if (pendingApplications.isNotEmpty()) {
            tasks.add(
                DashboardTaskDto(
                    title = "지원서 검토",
                    description = "${pendingApplications.size}건의 대기 중인 지원서가 있습니다.",
                    type = "application",
                    isUrgent = pendingApplications.size >= 5
                )
            )
        }
        
        // 2. 오늘 출근해야 할 근무자 확인
        val today = LocalDate.now()
        val todaySchedules = scheduleRepository.findByManagerIdAndWorkDate(managerId, today)
        val notCheckedIn = todaySchedules.filter { schedule ->
            val attendanceRecord = attendanceRecordRepository.findByScheduleId(schedule.id!!)
            attendanceRecord?.actualStartTime == null
        }
        
        if (notCheckedIn.isNotEmpty()) {
            tasks.add(
                DashboardTaskDto(
                    title = "출근체크 확인",
                    description = "${notCheckedIn.size}명의 근무자가 아직 출근체크를 하지 않았습니다.",
                    type = "attendance",
                    isUrgent = true
                )
            )
        }
        
        // 3. 급여 정산 (이번 달) - YearMonth 대신 전체 조회 후 필터링
        val allPayrollRecords = payrollRecordRepository.findByManagerIdOrderByPayrollPeriodDesc(managerId)
        val thisMonth = YearMonth.now()
        val payrollRecords = allPayrollRecords.filter { it.payrollPeriod == thisMonth }
        val pendingPayrolls = payrollRecords.filter { it.status.name == "PENDING" }
        
        if (pendingPayrolls.isNotEmpty()) {
            tasks.add(
                DashboardTaskDto(
                    title = "급여 정산",
                    description = "${pendingPayrolls.size}건의 급여 정산이 대기 중입니다.",
                    type = "salary",
                    isUrgent = false
                )
            )
        }
        
        // 4. 마감 임박 공고 확인
        val deadlineToday = recruitRepository.findByManagerIdAndStatusIn(
            managerId,
            listOf(RecruitStatus.ACTIVE),
            org.springframework.data.domain.Pageable.unpaged()
        ).content.filter { 
            it.deadline.toLocalDate() == today || it.deadline.toLocalDate() == today.plusDays(1)
        }
        
        if (deadlineToday.isNotEmpty()) {
            tasks.add(
                DashboardTaskDto(
                    title = "공고 마감",
                    description = "${deadlineToday.size}건의 공고가 곧 마감됩니다.",
                    type = "recruit",
                    isUrgent = true
                )
            )
        }
        
        return tasks
    }
} 