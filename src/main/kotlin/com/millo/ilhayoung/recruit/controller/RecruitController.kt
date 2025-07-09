package com.millo.ilhayoung.recruit.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.recruit.dto.*
import com.millo.ilhayoung.recruit.service.RecruitService

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
 * 채용공고 컨트롤러
 */
@Tag(name = "채용공고 API", description = "채용공고 관련 API")
@RestController
@RequestMapping("/api/v1/recruits")
@SecurityRequirement(name = "BearerAuth")
class RecruitController(
    private val recruitService: RecruitService
) {

    @Operation(
        summary = "채용공고 등록",
        description = "Manager가 새로운 채용공고를 등록합니다."
    )
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    fun createRecruit(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateRecruitRequest
    ): ResponseEntity<ApiResponse<RecruitResponse>> {
        val response = recruitService.createRecruit(userPrincipal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 목록 조회",
        description = "채용공고 목록을 조회합니다. 필터링 및 검색이 가능합니다."
    )
    @GetMapping
    fun getRecruits(
        @Parameter(description = "검색 키워드") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "지역 필터") @RequestParam(required = false) location: String?,
        @Parameter(description = "근무기간 필터") @RequestParam(required = false) workPeriod: com.millo.ilhayoung.recruit.domain.WorkPeriod?,
        @Parameter(description = "최소 급여") @RequestParam(required = false) minSalary: Long?,
        @Parameter(description = "최대 급여") @RequestParam(required = false) maxSalary: Long?,
        @Parameter(description = "직무 필터") @RequestParam(required = false) jobType: String?,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<ApiResponse<Page<RecruitSummaryResponse>>> {
        val searchRequest = RecruitSearchRequest(
            keyword = keyword,
            location = location,
            workPeriod = workPeriod,
            minSalary = minSalary,
            maxSalary = maxSalary,
            jobType = jobType,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        
        val response = recruitService.getRecruits(searchRequest)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 상세 조회",
        description = "특정 채용공고의 상세 정보를 조회합니다."
    )
    @GetMapping("/{recruitId}")
    fun getRecruit(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String
    ): ResponseEntity<ApiResponse<RecruitResponse>> {
        val response = recruitService.getRecruit(recruitId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "내가 작성한 채용공고 목록 조회",
        description = "로그인한 Manager가 작성한 채용공고 목록을 조회합니다.",
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    fun getMyRecruits(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<RecruitSummaryResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = recruitService.getMyRecruits(userPrincipal.userId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 수정",
        description = "채용공고를 수정합니다. 작성자만 수정 가능합니다.",
    )
    @PutMapping("/{recruitId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun updateRecruit(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: UpdateRecruitRequest
    ): ResponseEntity<ApiResponse<RecruitResponse>> {
        val response = recruitService.updateRecruit(recruitId, userPrincipal.userId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 상태 변경",
        description = "채용공고의 상태를 변경합니다. 작성자만 변경 가능합니다.",
    )
    @PatchMapping("/{recruitId}/status")
    @PreAuthorize("hasRole('MANAGER')")
    fun updateRecruitStatus(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: UpdateRecruitStatusRequest
    ): ResponseEntity<ApiResponse<RecruitResponse>> {
        val response = recruitService.updateRecruitStatus(recruitId, userPrincipal.userId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "채용공고 삭제",
        description = "채용공고를 삭제합니다. 작성자만 삭제 가능하며, 지원자가 있는 경우 삭제할 수 없습니다.",
    )
    @DeleteMapping("/{recruitId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun deleteRecruit(
        @Parameter(description = "채용공고 ID") @PathVariable recruitId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit>> {
        recruitService.deleteRecruit(recruitId, userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @Operation(
        summary = "인기/추천 채용공고 조회",
        description = "조회수가 높은 인기 채용공고를 조회합니다."
    )
    @GetMapping("/featured")
    fun getFeaturedRecruits(
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<RecruitSummaryResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = recruitService.getFeaturedRecruits(pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "지원 현황 요약 조회",
        description = "Manager의 모든 채용공고에 대한 지원 현황을 요약하여 조회합니다.",
    )
    @GetMapping("/applications/summary")
    @PreAuthorize("hasRole('MANAGER')")
    fun getApplicationSummary(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<ApplicationSummaryResponse>>> {
        val response = recruitService.getApplicationSummary(userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
} 