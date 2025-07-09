package com.millo.ilhayoung.recruit.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import com.millo.ilhayoung.recruit.dto.*
import com.millo.ilhayoung.recruit.service.ApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 지원 컨트롤러
 */
@Tag(name = "지원 API", description = "채용공고 지원 관련 API")
@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "BearerAuth")
class ApplicationController(
    private val applicationService: ApplicationService
) {

    @Operation(
        summary = "채용공고 지원",
        description = "Staff가 특정 채용공고에 지원합니다."
    )
    @PostMapping("/recruits/{recruitId}/applications")
    @PreAuthorize("hasRole('STAFF')")
    fun applyToRecruit(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<ApiResponse<ApplicationResponse>> {
        val response = applicationService.applyToRecruit(recruitId, userPrincipal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @Operation(
        summary = "내 지원 내역 조회",
        description = "Staff가 자신의 지원 내역을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('STAFF')")
    fun getMyApplications(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ApplicationHistoryResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = applicationService.getMyApplications(userPrincipal.userId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 지원자 목록 조회",
        description = "Manager가 특정 채용공고의 지원자 목록을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/recruits/{recruitId}/applications")
    @PreAuthorize("hasRole('MANAGER')")
    fun getRecruitApplications(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ApplicantInfoResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = applicationService.getRecruitApplications(recruitId, userPrincipal.userId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "특정 상태의 지원자 목록 조회",
        description = "Manager가 특정 채용공고의 특정 상태 지원자 목록을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/recruits/{recruitId}/applications/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    fun getRecruitApplicationsByStatus(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @Parameter(description = "지원 상태") @PathVariable status: ApplicationStatus,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ApplicantInfoResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = applicationService.getRecruitApplicationsByStatus(
            recruitId, userPrincipal.userId, status, pageable
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "지원 상태 변경",
        description = "Manager가 지원자의 상태를 변경합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('MANAGER')")
    fun updateApplicationStatus(
        @Parameter(description = "지원 ID") @PathVariable applicationId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: UpdateApplicationStatusRequest
    ): ResponseEntity<ApiResponse<ApplicationResponse>> {
        val response = applicationService.updateApplicationStatus(
            applicationId, userPrincipal.userId, request
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "지원 취소",
        description = "Staff가 자신의 지원을 취소합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @DeleteMapping("/applications/{applicationId}")
    @PreAuthorize("hasRole('STAFF')")
    fun cancelApplication(
        @Parameter(description = "지원 ID") @PathVariable applicationId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit>> {
        applicationService.cancelApplication(applicationId, userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @Operation(
        summary = "지원서 상세 조회",
        description = "지원서의 상세 정보를 조회합니다. 지원자 본인 또는 해당 공고의 Manager만 조회 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    fun getApplication(
        @Parameter(description = "지원 ID") @PathVariable applicationId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<ApplicationResponse>> {
        val response = applicationService.getApplication(applicationId, userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
} 