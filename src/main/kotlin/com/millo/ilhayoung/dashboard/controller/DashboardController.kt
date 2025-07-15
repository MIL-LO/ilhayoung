package com.millo.ilhayoung.dashboard.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.dashboard.dto.EmployerDashboardResponse
import com.millo.ilhayoung.dashboard.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 대시보드 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "대시보드 관련 API")
class DashboardController(
    private val dashboardService: DashboardService
) {

    /**
     * 사업자 대시보드 데이터 조회
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 사업자 대시보드 데이터
     */
    @Operation(
        summary = "사업자 대시보드 조회",
        description = "사업자의 대시보드 데이터를 조회합니다. 출근 현황, 지원 현황, 급여 정보 등을 포함합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @GetMapping("/employer")
    fun getEmployerDashboard(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<EmployerDashboardResponse> {
        val dashboardData = dashboardService.getEmployerDashboard(userPrincipal.userId)
        return ApiResponse.success(dashboardData)
    }
} 