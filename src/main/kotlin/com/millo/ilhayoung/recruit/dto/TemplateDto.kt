package com.millo.ilhayoung.recruit.dto

import com.millo.ilhayoung.recruit.domain.RecruitTemplate
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 템플릿 생성 요청
 */
@Schema(description = "템플릿 생성 요청", example = """
{
  "name": "카페 홀 스태프 기본 템플릿",
  "title": "카페 홀 스태프 모집",
  "workSchedule": {
    "days": ["월", "화", "수", "목", "금"],
    "startTime": "09:00",
    "endTime": "18:00",
    "workPeriod": "ONE_TO_THREE"
  }
}
""")
data class CreateTemplateRequest(
    @field:NotBlank(message = "템플릿 이름은 필수입니다")
    @field:Size(max = 50, message = "템플릿 이름은 50자 이내여야 합니다")
    @Schema(description = "템플릿 이름", example = "카페 홀 스태프 기본")
    val name: String,

    @field:NotBlank(message = "공고 제목 템플릿은 필수입니다")
    @field:Size(max = 100, message = "공고 제목은 100자 이내여야 합니다")
    @Schema(description = "공고 제목 템플릿", example = "카페 홀 스태프 모집")
    val title: String,

    @field:Valid
    @Schema(description = "근무 스케줄")
    val workSchedule: WorkScheduleDto
)

/**
 * 템플릿 응답
 */
@Schema(description = "템플릿 응답")
data class TemplateResponse(
    @Schema(description = "템플릿 ID")
    val id: String,

    @Schema(description = "템플릿 이름")
    val name: String,

    @Schema(description = "공고 제목 템플릿")
    val title: String,

    @Schema(description = "근무 스케줄")
    val workSchedule: WorkScheduleDto,

    @Schema(description = "생성일")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(template: RecruitTemplate): TemplateResponse {
            return TemplateResponse(
                id = template.id!!,
                name = template.name,
                title = template.title,
                workSchedule = WorkScheduleDto(
                    days = template.workSchedule.days,
                    startTime = template.workSchedule.startTime,
                    endTime = template.workSchedule.endTime,
                    workPeriod = template.workSchedule.workPeriod
                ),
                createdAt = template.createdAt!!
            )
        }
    }
} 