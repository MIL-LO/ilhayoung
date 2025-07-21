package com.millo.ilhayoung.attendance.dto

import com.millo.ilhayoung.attendance.domain.WorkStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

/**
 * 근로자 전체 현황 DTO
 */
@Schema(description = "관리자용 근로자 전체 현황 정보")
data class WorkerOverviewDto(
    @Schema(
        description = "총 근로자 수",
        example = "15"
    )
    val totalWorkers: Int,
    
    @Schema(
        description = "오늘 출근한 근로자 수",
        example = "12"
    )
    val presentWorkers: Int,
    
    @Schema(
        description = "오늘 결근한 근로자 수", 
        example = "2"
    )
    val absentWorkers: Int,
    
    @Schema(
        description = "오늘 지각한 근로자 수",
        example = "1"
    )
    val lateWorkers: Int,
    
    @Schema(
        description = "개별 근로자 상태 목록"
    )
    val workers: List<StaffDetailDto>
)

/**
 * 개별 근로자 상세 정보 DTO
 */
@Schema(description = "개별 근로자의 상세 근무 정보")
data class StaffDetailDto(
    @Schema(
        description = "스태프 고유 ID",
        example = "65a1b2c3d4e5f6789abcdef3"
    )
    val staffId: String,
    
    @Schema(
        description = "스태프 이름",
        example = "김직원"
    )
    val staffName: String,
    
    @Schema(
        description = "오늘의 근무 상태",
        example = "PRESENT",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"]
    )
    val todayStatus: WorkStatus,
    
    @Schema(
        description = "오늘 근무 시작 시간",
        example = "09:00"
    )
    val startTime: LocalTime?,
    
    @Schema(
        description = "오늘 근무 종료 시간",
        example = "18:00"
    )
    val endTime: LocalTime?,
    
    @Schema(
        description = "근무 장소",
        example = "서울시 강남구 테헤란로 123"
    )
    val workLocation: String?,
    
    @Schema(
        description = "이번 주 총 근무 시간 (분 단위)",
        example = "2400"
    )
    val weeklyWorkMinutes: Long,
    
    @Schema(
        description = "이번 달 총 근무 시간 (분 단위)",
        example = "10800"
    )
    val monthlyWorkMinutes: Long
)

/**
 * 이번 주 예정 스케줄 DTO
 */
@Schema(description = "이번 주 예정된 근무 스케줄")
data class UpcomingScheduleDto(
    @Schema(
        description = "스케줄 고유 ID",
        example = "65a1b2c3d4e5f6789abcdef0"
    )
    val scheduleId: String,
    
    @Schema(
        description = "근무 날짜",
        example = "2024-01-15"
    )
    val workDate: LocalDate,
    
    @Schema(
        description = "근무 시작 시간",
        example = "09:00"
    )
    val startTime: LocalTime,
    
    @Schema(
        description = "근무 종료 시간",
        example = "18:00"
    )
    val endTime: LocalTime,
    
    @Schema(
        description = "회사명",
        example = "스타벅스 강남점"
    )
    val companyName: String
)

/**
 * 스태프 상태 업데이트 요청 DTO
 */
@Schema(description = "스태프 근무 상태 수동 변경 요청")
data class StaffStatusUpdateDto(
    @Schema(
        description = "변경할 근무 상태. 관리자가 특정 스태프의 상태를 수동으로 변경할 때 사용",
        example = "ABSENT",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val status: WorkStatus
)

/**
 * 체크인/체크아웃 요청 DTO
 */
@Schema(description = "출근/퇴근 체크 요청")
data class CheckInOutRequestDto(
    @Schema(
        description = "스케줄 고유 ID (오늘의 스케줄 조회 API에서 획득)",
        example = "65a1b2c3d4e5f6789abcdef0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val scheduleId: String,
    
    @Schema(
        description = "체크 유형",
        example = "CHECK_IN",
        allowableValues = ["CHECK_IN", "CHECK_OUT"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val checkType: CheckType
)

/**
 * 체크 유형 ENUM
 */
@Schema(description = "체크인/체크아웃 유형")
enum class CheckType {
    @Schema(description = "출근 체크인")
    CHECK_IN,
    
    @Schema(description = "퇴근 체크아웃")
    CHECK_OUT
}

/**
 * 체크인/체크아웃 응답 DTO
 */
@Schema(description = "출근/퇴근 체크 결과")
data class CheckInOutResponseDto(
    @Schema(
        description = "처리 성공 여부",
        example = "true"
    )
    val success: Boolean,
    
    @Schema(
        description = "현재 근무 상태 (자동 결정됨)",
        example = "PRESENT",
        allowableValues = ["PRESENT", "LATE", "ABSENT", "COMPLETED"]
    )
    val currentStatus: WorkStatus,
    
    @Schema(
        description = "상태 변경 사유 메시지",
        example = "정시 출근 처리되었습니다."
    )
    val statusMessage: String,
    
    @Schema(
        description = "체크인 시간 (출근 체크 시)",
        example = "09:00"
    )
    val checkInTime: LocalTime?,
    
    @Schema(
        description = "체크아웃 시간 (퇴근 체크 시)", 
        example = "18:00"
    )
    val checkOutTime: LocalTime?
) 