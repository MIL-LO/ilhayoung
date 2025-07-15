package com.millo.ilhayoung.attendance.service

import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.dto.*
import com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class AttendanceService(
    private val scheduleRepository: ScheduleRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val staffRepository: StaffRepository,
    private val applicationRepository: ApplicationRepository,
    private val recruitRepository: RecruitRepository,
) {
    fun getWorkersOverview(): WorkerOverviewDto {
        val today = LocalDate.now()
        
        // HIRED 상태인 모든 지원자 조회
        val hiredApplications = applicationRepository.findByStatus(ApplicationStatus.HIRED)
        
        val workers = mutableListOf<StaffDetailDto>()
        var presentWorkers = 0
        var absentWorkers = 0
        var lateWorkers = 0
        
        for (application in hiredApplications) {
            // 오늘 스케줄 조회
            val todaySchedules = scheduleRepository.findByStaffIdAndWorkDate(application.staffId, today)
            val todaySchedule = todaySchedules.firstOrNull()
            
            // 출근 기록 조회
            val attendanceRecord = todaySchedule?.let { 
                attendanceRecordRepository.findByScheduleId(it.id!!) 
            }
            
            // 오늘 상태 결정
            val todayStatus = when {
                todaySchedule == null -> WorkStatus.SCHEDULED // 오늘 스케줄 없음
                attendanceRecord?.actualStartTime != null && attendanceRecord.actualEndTime != null -> WorkStatus.COMPLETED
                attendanceRecord?.actualStartTime != null -> {
                    val scheduleStartTime = todaySchedule.workDate.atTime(todaySchedule.startTime)
                    if (attendanceRecord.actualStartTime!!.isAfter(scheduleStartTime)) {
                        lateWorkers++
                        WorkStatus.LATE
                    } else {
                        presentWorkers++
                        WorkStatus.PRESENT
                    }
                }
                else -> {
                    // 스케줄이 있지만 아직 출근하지 않은 경우
                    val now = LocalTime.now()
                    val scheduleEndTime = todaySchedule.endTime
                    if (now.isAfter(scheduleEndTime)) {
                        absentWorkers++
                        WorkStatus.ABSENT
                    } else {
                        WorkStatus.SCHEDULED
                    }
                }
            }
            
            // 채용공고 정보 조회
            val recruit = recruitRepository.findById(application.recruitId).orElse(null)
            
            val worker = StaffDetailDto(
                staffId = application.staffId,
                staffName = application.name,
                todayStatus = todayStatus,
                startTime = todaySchedule?.startTime,
                endTime = todaySchedule?.endTime,
                workLocation = recruit?.workLocation ?: "",
                weeklyWorkMinutes = 0, // TODO: 주간 근무시간 계산 구현
                monthlyWorkMinutes = 0  // TODO: 월간 근무시간 계산 구현
            )
            
            workers.add(worker)
        }
        
        return WorkerOverviewDto(
            totalWorkers = workers.size,
            presentWorkers = presentWorkers,
            absentWorkers = absentWorkers,
            lateWorkers = lateWorkers,
            workers = workers
        )
    }

    fun getStaffDetail(staffId: String): StaffDetailDto {
        // 이 부분은 실제 로직 구현이 필요합니다.
        // 현재는 더미 데이터를 반환합니다.
        val staff = staffRepository.findById(staffId).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        return StaffDetailDto(
            staffId = staff.id!!,
            staffName = staff.getName(),
            todayStatus = WorkStatus.SCHEDULED,
            startTime = null,
            endTime = null,
            workLocation = "",
            weeklyWorkMinutes = 0,
            monthlyWorkMinutes = 0
        )
    }

    fun updateStaffStatus(staffId: String, status: WorkStatus) {
        val today = LocalDate.now()
        val schedules = scheduleRepository.findByStaffIdAndWorkDate(staffId, today)
        if (schedules.isEmpty()) {
            throw BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND, "오늘 스케줄이 없습니다.")
        }
        val schedule = schedules.first()
        val updatedSchedule = schedule.copy(status = status)
        scheduleRepository.save(updatedSchedule)
    }

    fun processCheckInOut(userId: String, scheduleId: String, checkType: CheckType): CheckInOutResponseDto {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND, "스케줄을 찾을 수 없습니다.") }

        if (schedule.staffId != userId) {
            throw BusinessException(ErrorCode.FORBIDDEN, "자신의 스케줄에만 체크인/아웃 할 수 있습니다.")
        }

        // 출근 기록이 없으면 생성
        var attendanceRecord = attendanceRecordRepository.findByScheduleId(scheduleId)
        if (attendanceRecord == null) {
            attendanceRecord = com.millo.ilhayoung.attendance.domain.AttendanceRecord(
                scheduleId = scheduleId,
                staffId = userId,
                managerId = schedule.managerId,
                staffName = staffRepository.findById(userId).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }.getName(),
                workDate = schedule.workDate,
                status = "SCHEDULED",
                actualStartTime = null,
                actualEndTime = null,
                isLate = false,
                lateMinutes = 0
            )
        }

        val now = LocalDateTime.now()
        var workStatus = schedule.status
        var message: String

        when (checkType) {
            CheckType.CHECK_IN -> {
                if (attendanceRecord.actualStartTime != null) {
                    throw BusinessException(ErrorCode.ALREADY_CLOCKED_IN)
                }
                attendanceRecord = attendanceRecord.copy(actualStartTime = now)
                
                val scheduleStartTime = schedule.workDate.atTime(schedule.startTime)
                workStatus = if (now.isAfter(scheduleStartTime)) WorkStatus.LATE else WorkStatus.PRESENT
                message = "체크인 되었습니다."
            }
            CheckType.CHECK_OUT -> {
                if (attendanceRecord.actualStartTime == null) {
                    throw BusinessException(ErrorCode.NOT_CLOCKED_IN)
                }
                if (attendanceRecord.actualEndTime != null) {
                    throw BusinessException(ErrorCode.ALREADY_CLOCKED_OUT)
                }
                attendanceRecord = attendanceRecord.copy(actualEndTime = now)
                
                // 지각 상태일 때는 체크아웃 후에도 LATE 상태 유지
                workStatus = if (schedule.status == WorkStatus.LATE) {
                    WorkStatus.LATE
                } else {
                    WorkStatus.COMPLETED
                }
                message = "체크아웃 되었습니다."
            }
        }

        attendanceRecordRepository.save(attendanceRecord)
        scheduleRepository.save(schedule.copy(status = workStatus))
        
        return CheckInOutResponseDto(
            success = true,
            currentStatus = workStatus,
            statusMessage = message,
            checkInTime = attendanceRecord.actualStartTime?.toLocalTime(),
            checkOutTime = attendanceRecord.actualEndTime?.toLocalTime()
        )
    }
} 