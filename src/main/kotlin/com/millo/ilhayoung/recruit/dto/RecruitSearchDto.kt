package com.millo.ilhayoung.recruit.dto

import com.millo.ilhayoung.recruit.domain.WorkPeriod
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * 채용공고 검색 요청
 */
@Schema(description = "채용공고 검색 요청")
data class RecruitSearchRequest(
    @Schema(description = "검색 키워드", example = "카페")
    val keyword: String? = null,

    @Schema(description = "지역 필터", example = "제주시")
    val location: String? = null,

    @Schema(description = "근무기간 필터")
    val workPeriod: WorkPeriod? = null,

    @Schema(description = "최소 급여", example = "9000")
    val minSalary: Long? = null,

    @Schema(description = "최대 급여", example = "15000")
    val maxSalary: Long? = null,

    @Schema(description = "직무 필터", example = "서빙")
    val jobType: String? = null,

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    val page: Int = 0,

    @Schema(description = "페이지 크기", example = "20")
    val size: Int = 20,

    @Schema(description = "정렬 기준", allowableValues = ["salary", "workPeriod", "createdAt", "deadline"])
    val sortBy: String = "createdAt",

    @Schema(description = "정렬 방향", allowableValues = ["asc", "desc"])
    val sortDirection: String = "desc"
) {
    fun toPageable(): Pageable {
        val direction = if (sortDirection.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sort = Sort.by(direction, sortBy)
        return PageRequest.of(page, size, sort)
    }
}

/**
 * 지원 현황 요약
 */
@Schema(description = "지원 현황 요약")
data class ApplicationSummaryResponse(
    @Schema(description = "공고 ID")
    val recruitId: String,

    @Schema(description = "공고 제목")
    val recruitTitle: String,

    @Schema(description = "총 지원자 수")
    val totalApplications: Int,

    @Schema(description = "검토중 지원자 수")
    val reviewingCount: Int,

    @Schema(description = "면접 요청 지원자 수")
    val interviewCount: Int,

    @Schema(description = "채용 확정 지원자 수")
    val hiredCount: Int,

    @Schema(description = "공고 등록일")
    val createdAt: java.time.LocalDateTime,

    @Schema(description = "공고 마감일")
    val deadline: java.time.LocalDateTime
) 