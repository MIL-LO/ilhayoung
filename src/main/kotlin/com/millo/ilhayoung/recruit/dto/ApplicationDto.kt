package com.millo.ilhayoung.recruit.dto

import com.millo.ilhayoung.recruit.domain.Application
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 지원 요청
 */
@Schema(description = "지원 요청", example = """
{
  "name": "이제주",
  "birthDate": "1995-03-15",
  "contact": "010-1234-5678",
  "address": "제주시 노형동 456-78",
  "experience": "한식 주점 홀 아르바이트 3개월, 패스트푸드점 카운터 6개월",
  "climateScore": 85
}
""")
data class CreateApplicationRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 20, message = "이름은 20자 이내여야 합니다")
    @Schema(description = "이름", example = "김제주")
    val name: String,

    @field:NotNull(message = "생년월일은 필수입니다")
    @field:Past(message = "생년월일은 과거 날짜여야 합니다")
    @Schema(description = "생년월일", example = "1995-03-15")
    val birthDate: LocalDate,

    @field:NotBlank(message = "연락처는 필수입니다")
    @field:Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    @Schema(description = "연락처", example = "010-1234-5678")
    val contact: String,

    @field:NotBlank(message = "주소는 필수입니다")
    @Schema(description = "주소", example = "제주시 노형동 123-45")
    val address: String,

    @field:NotBlank(message = "경력 또는 관련 경험은 필수입니다")
    @field:Size(max = 500, message = "경력은 500자 이내여야 합니다")
    @Schema(description = "경력 또는 관련 경험", example = "한식 주점 홀 아르바이트 3개월")
    val experience: String,

    @field:Min(value = 0, message = "오름지수는 0 이상이어야 합니다")
    @field:Max(value = 100, message = "오름지수는 100 이하여야 합니다")
    @Schema(description = "오름지수", example = "85")
    val climateScore: Int? = null
)

/**
 * 지원 응답
 */
@Schema(description = "지원 응답")
data class ApplicationResponse(
    @Schema(description = "지원 ID")
    val id: String,

    @Schema(description = "공고 ID")
    val recruitId: String,

    @Schema(description = "지원자 ID")
    val staffId: String,

    @Schema(description = "이름")
    val name: String,

    @Schema(description = "생년월일")
    val birthDate: LocalDate,

    @Schema(description = "연락처")
    val contact: String,

    @Schema(description = "주소")
    val address: String,

    @Schema(description = "경력 또는 관련 경험")
    val experience: String,

    @Schema(description = "오름지수")
    val climateScore: Int?,

    @Schema(description = "지원 상태")
    val status: ApplicationStatus,

    @Schema(description = "지원일시")
    val createdAt: LocalDateTime,

    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(application: Application): ApplicationResponse {
            return ApplicationResponse(
                id = application.id!!,
                recruitId = application.recruitId,
                staffId = application.staffId,
                name = application.name,
                birthDate = application.birthDate,
                contact = application.contact,
                address = application.address,
                experience = application.experience,
                climateScore = application.climateScore,
                status = application.status,
                createdAt = application.createdAt!!,
                updatedAt = application.updatedAt!!
            )
        }
    }
}

/**
 * 지원 내역 응답 (공고 정보 포함)
 */
@Schema(description = "지원 내역 응답")
data class ApplicationHistoryResponse(
    @Schema(description = "지원 ID")
    val id: String,

    @Schema(description = "공고 제목")
    val recruitTitle: String,

    @Schema(description = "기업명")
    val companyName: String,

    @Schema(description = "지원 상태")
    val status: ApplicationStatus,

    @Schema(description = "지원일시")
    val appliedAt: LocalDateTime,

    @Schema(description = "공고 마감일")
    val recruitDeadline: LocalDateTime
)

/**
 * 지원자 정보 응답 (Manager용)
 */
@Schema(description = "지원자 정보 응답")
data class ApplicantInfoResponse(
    @Schema(description = "지원 ID")
    val id: String,

    @Schema(description = "이름")
    val name: String,

    @Schema(description = "생년월일")
    val birthDate: LocalDate,

    @Schema(description = "연락처")
    val contact: String,

    @Schema(description = "주소")
    val address: String,

    @Schema(description = "경력 또는 관련 경험")
    val experience: String,

    @Schema(description = "오름지수")
    val climateScore: Int?,

    @Schema(description = "지원 상태")
    val status: ApplicationStatus,

    @Schema(description = "지원일시")
    val appliedAt: LocalDateTime
) {
    companion object {
        fun from(application: Application): ApplicantInfoResponse {
            return ApplicantInfoResponse(
                id = application.id!!,
                name = application.name,
                birthDate = application.birthDate,
                contact = application.contact,
                address = application.address,
                experience = application.experience,
                climateScore = application.climateScore,
                status = application.status,
                appliedAt = application.createdAt!!
            )
        }
    }
}

/**
 * 지원 상태 변경 요청
 */
@Schema(description = "지원 상태 변경 요청", example = """
{
  "status": "INTERVIEW"
}
""")
data class UpdateApplicationStatusRequest(
    @field:NotNull(message = "상태는 필수입니다")
    @Schema(description = "지원 상태", example = "INTERVIEW", allowableValues = ["APPLIED", "REVIEWING", "INTERVIEW", "HIRED", "REJECTED"])
    val status: ApplicationStatus
)

/**
 * 지원 현황 요약 응답
 */
@Schema(description = "지원 현황 요약 응답")
data class ApplicationSummaryResponse(
    @Schema(description = "채용공고 ID")
    val recruitId: String,

    @Schema(description = "채용공고 제목")
    val recruitTitle: String,

    @Schema(description = "총 지원자 수")
    val totalApplications: Long,

    @Schema(description = "서류 검토 중인 지원자 수")
    val reviewingCount: Long,

    @Schema(description = "면접 중인 지원자 수")
    val interviewCount: Long,

    @Schema(description = "채용된 지원자 수")
    val hiredCount: Long,

    @Schema(description = "공고 등록일")
    val createdAt: LocalDateTime,

    @Schema(description = "공고 마감일")
    val deadline: LocalDateTime
) 