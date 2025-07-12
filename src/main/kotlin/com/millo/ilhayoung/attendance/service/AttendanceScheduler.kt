package com.millo.ilhayoung.attendance.service

import com.millo.ilhayoung.attendance.domain.WorkStatus
import com.millo.ilhayoung.attendance.repository.ScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class AttendanceScheduler(
    private val scheduleRepository: ScheduleRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 1시간마다 실행되어, 근무 종료 시간이 지났지만 여전히 'SCHEDULED' 상태인
     * 스케줄을 찾아 'ABSENT' (결근)으로 처리합니다.
     */
    @Transactional
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000 밀리초
    fun processAbsentSchedules() {
        val now = LocalDateTime.now()
        logger.info("결근 처리 스케줄러 실행: $now")

        // endDateTime이 있는 스케줄들 처리
        val absentCandidatesWithEndDateTime = scheduleRepository.findByStatusAndEndDateTimeBefore(WorkStatus.SCHEDULED, now)
        
        // endDateTime이 null인 스케줄들 처리 (workDate와 endTime으로 계산)
        val absentCandidatesWithoutEndDateTime = scheduleRepository.findByStatusAndEndDateTimeIsNull(WorkStatus.SCHEDULED)
            .filter { schedule ->
                val calculatedEndDateTime = schedule.workDate.atTime(schedule.endTime)
                now.isAfter(calculatedEndDateTime)
            }

        val allAbsentCandidates = absentCandidatesWithEndDateTime + absentCandidatesWithoutEndDateTime

        if (allAbsentCandidates.isEmpty()) {
            logger.info("결근 처리 대상 없음.")
            return
        }

        val updatedSchedules = allAbsentCandidates.map { schedule ->
            // endDateTime이 null인 경우 계산하여 설정
            val endDateTime = schedule.endDateTime ?: schedule.workDate.atTime(schedule.endTime)
            schedule.copy(status = WorkStatus.ABSENT, endDateTime = endDateTime)
        }
        scheduleRepository.saveAll(updatedSchedules)

        logger.info("${updatedSchedules.size}건의 스케줄을 결근 처리했습니다.")
    }
} 