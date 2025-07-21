package com.millo.ilhayoung.attendance.controller

import com.millo.ilhayoung.attendance.dto.*
import com.millo.ilhayoung.attendance.service.ScheduleService
import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(
    name = "스케줄 관리", description = "근무 스케줄 관련 API"
)
@RestController
@RequestMapping("/api/v1/schedules")
@SecurityRequirement(name = "BearerAuth")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @Operation(
        summary = "[MANAGER] 지원서 승인 시 스케줄 자동 생성",
        description = """
        지원서가 승인될 때 채용공고의 근무 일정(WorkSchedule)을 기반으로 자동으로 스케줄을 생성합니다.
        
        **생성 규칙:**
        - WorkSchedule의 요일과 시간을 기반으로 반복 일정 생성
        - WorkPeriod에 따라 생성 기간 결정 (ONE_WEEK: 7일, ONE_MONTH: 30일, THREE_MONTHS: 90일)
        - 초기 상태는 모두 SCHEDULED로 설정
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정) - 초기 생성 시 기본값
        
        **권한:** MANAGER만 실행 가능
        """
    )
    @PostMapping("/applications/{applicationId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun createSchedulesFromApplication(
        @Parameter(
            description = "스케줄을 생성할 지원서의 고유 ID",
            required = true,
            example = "65a1b2c3d4e5f6789abcdef1"
        )
        @PathVariable applicationId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<String>> {
        scheduleService.createSchedulesFromApplication(applicationId)
        return ResponseEntity.ok(
            ApiResponse.success("스케줄이 성공적으로 생성되었습니다.")
        )
    }

    @Operation(
        summary = "[MANAGER/STAFF] 월별 스케줄 조회 (달력용)",
        description = """
        지정된 년월의 스케줄을 조회하여 달력 형태로 표시할 수 있는 데이터를 반환합니다.
        
        **조회 범위:**
        - STAFF: 본인의 스케줄만 조회
        - MANAGER: 모든 스태프의 스케줄 조회
        
        **반환 정보:**
        - 달력에 표시할 기본 정보 (날짜, 시간, 회사명, 상태)
        - 회사명은 달력에서 빨간색으로 강조 표시됨
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        """
    )
    @GetMapping
    fun getMonthlySchedules(
        @Parameter(
            description = "조회할 연도 (4자리 숫자)",
            required = true,
            example = "2024"
        )
        @RequestParam year: Int,
        
        @Parameter(
            description = "조회할 월 (1-12)",
            required = true,
            example = "1"
        )
        @RequestParam month: Int,
        
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<MonthlyScheduleDto>>> {
        val schedules = scheduleService.getMonthlySchedules(
            year = year, 
            month = month, 
            userId = userPrincipal.userId,
            userType = userPrincipal.userType?.name
        )
        return ResponseEntity.ok(
            ApiResponse.success(schedules)
        )
    }

    @Operation(
        summary = "[MANAGER/STAFF] 스케줄 상세 정보 조회",
        description = """
        특정 스케줄의 상세 정보를 조회합니다. 달력에서 특정 일정을 클릭했을 때 표시되는 상세 정보입니다.
        
        **포함 정보:**
        - 근무자 정보 (이름, ID)
        - 근무 일시 및 장소
        - 회사 정보 및 직책
        - 시급 정보
        - 현재 근무 상태
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:**
        - STAFF: 본인 스케줄만 조회 가능
        - MANAGER: 모든 스케줄 조회 가능
        """
    )
    @GetMapping("/{scheduleId}")
    fun getScheduleDetail(
        @Parameter(
            description = "조회할 스케줄의 고유 ID",
            required = true,
            example = "65a1b2c3d4e5f6789abcdef0"
        )
        @PathVariable scheduleId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<ScheduleDetailDto>> {
        val schedule = scheduleService.getScheduleDetail(scheduleId, userPrincipal.userId)
        return ResponseEntity.ok(
            ApiResponse.success(schedule)
        )
    }

    /**
     * 스케줄 상태 수정
     */
    @PutMapping("/{scheduleId}/status")
    fun updateScheduleStatus(
        @PathVariable scheduleId: String,
        @RequestBody request: ScheduleStatusUpdateDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ScheduleDetailDto>> {
        val userId = authentication.name
        val updatedSchedule = scheduleService.updateScheduleStatus(scheduleId, request.status)
        val scheduleDetail = scheduleService.getScheduleDetail(scheduleId, userId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                message = "스케줄 상태가 변경되었습니다.",
                data = scheduleDetail
            )
        )
    }

    @Operation(
        summary = "[STAFF] 오늘의 근무 스케줄 조회",
        description = """
        현재 로그인한 사용자의 오늘 근무 스케줄을 조회하고 체크인/체크아웃 가능 여부를 확인합니다.
        
        **체크인/체크아웃 규칙:**
        - 체크인 가능: 근무일이고 아직 체크인하지 않은 경우
        - 체크아웃 가능: 이미 체크인했고 아직 체크아웃하지 않은 경우
        
        **상태 메시지 예시:**
        - "근무 시간입니다. 출근 체크를 해주세요."
        - "근무 중입니다. 퇴근 체크를 해주세요."
        - "오늘 근무가 완료되었습니다."
        - "오늘은 근무일이 아닙니다."
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** STAFF만 조회 가능 (본인 스케줄만)
        """
    )
    @GetMapping("/today")
    @PreAuthorize("hasRole('STAFF')")
    fun getTodaySchedule(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<TodayScheduleDto>> {
        val todaySchedule = scheduleService.getTodaySchedule(userPrincipal.userId)
        return ResponseEntity.ok(
            ApiResponse.success(todaySchedule)
        )
    }
} 