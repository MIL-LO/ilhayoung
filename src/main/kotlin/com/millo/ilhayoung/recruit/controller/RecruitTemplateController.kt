package com.millo.ilhayoung.recruit.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.recruit.dto.CreateTemplateRequest
import com.millo.ilhayoung.recruit.dto.TemplateResponse
import com.millo.ilhayoung.recruit.service.RecruitTemplateService
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
 * 채용공고 템플릿 컨트롤러
 */
@Tag(name = "채용공고 템플릿 API", description = "채용공고 템플릿 관련 API")
@RestController
@RequestMapping("/api/v1/recruits/templates")
class RecruitTemplateController(
    private val templateService: RecruitTemplateService
) {

    @Operation(
        summary = "템플릿 생성",
        description = "Manager가 새로운 채용공고 템플릿을 생성합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    fun createTemplate(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateTemplateRequest
    ): ResponseEntity<ApiResponse<TemplateResponse>> {
        val response = templateService.createTemplate(userPrincipal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @Operation(
        summary = "내 템플릿 목록 조회",
        description = "Manager가 자신의 템플릿 목록을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    fun getMyTemplates(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<TemplateResponse>>> {
        val pageable = PageRequest.of(page, size)
        val response = templateService.getMyTemplates(userPrincipal.userId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "내 모든 템플릿 조회",
        description = "Manager가 자신의 모든 템플릿을 조회합니다. (페이징 없음)",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    fun getAllMyTemplates(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<TemplateResponse>>> {
        val response = templateService.getAllMyTemplates(userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "템플릿 상세 조회",
        description = "특정 템플릿의 상세 정보를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun getTemplate(
        @Parameter(description = "템플릿 ID") @PathVariable templateId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<TemplateResponse>> {
        val response = templateService.getTemplate(templateId, userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "템플릿 수정",
        description = "템플릿을 수정합니다. 템플릿 작성자만 수정 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun updateTemplate(
        @Parameter(description = "템플릿 ID") @PathVariable templateId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateTemplateRequest
    ): ResponseEntity<ApiResponse<TemplateResponse>> {
        val response = templateService.updateTemplate(templateId, userPrincipal.userId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "템플릿 삭제",
        description = "템플릿을 삭제합니다. 템플릿 작성자만 삭제 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('MANAGER')")
    fun deleteTemplate(
        @Parameter(description = "템플릿 ID") @PathVariable templateId: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit>> {
        templateService.deleteTemplate(templateId, userPrincipal.userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @Operation(
        summary = "템플릿 이름으로 검색",
        description = "템플릿 이름으로 검색합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    fun searchTemplatesByName(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "검색할 템플릿 이름") @RequestParam name: String
    ): ResponseEntity<ApiResponse<List<TemplateResponse>>> {
        val response = templateService.searchTemplatesByName(userPrincipal.userId, name)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
} 