package com.millo.ilhayoung.attendance.service

import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.dto.*
import com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
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
) {
    fun getWorkersOverview(): WorkerOverviewDto {
        // 이 부분은 실제 로직 구현이 필요합니다.
        // 현재는 더미 데이터를 반환합니다.
        return WorkerOverviewDto(
            totalWorkers = 0,
            presentWorkers = 0,
            absentWorkers = 0,
            lateWorkers = 0,
            workers = emptyList()
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

        var attendanceRecord = attendanceRecordRepository.findByScheduleId(scheduleId)
            ?: throw BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND, "출근 기록을 찾을 수 없습니다.")

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
                workStatus = WorkStatus.COMPLETED
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