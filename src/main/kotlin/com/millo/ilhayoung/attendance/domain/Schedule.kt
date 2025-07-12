package com.millo.ilhayoung.attendance.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 근무 일정 도메인
 */
@Document(collection = "schedules")
@CompoundIndexes(
    CompoundIndex(
        name = "application_date_idx",
        def = "{'applicationId': 1, 'workDate': 1}",
        unique = true
    )
)
data class Schedule(
    val applicationId: String,            // 지원서 ID
    val recruitId: String,                // 채용공고 ID
    val staffId: String,                  // 스태프 ID
    val managerId: String,                // 매니저 ID
    val workDate: LocalDate,              // 근무 일자
    val startTime: LocalTime,             // 시작 시간
    val endTime: LocalTime,               // 종료 시간
    val position: String,                 // 직책
    val jobType: String,                  // 직무
    val workLocation: String,             // 근무지
    val companyName: String,              // 회사명
    val hourlyWage: Long,                 // 시급
    val status: WorkStatus = WorkStatus.SCHEDULED, // 근무 상태
    val endDateTime: LocalDateTime? = null // 근무 종료 일시 (결근 처리용 인덱스)
) : BaseDocument() 