package com.millo.ilhayoung.attendance.controller

import com.millo.ilhayoung.attendance.dto.*
import com.millo.ilhayoung.attendance.service.AttendanceService
import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Attendance",
    description = "출근 관리 API - 근로자 출석 현황 조회 및 체크인/체크아웃 관리"
)
@RestController
@RequestMapping("/api/attendances")
@SecurityRequirement(name = "BearerAuth")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    @Operation(
        summary = "[MANAGER] 전체 근로자 출석 현황 조회",
        description = """
        관리자가 모든 근로자의 출석 현황을 한눈에 볼 수 있는 대시보드 정보를 제공합니다.
        
        **제공 정보:**
        - 전체/출근/결근/지각 근로자 수 통계
        - 각 근로자별 상세 정보 (오늘 상태, 근무 시간, 주/월 누적 근무시간)
        
        **근무 시간 계산:**
        - 주간 근무시간: 이번 주 월요일부터 현재까지
        - 월간 근무시간: 이번 달 1일부터 현재까지
        - 시간은 분 단위로 제공 (예: 480분 = 8시간)
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** MANAGER만 조회 가능
        """
    )
    @GetMapping("/overview")
    @PreAuthorize("hasRole('MANAGER')")
    fun getWorkersOverview(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<WorkerOverviewDto>> {
        val overview = attendanceService.getWorkersOverview()
        return ResponseEntity.ok(
            ApiResponse.success(overview)
        )
    }

    @Operation(
        summary = "[MANAGER/STAFF] 특정 스태프 상세 정보 조회",
        description = """
        특정 스태프의 상세한 근무 정보를 조회합니다.
        
        **포함 정보:**
        - 오늘의 근무 상태와 시간
        - 근무 장소 정보
        - 주간/월간 누적 근무시간
        
        **사용 목적:**
        - 관리자가 특정 직원의 상세 근무 현황 확인
        - 근무시간 관리 및 급여 계산 참고자료
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** MANAGER 또는 본인만 조회 가능
        """
    )
    @GetMapping("/staff/{staffId}/detail")
    @PreAuthorize("hasRole('MANAGER') or #staffId == principal.userId")
    fun getStaffDetail(
        @Parameter(
            description = "조회할 스태프의 고유 ID",
            required = true,
            example = "65a1b2c3d4e5f6789abcdef3"
        )
        @PathVariable staffId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<StaffDetailDto>> {
        val staffDetail = attendanceService.getStaffDetail(staffId)
        return ResponseEntity.ok(
            ApiResponse.success(staffDetail)
        )
    }

    @Operation(
        summary = "[MANAGER] 스태프 근무 상태 수정",
        description = """
        관리자가 특정 스태프의 근무 상태를 수동으로 변경합니다.
        
        **변경 가능한 상태:**
        - SCHEDULED: 근무 예정
        - PRESENT: 출근 (정상 출근)
        - ABSENT: 결근
        - LATE: 지각
        - COMPLETED: 근무 완료
        
        **사용 사례:**
        - 직원의 사전 결근 신고 처리
        - 체크인/체크아웃 시스템 오류 시 수동 처리
        - 출석 상태 정정
        - 휴가/병가 등 특수 상황 처리
        
        **주의사항:**
        - 오늘 날짜의 스케줄에 대해서만 상태 변경 가능
        - 이미 COMPLETED 상태인 경우 변경 제한
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** MANAGER만 실행 가능
        """
    )
    @PutMapping("/staff/{staffId}/status")
    @PreAuthorize("hasRole('MANAGER')")
    fun updateStaffStatus(
        @Parameter(
            description = "상태를 변경할 스태프의 고유 ID",
            required = true,
            example = "65a1b2c3d4e5f6789abcdef3"
        )
        @PathVariable staffId: String,
        
        @Parameter(
            description = "변경할 근무 상태 정보",
            required = true
        )
        @RequestBody request: StaffStatusUpdateDto,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<String>> {
        attendanceService.updateStaffStatus(staffId, request.status)
        return ResponseEntity.ok(
            ApiResponse.success("스태프 상태가 성공적으로 수정되었습니다.")
        )
    }

    @Operation(
        summary = "[STAFF] 나의 상세 근무 정보 조회",
        description = """
        현재 로그인한 스태프의 상세 근무 정보를 조회합니다.
        
        **포함 정보:**
        - 오늘의 근무 상태 및 시간
        - 현재 근무 장소
        - 이번 주/달 누적 근무시간
        - 이번 주 예정된 스케줄 목록
        
        **근무시간 활용:**
        - 급여 계산 참고
        - 근로기준법 준수 확인 (주 52시간 등)
        - 개인 근무 패턴 파악
        
        **사용 ENUM:**
        - **WorkStatus**: 근무 상태
          - `SCHEDULED`: 예정 (근무 예정)
          - `PRESENT`: 출근 (정상 출근)
          - `ABSENT`: 결근 (결근)
          - `LATE`: 지각 (지각)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** STAFF만 조회 가능 (본인 정보만)
        """
    )
    @GetMapping("/my-detail")
    @PreAuthorize("hasRole('STAFF')")
    fun getMyDetail(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<StaffDetailDto>> {
        val myDetail = attendanceService.getStaffDetail(userPrincipal.userId)
        return ResponseEntity.ok(
            ApiResponse.success(myDetail)
        )
    }

    @Operation(
        summary = "[STAFF] 체크인/체크아웃 처리",
        description = """
        스태프가 출근(체크인) 또는 퇴근(체크아웃)을 처리합니다.
        
        **자동 상태 결정 규칙 (한국 시간 기준):**
        
        **체크인 시:**
        - 09:00 이전: PRESENT (정상 출근)
        - 09:01 이후: LATE (지각)
        - 18:00 이후: ABSENT (결근 처리)
        
        **체크아웃 시:**
        - 이미 체크인한 상태에서만 가능
        - 18:00 이전이라도 체크아웃 가능
        - 체크아웃 완료 시 COMPLETED 상태로 변경
        
        **사용 절차:**
        1. `GET /api/schedules/today`로 오늘 스케줄 및 체크 가능 여부 확인
        2. 체크 가능한 경우 해당 API 호출
        3. 응답으로 받은 상태와 메시지 확인
        
        **사용 ENUM:**
        - **CheckType**: 체크 유형
          - `CHECK_IN`: 출근 체크인
          - `CHECK_OUT`: 퇴근 체크아웃
        - **WorkStatus**: 근무 상태 (응답에서 반환)
          - `PRESENT`: 출근 (정상 출근)
          - `LATE`: 지각 (지각)
          - `ABSENT`: 결근 (결근)
          - `COMPLETED`: 완료 (근무 완료)
        
        **권한:** STAFF만 실행 가능 (본인 스케줄만)
        """
    )
    @PostMapping("/check-in-out")
    @PreAuthorize("hasRole('STAFF')")
    fun processCheckInOut(
        @Parameter(
            description = "체크인/체크아웃 요청 정보",
            required = true
        )
        @RequestBody request: CheckInOutRequestDto,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<CheckInOutResponseDto>> {
        val result = attendanceService.processCheckInOut(
            userId = userPrincipal.userId,
            scheduleId = request.scheduleId,
            checkType = request.checkType
        )
        return ResponseEntity.ok(
            ApiResponse.success(result)
        )
    }
} 