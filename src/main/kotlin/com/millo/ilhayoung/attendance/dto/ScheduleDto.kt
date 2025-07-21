package com.millo.ilhayoung.attendance.dto

import com.millo.ilhayoung.attendance.domain.WorkStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

/**
 * 월별 달력 조회용 DTO
 */
@Schema(description = "월별 달력 조회용 스케줄 정보")
data class MonthlyScheduleDto(
    @Schema(
        description = "스케줄 고유 ID",
        example = "65a1b2c3d4e5f6789abcdef0"
    )
    val id: String,
    
    @Schema(
        description = "근무 날짜 (YYYY-MM-DD 형식)",
        example = "2024-01-15"
    )
    val workDate: LocalDate,
    
    @Schema(
        description = "근무 시작 시간 (HH:mm 형식)",
        example = "09:00"
    )
    val startTime: LocalTime,
    
    @Schema(
        description = "근무 종료 시간 (HH:mm 형식)",
        example = "18:00"
    )
    val endTime: LocalTime,
    
    @Schema(
        description = "스태프 이름",
        example = "김직원"
    )
    val staffName: String,
    
    @Schema(
        description = "회사명 (달력에서 빨간색으로 표시됨)",
        example = "스타벅스 강남점"
    )
    val companyName: String,
    
    @Schema(
        description = "직책/포지션",
        example = "서빙"
    )
    val position: String,
    
    @Schema(
        description = "직무 유형",
        example = "서비스업"
    )
    val jobType: String,
    
    @Schema(
        description = "시급 (원 단위)",
        example = "12000"
    )
    val hourlyWage: Long,
    
    @Schema(
        description = "지급일 (매월 몇 일)",
        example = "10"
    )
    val paymentDate: String,
    
    @Schema(
        description = "근무 상태",
        example = "SCHEDULED",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"]
    )
    val status: WorkStatus
)

/**
 * 스케줄 상세 정보 DTO
 */
@Schema(description = "스케줄 상세 정보")
data class ScheduleDetailDto(
    @Schema(
        description = "스케줄 고유 ID",
        example = "65a1b2c3d4e5f6789abcdef0"
    )
    val id: String,
    
    @Schema(
        description = "지원서 ID (연관된 지원서)",
        example = "65a1b2c3d4e5f6789abcdef1"
    )
    val applicationId: String,
    
    @Schema(
        description = "채용공고 ID (연관된 채용공고)",
        example = "65a1b2c3d4e5f6789abcdef2"
    )
    val recruitId: String,
    
    @Schema(
        description = "스태프 ID (근무자)",
        example = "65a1b2c3d4e5f6789abcdef3"
    )
    val staffId: String,
    
    @Schema(
        description = "스태프 이름",
        example = "김직원"
    )
    val staffName: String,
    
    @Schema(
        description = "근무 날짜 (YYYY-MM-DD 형식)",
        example = "2024-01-15"
    )
    val workDate: LocalDate,
    
    @Schema(
        description = "근무 시작 시간 (HH:mm 형식)",
        example = "09:00"
    )
    val startTime: LocalTime,
    
    @Schema(
        description = "근무 종료 시간 (HH:mm 형식)",
        example = "18:00"
    )
    val endTime: LocalTime,
    
    @Schema(
        description = "직책/포지션",
        example = "서빙"
    )
    val position: String,
    
    @Schema(
        description = "직무 유형",
        example = "서비스업"
    )
    val jobType: String,
    
    @Schema(
        description = "근무 장소",
        example = "서울시 강남구 테헤란로 123"
    )
    val workLocation: String,
    
    @Schema(
        description = "회사명",
        example = "스타벅스 강남점"
    )
    val companyName: String,
    
    @Schema(
        description = "시급 (원 단위)",
        example = "12000"
    )
    val hourlyWage: Long,
    
    @Schema(
        description = "근무 상태",
        example = "SCHEDULED",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"]
    )
    val status: WorkStatus
)

/**
 * 스케줄 상태 업데이트 요청 DTO
 */
@Schema(description = "스케줄 상태 업데이트 요청")
data class ScheduleStatusUpdateDto(
    @Schema(
        description = "변경할 근무 상태. MANAGER 권한으로 수동 상태 변경 시 사용",
        example = "ABSENT",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val status: WorkStatus
)

/**
 * 대체 근무자 정보 DTO
 */
@Schema(description = "대체 근무자 채용공고 생성을 위한 정보")
data class ReplacementInfoDto(
    @Schema(
        description = "원본 채용공고 ID (대체 채용공고 생성 시 참조)",
        example = "65a1b2c3d4e5f6789abcdef2"
    )
    val recruitId: String,
    
    @Schema(
        description = "대체 채용공고 제목 (자동 생성)",
        example = "카페 서빙 스태프 모집 (대체근무)"
    )
    val title: String,
    
    @Schema(
        description = "근무 장소",
        example = "서울시 강남구 테헤란로 123"
    )
    val workLocation: String,
    
    @Schema(
        description = "대체 근무 날짜",
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
        description = "직책/포지션",
        example = "서빙"
    )
    val position: String,
    
    @Schema(
        description = "직무 유형",
        example = "서비스업"
    )
    val jobType: String,
    
    @Schema(
        description = "시급 (원 단위)",
        example = "12000"
    )
    val hourlyWage: Long,
    
    @Schema(
        description = "결근한 직원 이름 (대체 사유)",
        example = "김직원"
    )
    val absentStaffName: String
)

/**
 * 오늘의 근무 스케줄 DTO
 */
@Schema(description = "오늘의 근무 스케줄 및 체크인/체크아웃 가능 여부")
data class TodayScheduleDto(
    @Schema(
        description = "스케줄 고유 ID (체크인/체크아웃 시 필요)",
        example = "65a1b2c3d4e5f6789abcdef0"
    )
    val id: String,
    
    @Schema(
        description = "근무 날짜 (오늘 날짜)",
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
    val companyName: String,
    
    @Schema(
        description = "직책/포지션",
        example = "서빙"
    )
    val position: String,
    
    @Schema(
        description = "직무 유형",
        example = "서비스업"
    )
    val jobType: String,
    
    @Schema(
        description = "근무 장소",
        example = "서울시 강남구 테헤란로 123"
    )
    val workLocation: String,
    
    @Schema(
        description = "현재 근무 상태",
        example = "SCHEDULED",
        allowableValues = ["SCHEDULED", "PRESENT", "ABSENT", "LATE", "COMPLETED"]
    )
    val status: WorkStatus,
    
    @Schema(
        description = "출근 체크인 가능 여부 (true: 출근 가능, false: 불가능)",
        example = "true"
    )
    val canCheckIn: Boolean,
    
    @Schema(
        description = "퇴근 체크아웃 가능 여부 (true: 퇴근 가능, false: 불가능)",
        example = "false"
    )
    val canCheckOut: Boolean,
    
    @Schema(
        description = "현재 상태에 대한 사용자 친화적 메시지",
        example = "근무 시간입니다. 출근 체크를 해주세요."
    )
    val statusMessage: String
) 