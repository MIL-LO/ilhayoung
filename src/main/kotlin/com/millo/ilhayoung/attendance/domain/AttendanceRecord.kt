package com.millo.ilhayoung.attendance.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 출근 기록 도메인
 */
@Document(collection = "attendance_records")
@CompoundIndexes(
    CompoundIndex(
        name = "schedule_idx",
        def = "{'scheduleId': 1}",
        unique = true
    )
)
data class AttendanceRecord(
    val scheduleId: String,               // 스케줄 ID
    val staffId: String,                  // 스태프 ID
    val actualStartTime: LocalDateTime?,  // 실제 출근 시간
    val actualEndTime: LocalDateTime?,    // 실제 퇴근 시간
    val isLate: Boolean = false,          // 지각 여부
    val lateMinutes: Int = 0,             // 지각 시간(분)
    val notes: String? = null,            // 특이사항
    
) : BaseDocument() 