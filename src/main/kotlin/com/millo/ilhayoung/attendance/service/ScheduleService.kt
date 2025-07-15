package com.millo.ilhayoung.attendance.service

import com.millo.ilhayoung.attendance.domain.Schedule
import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.dto.MonthlyScheduleDto
import com.millo.ilhayoung.attendance.dto.ReplacementInfoDto
import com.millo.ilhayoung.attendance.dto.ScheduleDetailDto
import com.millo.ilhayoung.attendance.dto.TodayScheduleDto
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.Application
import com.millo.ilhayoung.recruit.domain.Recruit
import com.millo.ilhayoung.recruit.domain.WorkPeriod
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.user.domain.Staff
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val applicationRepository: ApplicationRepository,
    private val recruitRepository: RecruitRepository,
    private val staffRepository: StaffRepository,
    private val attendanceRecordRepository: com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
) {

    /**
     * 채용 확정 시 근무 일정 자동 생성
     */
    fun createSchedulesFromApplication(applicationId: String): List<Schedule> {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "지원서를 찾을 수 없습니다.") }
        
        val recruit = recruitRepository.findById(application.recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "채용공고를 찾을 수 없습니다.") }

        // 기존 스케줄이 있는지 확인하고 삭제
        val existingSchedules = scheduleRepository.findByApplicationId(applicationId)
        if (existingSchedules.isNotEmpty()) {
            println("기존 스케줄 ${existingSchedules.size}개를 삭제하고 새로 생성합니다.")
            scheduleRepository.deleteAll(existingSchedules)
        }

        return generateSchedules(application, recruit)
    }

    /**
     * 월별 스케줄 조회 (STAFF는 본인 것만, MANAGER는 관리하는 모든 것)
     */
    @Transactional(readOnly = true)
    fun getMonthlySchedules(
        year: Int,
        month: Int,
        userId: String,
        userType: String? = null
    ): List<MonthlyScheduleDto> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        // 사용자 타입에 따라 다른 조회 방식 사용
        val schedules = when (userType?.uppercase()) {
            "STAFF" -> {
                // STAFF는 본인의 스케줄만 조회
                scheduleRepository.findByStaffIdAndWorkDateBetween(userId, startDate, endDate)
            }
            "MANAGER" -> {
                // MANAGER는 관리하는 모든 스케줄을 조회
                scheduleRepository.findByManagerIdAndWorkDateBetween(userId, startDate, endDate)
            }
            else -> {
                // 기본값: STAFF로 가정하고 본인 스케줄만 조회
                scheduleRepository.findByStaffIdAndWorkDateBetween(userId, startDate, endDate)
            }
        }

        // 스태프 정보를 한 번에 조회하여 성능 최적화
        val staffIds = schedules.map { it.staffId }.distinct()
        val staffMap = staffRepository.findAllById(staffIds).associateBy { it.id!! }

        return schedules.map { schedule ->
            val staff = staffMap[schedule.staffId]
            MonthlyScheduleDto(
                id = schedule.id!!,
                workDate = schedule.workDate,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                staffName = staff?.getName() ?: "알 수 없음",
                companyName = schedule.companyName,
                position = schedule.position,
                jobType = schedule.jobType,
                hourlyWage = schedule.hourlyWage,
                paymentDate = schedule.paymentDate,
                status = schedule.status
            )
        }
    }

    /**
     * 스케줄 상세 조회
     */
    @Transactional(readOnly = true)
    fun getScheduleDetail(scheduleId: String, userId: String): ScheduleDetailDto {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "스케줄을 찾을 수 없습니다.") }
        
        val staff = staffRepository.findById(schedule.staffId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "스태프를 찾을 수 없습니다.") }

        return ScheduleDetailDto(
            id = schedule.id!!,
            applicationId = schedule.applicationId,
            recruitId = schedule.recruitId,
            staffId = schedule.staffId,
            staffName = staff.getName(),
            workDate = schedule.workDate,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            position = schedule.position,
            jobType = schedule.jobType,
            workLocation = schedule.workLocation,
            companyName = schedule.companyName,
            hourlyWage = schedule.hourlyWage,
            status = schedule.status
        )
    }

    /**
     * 스케줄 상태 업데이트
     */
    fun updateScheduleStatus(scheduleId: String, status: WorkStatus): Schedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "스케줄을 찾을 수 없습니다.") }

        val updatedSchedule = schedule.copy(status = status)
        return scheduleRepository.save(updatedSchedule)
    }

    /**
     * 대체 근무자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getReplacementInfo(scheduleId: String): ReplacementInfoDto {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "스케줄을 찾을 수 없습니다.") }
        
        val recruit = recruitRepository.findById(schedule.recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "채용공고를 찾을 수 없습니다.") }
        
        val staff = staffRepository.findById(schedule.staffId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "스태프를 찾을 수 없습니다.") }

        return ReplacementInfoDto(
            recruitId = recruit.id!!,
            title = "${recruit.title} (대체근무)",
            workLocation = schedule.workLocation,
            workDate = schedule.workDate,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            position = schedule.position,
            jobType = schedule.jobType,
            hourlyWage = schedule.hourlyWage,
            absentStaffName = staff.getName()
        )
    }

    /**
     * 오늘의 근무 스케줄 조회
     */
    @Transactional(readOnly = true)
    fun getTodaySchedule(staffId: String): TodayScheduleDto {
        val today = LocalDate.now()
        val schedules = scheduleRepository.findByStaffIdAndWorkDate(staffId, today)
        
        if (schedules.isEmpty()) {
            throw BusinessException(ErrorCode.RECRUIT_NOT_FOUND, "오늘 근무 스케줄이 없습니다.")
        }
        
        // PRESENT 상태의 스케줄을 우선으로 선택, 없으면 SCHEDULED 상태 선택
        val schedule = schedules.find { it.status == WorkStatus.PRESENT }
            ?: schedules.find { it.status == WorkStatus.SCHEDULED }
            ?: schedules.first()
            
        val now = LocalTime.now()
        
        // 출근 기록 확인
        val attendanceRecord = attendanceRecordRepository.findByScheduleId(schedule.id!!)
        
        // 출근/퇴근 가능 시간 계산 (30분 여유)
        val checkInStartTime = schedule.startTime.minusMinutes(30)
        val checkOutEndTime = schedule.endTime.plusMinutes(30)
        
        val canCheckIn = attendanceRecord?.actualStartTime == null && 
                        schedule.status == WorkStatus.SCHEDULED &&
                        now.isAfter(checkInStartTime) && now.isBefore(checkOutEndTime)
        
        val canCheckOut = (schedule.status == WorkStatus.PRESENT || schedule.status == WorkStatus.LATE) && 
                         (attendanceRecord?.actualEndTime == null) &&
                         now.isAfter(schedule.startTime) && now.isBefore(checkOutEndTime)
        
        val statusMessage = when (schedule.status) {
            WorkStatus.SCHEDULED -> {
                when {
                    now.isBefore(checkInStartTime) -> "출근 가능 시간: ${checkInStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))}부터"
                    now.isAfter(checkOutEndTime) -> "근무 시간이 지났습니다."
                    else -> "근무 시간입니다. 출근 체크를 해주세요."
                }
            }
            WorkStatus.PRESENT -> "출근 완료. 퇴근 체크를 잊지 마세요."
            WorkStatus.LATE -> "지각 출근 완료. 퇴근 체크를 잊지 마세요."
            WorkStatus.COMPLETED -> "근무 완료"
            WorkStatus.ABSENT -> "결근"
        }
        
        return TodayScheduleDto(
            id = schedule.id!!,
            workDate = schedule.workDate,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            companyName = schedule.companyName,
            position = schedule.position,
            jobType = schedule.jobType,
            workLocation = schedule.workLocation,
            status = schedule.status,
            canCheckIn = canCheckIn,
            canCheckOut = canCheckOut,
            statusMessage = statusMessage
        )
    }

    /**
     * 근무 일정 생성 로직
     */
    private fun generateSchedules(application: Application, recruit: Recruit): List<Schedule> {
        val workSchedules = recruit.workSchedule
        val schedules = mutableListOf<Schedule>()

        val startDate = application.createdAt.toLocalDate()
        val periodDays = when (workSchedules.workPeriod) {
            WorkPeriod.WITHIN_WEEK -> 7
            WorkPeriod.ONE_MONTH -> 30
            WorkPeriod.ONE_TO_THREE -> 90
            else -> 0
        }
        val endDate = startDate.plusDays(periodDays.toLong())

        val workDayMap = workSchedules.days.associate {
            koreanDayToEnglish(it) to (LocalTime.parse(workSchedules.startTime) to LocalTime.parse(workSchedules.endTime))
        }

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            workDayMap[currentDate.dayOfWeek]?.let { (startTime, endTime) ->
                schedules.add(
                    Schedule(
                        applicationId = application.id!!,
                        recruitId = recruit.id!!,
                        managerId = recruit.managerId,
                        staffId = application.staffId,
                        workDate = currentDate,
                        startTime = startTime,
                        endTime = endTime,
                        position = recruit.position,
                        jobType = recruit.jobType,
                        workLocation = recruit.workLocation,
                        companyName = recruit.companyName,
                        hourlyWage = recruit.salary,
                        status = WorkStatus.SCHEDULED,
                        endDateTime = currentDate.atTime(endTime),
                        paymentDate = recruit.paymentDate
                    )
                )
            }
            currentDate = currentDate.plusDays(1)
        }

        return scheduleRepository.saveAll(schedules)
    }

    /**
     * WorkSchedule과 WorkPeriod에 따른 근무 날짜 생성
     */
    private fun generateWorkDates(workSchedule: com.millo.ilhayoung.recruit.domain.WorkSchedule): List<LocalDate> {
        val today = LocalDate.now()
        val workDays = workSchedule.days.map { koreanDayToEnglish(it) }
        
        return when (workSchedule.workPeriod) {
            com.millo.ilhayoung.recruit.domain.WorkPeriod.ONE_DAY -> {
                listOf(getNextWorkDay(today, workDays))
            }
            com.millo.ilhayoung.recruit.domain.WorkPeriod.WITHIN_WEEK -> {
                generateDatesWithinPeriod(today, workDays, 7)
            }
            com.millo.ilhayoung.recruit.domain.WorkPeriod.ONE_MONTH -> {
                generateDatesWithinPeriod(today, workDays, 30)
            }
            com.millo.ilhayoung.recruit.domain.WorkPeriod.ONE_TO_THREE -> {
                generateDatesWithinPeriod(today, workDays, 90)
            }
            com.millo.ilhayoung.recruit.domain.WorkPeriod.THREE_TO_SIX -> {
                generateDatesWithinPeriod(today, workDays, 180)
            }
            com.millo.ilhayoung.recruit.domain.WorkPeriod.LONG_TERM -> {
                generateDatesWithinPeriod(today, workDays, 365)
            }
        }
    }

    private fun getNextWorkDay(startDate: LocalDate, workDays: List<DayOfWeek>): LocalDate {
        var date = startDate
        while (!workDays.contains(date.dayOfWeek)) {
            date = date.plusDays(1)
        }
        return date
    }

    private fun generateDatesWithinPeriod(
        startDate: LocalDate,
        workDays: List<DayOfWeek>,
        periodDays: Long
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate
        val endDate = startDate.plusDays(periodDays)

        while (!currentDate.isAfter(endDate)) {
            if (workDays.contains(currentDate.dayOfWeek)) {
                dates.add(currentDate)
            }
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }

    private fun koreanDayToEnglish(koreanDay: String): DayOfWeek {
        return when (koreanDay) {
            "월" -> DayOfWeek.MONDAY
            "화" -> DayOfWeek.TUESDAY
            "수" -> DayOfWeek.WEDNESDAY
            "목" -> DayOfWeek.THURSDAY
            "금" -> DayOfWeek.FRIDAY
            "토" -> DayOfWeek.SATURDAY
            "일" -> DayOfWeek.SUNDAY
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 요일입니다: $koreanDay")
        }
    }
} 