package com.millo.ilhayoung.user.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.user.dto.*
import com.millo.ilhayoung.user.service.UserDashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "EmployerDashboard", description = "사업자 대시보드 API")
@RestController
@RequestMapping("/api/v1/employer/dashboard")
class UserDashboardController(
    private val dashboardService: UserDashboardService
) {
    @Operation(summary = "대시보드 요약", security = [SecurityRequirement(name = "BearerAuth")])
    @GetMapping("/summary")
    fun getSummary(@AuthenticationPrincipal user: UserPrincipal): ApiResponse<DashboardSummaryDto> {
        return ApiResponse.success(dashboardService.getSummary(user.userId))
    }

    @Operation(summary = "최근 활동", security = [SecurityRequirement(name = "BearerAuth")])
    @GetMapping("/activities")
    fun getActivities(@AuthenticationPrincipal user: UserPrincipal): ApiResponse<List<DashboardActivityDto>> {
        return ApiResponse.success(dashboardService.getActivities(user.userId))
    }

    @Operation(summary = "오늘의 할 일", security = [SecurityRequirement(name = "BearerAuth")])
    @GetMapping("/tasks")
    fun getTasks(@AuthenticationPrincipal user: UserPrincipal): ApiResponse<List<DashboardTaskDto>> {
        return ApiResponse.success(dashboardService.getTasks(user.userId))
    }
} 