package com.millo.ilhayoung.salary.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.salary.dto.*
import com.millo.ilhayoung.salary.service.SalaryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Salary", description = "급여 관리 API")
@RestController
@RequestMapping("/api/v1/salaries")
class SalaryController(
    private val salaryService: SalaryService
) {

    @Operation(summary = "내 예상 급여 조회", description = "STAFF가 현재 월 예상 급여를 조회합니다.")
    @GetMapping("/my-estimated")
    @PreAuthorize("hasRole('STAFF')")
    fun getMyEstimatedSalary(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<EstimatedSalaryDto>> {
        val estimatedSalary = salaryService.getEstimatedSalary(userPrincipal.userId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = estimatedSalary,
                message = "예상 급여를 조회했습니다."
            )
        )
    }

    @Operation(summary = "내 급여 이력 조회", description = "STAFF가 본인의 급여 이력을 조회합니다.")
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('STAFF')")
    fun getMyPayrollHistory(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<PayrollHistoryDto>>> {
        val payrollHistory = salaryService.getPayrollHistory(userPrincipal.userId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = payrollHistory,
                message = "급여 이력을 조회했습니다."
            )
        )
    }

    @Operation(summary = "스태프 급여 정보 조회", description = "MANAGER가 특정 스태프의 급여 정보를 조회합니다.")
    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun getStaffSalary(
        @PathVariable staffId: String
    ): ResponseEntity<ApiResponse<EstimatedSalaryDto>> {
        val estimatedSalary = salaryService.getEstimatedSalary(staffId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = estimatedSalary,
                message = "스태프 급여 정보를 조회했습니다."
            )
        )
    }

    @Operation(summary = "급여 상세 정보 조회", description = "급여 기록의 상세 정보를 조회합니다.")
    @GetMapping("/payrolls/{payrollRecordId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    fun getPayrollDetail(
        @PathVariable payrollRecordId: String
    ): ResponseEntity<ApiResponse<PayrollDetailDto>> {
        val payrollDetail = salaryService.getPayrollDetail(payrollRecordId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = payrollDetail,
                message = "급여 상세 정보를 조회했습니다."
            )
        )
    }

    @Operation(summary = "급여 계산", description = "MANAGER가 급여를 계산하고 생성합니다.")
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('MANAGER')")
    fun calculatePayroll(
        @RequestBody requestDto: PayrollCalculationRequestDto
    ): ResponseEntity<ApiResponse<List<String>>> {
        val payrollRecords = salaryService.calculatePayroll(requestDto)
        val payrollRecordIds = payrollRecords.map { it.id!! }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = payrollRecordIds,
                message = "급여가 계산되었습니다."
            )
        )
    }

    @Operation(summary = "급여 승인", description = "MANAGER가 계산된 급여를 승인합니다.")
    @PostMapping("/approve")
    @PreAuthorize("hasRole('MANAGER')")
    fun approvePayrolls(
        @RequestBody approvalDto: PayrollApprovalDto
    ): ResponseEntity<ApiResponse<String>> {
        salaryService.approvePayrolls(approvalDto)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = "SUCCESS",
                message = "급여가 승인되었습니다."
            )
        )
    }

    @Operation(summary = "매니저 급여 기록 조회", description = "MANAGER가 관리하는 모든 급여 기록을 조회합니다.")
    @GetMapping("/manager-records")
    @PreAuthorize("hasRole('MANAGER')")
    fun getManagerPayrollRecords(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<PayrollHistoryDto>>> {
        val payrollRecords = salaryService.getManagerPayrollRecords(userPrincipal.userId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                data = payrollRecords,
                message = "매니저 급여 기록을 조회했습니다."
            )
        )
    }
} 