package com.millo.ilhayoung.recruit.dto

import com.millo.ilhayoung.recruit.domain.*
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * 채용공고 등록 요청
 */
@Schema(description = "채용공고 등록 요청", example = """
{
  "title": "제주 연동 카페 홀 스태프 모집",
  "workLocation": "제주시 연동",
  "salary": 10000,
  "jobType": "서빙",
  "position": "홀 스태프",
  "workSchedule": {
    "days": ["월", "화", "수", "목", "금"],
    "startTime": "09:00",
    "endTime": "18:00",
    "workPeriod": "ONE_TO_THREE"
  },
  "gender": "무관",
  "description": "친절하고 밝은 성격의 홀 스태프를 모집합니다. 카페 운영 경험이 있으시면 우대합니다.",
  "images": ["https://example.com/cafe1.jpg"],
  "deadline": "2025-02-15T23:59:59",
  "paymentDate": "매월 25일",
  "companyName": "제주 힐링 카페",
  "companyAddress": "제주시 연동 123-45",
  "companyContact": "064-123-4567",
  "representativeName": "김제주"
}
""")
data class CreateRecruitRequest(
    @field:NotBlank(message = "공고 제목은 필수입니다")
    @field:Size(max = 100, message = "공고 제목은 100자 이내여야 합니다")
    @Schema(description = "공고 제목", example = "카페 홀 스태프 모집")
    val title: String,

    @field:NotBlank(message = "근무지는 필수입니다")
    @Schema(description = "근무지", example = "제주시 연동")
    val workLocation: String,

    @field:Positive(message = "급여는 양수여야 합니다")
    @Schema(description = "급여", example = "9620")
    val salary: Long,

    @field:NotBlank(message = "직무는 필수입니다")
    @Schema(description = "직무", example = "서빙")
    val jobType: String,

    @field:NotBlank(message = "직책은 필수입니다")
    @Schema(description = "직책", example = "홀 스태프")
    val position: String,

    @field:Valid
    @Schema(description = "근무일정")
    val workSchedule: WorkScheduleDto,

    @Schema(description = "성별 (선택)", example = "무관", allowableValues = ["남성", "여성", "무관"])
    val gender: String? = "무관",

    @field:NotBlank(message = "상세 설명은 필수입니다")
    @Schema(description = "상세 설명")
    val description: String,

    @Schema(description = "이미지 URL 목록")
    val images: List<String> = emptyList(),

    @field:Future(message = "마감일은 현재 시간 이후여야 합니다")
    @Schema(description = "공고 마감 기한")
    val deadline: LocalDateTime,

    @field:NotBlank(message = "급여 정산일은 필수입니다")
    @Schema(description = "급여 정산일", example = "매월 25일")
    val paymentDate: String,

    @field:NotBlank(message = "기업명은 필수입니다")
    @Schema(description = "기업명", example = "제주 카페")
    val companyName: String,

    @field:NotBlank(message = "회사 주소는 필수입니다")
    @Schema(description = "회사 주소", example = "제주시 연동 123-45")
    val companyAddress: String,

    @field:NotBlank(message = "기업 연락처는 필수입니다")
    @field:Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    @Schema(description = "기업 연락처", example = "064-123-4567")
    val companyContact: String,

    @field:NotBlank(message = "대표자 이름은 필수입니다")
    @Schema(description = "대표자 이름", example = "홍길동")
    val representativeName: String
)

/**
 * 근무일정 DTO
 */
@Schema(description = "근무일정", example = """
{
  "days": ["월", "화", "수", "목", "금"],
  "startTime": "09:00",
  "endTime": "18:00",
  "workPeriod": "ONE_TO_THREE"
}
""")
data class WorkScheduleDto(
    @field:NotEmpty(message = "근무 요일은 최소 1개 이상이어야 합니다")
    @Schema(description = "근무 요일", example = "[\"월\", \"화\", \"수\", \"목\", \"금\"]")
    val days: List<String>,

    @field:NotBlank(message = "시작 시간은 필수입니다")
    @field:Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "시간 형식이 올바르지 않습니다 (HH:mm)")
    @Schema(description = "시작 시간", example = "09:00")
    val startTime: String,

    @field:NotBlank(message = "종료 시간은 필수입니다")
    @field:Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "시간 형식이 올바르지 않습니다 (HH:mm)")
    @Schema(description = "종료 시간", example = "18:00")
    val endTime: String,

    @field:NotNull(message = "근무 기간은 필수입니다")
    @Schema(description = "근무 기간", example = "ONE_TO_THREE", allowableValues = ["ONE_DAY", "WITHIN_WEEK", "ONE_MONTH", "ONE_TO_THREE", "THREE_TO_SIX", "LONG_TERM"])
    val workPeriod: WorkPeriod
) {
    fun toDomain(): WorkSchedule {
        return WorkSchedule(
            days = days,
            startTime = startTime,
            endTime = endTime,
            workPeriod = workPeriod
        )
    }
}

/**
 * 채용공고 응답
 */
@Schema(description = "채용공고 응답")
data class RecruitResponse(
    @Schema(description = "공고 ID")
    val id: String,

    @Schema(description = "공고 제목")
    val title: String,

    @Schema(description = "근무지")
    val workLocation: String,

    @Schema(description = "급여")
    val salary: Long,

    @Schema(description = "직무")
    val jobType: String,

    @Schema(description = "직책")
    val position: String,

    @Schema(description = "근무일정")
    val workSchedule: WorkScheduleDto,

    @Schema(description = "성별")
    val gender: String?,

    @Schema(description = "상세 설명")
    val description: String,

    @Schema(description = "이미지 URL 목록")
    val images: List<String>,

    @Schema(description = "공고 마감 기한")
    val deadline: LocalDateTime,

    @Schema(description = "급여 정산일")
    val paymentDate: String,

    @Schema(description = "작성자 ID")
    val managerId: String,

    @Schema(description = "기업명")
    val companyName: String,

    @Schema(description = "회사 주소")
    val companyAddress: String,

    @Schema(description = "기업 연락처")
    val companyContact: String,

    @Schema(description = "대표자 이름")
    val representativeName: String,

    @Schema(description = "공고 상태")
    val status: RecruitStatus,

    @Schema(description = "지원자 수")
    val applicationCount: Long,

    @Schema(description = "조회수")
    val viewCount: Long,

    @Schema(description = "등록일")
    val createdAt: LocalDateTime,

    @Schema(description = "수정일")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(recruit: Recruit): RecruitResponse {
            return RecruitResponse(
                id = recruit.id!!,
                title = recruit.title,
                workLocation = recruit.workLocation,
                salary = recruit.salary,
                jobType = recruit.jobType,
                position = recruit.position,
                workSchedule = WorkScheduleDto(
                    days = recruit.workSchedule.days,
                    startTime = recruit.workSchedule.startTime,
                    endTime = recruit.workSchedule.endTime,
                    workPeriod = recruit.workSchedule.workPeriod
                ),
                gender = recruit.gender,
                description = recruit.description,
                images = recruit.images,
                deadline = recruit.deadline,
                paymentDate = recruit.paymentDate,
                managerId = recruit.managerId,
                companyName = recruit.companyName,
                companyAddress = recruit.companyAddress,
                companyContact = recruit.companyContact,
                representativeName = recruit.representativeName,
                status = recruit.status,
                applicationCount = recruit.applicationCount,
                viewCount = recruit.viewCount,
                createdAt = recruit.createdAt!!,
                updatedAt = recruit.updatedAt!!
            )
        }
    }
}

/**
 * 채용공고 목록 응답 (요약 정보)
 */
@Schema(description = "채용공고 목록 응답")
data class RecruitSummaryResponse(
    @Schema(description = "공고 ID")
    val id: String,

    @Schema(description = "공고명")
    val title: String,

    @Schema(description = "기업명")
    val companyName: String,

    @Schema(description = "급여")
    val salary: Long,

    @Schema(description = "근무지역")
    val workLocation: String,

    @Schema(description = "근무일자 및 기간")
    val workSchedule: WorkScheduleDto,

    @Schema(description = "공고 상태")
    val status: RecruitStatus,

    @Schema(description = "지원자 수")
    val applicationCount: Long,

    @Schema(description = "등록일")
    val createdAt: LocalDateTime,

    @Schema(description = "마감일")
    val deadline: LocalDateTime
) {
    companion object {
        fun from(recruit: Recruit): RecruitSummaryResponse {
            return RecruitSummaryResponse(
                id = recruit.id!!,
                title = recruit.title,
                companyName = recruit.companyName,
                salary = recruit.salary,
                workLocation = recruit.workLocation,
                workSchedule = WorkScheduleDto(
                    days = recruit.workSchedule.days,
                    startTime = recruit.workSchedule.startTime,
                    endTime = recruit.workSchedule.endTime,
                    workPeriod = recruit.workSchedule.workPeriod
                ),
                status = recruit.status,
                applicationCount = recruit.applicationCount,
                createdAt = recruit.createdAt!!,
                deadline = recruit.deadline
            )
        }
    }
}

/**
 * 채용공고 수정 요청
 */
@Schema(description = "채용공고 수정 요청", example = """
{
  "title": "제주 연동 카페 홀 스태프 모집 (급구)",
  "salary": 11000,
  "deadline": "2025-01-31T23:59:59",
  "description": "친절하고 밝은 성격의 홀 스태프를 모집합니다. 카페 운영 경험이 있으시면 우대합니다. 급하게 구하고 있습니다!"
}
""")
data class UpdateRecruitRequest(
    @Schema(description = "공고 제목")
    val title: String?,

    @Schema(description = "근무지")
    val workLocation: String?,

    @Schema(description = "급여")
    val salary: Long?,

    @Schema(description = "직무")
    val jobType: String?,

    @Schema(description = "직책")
    val position: String?,

    @Schema(description = "근무일정")
    val workSchedule: WorkScheduleDto?,

    @Schema(description = "성별")
    val gender: String?,

    @Schema(description = "상세 설명")
    val description: String?,

    @Schema(description = "이미지 URL 목록")
    val images: List<String>?,

    @Schema(description = "공고 마감 기한")
    val deadline: LocalDateTime?,

    @Schema(description = "급여 정산일")
    val paymentDate: String?,

    @Schema(description = "기업명")
    val companyName: String?,

    @Schema(description = "회사 주소")
    val companyAddress: String?,

    @Schema(description = "기업 연락처")
    val companyContact: String?,

    @Schema(description = "대표자 이름")
    val representativeName: String?
)

/**
 * 채용공고 상태 변경 요청
 */
@Schema(description = "채용공고 상태 변경 요청", example = """
{
  "status": "CLOSED"
}
""")
data class UpdateRecruitStatusRequest(
    @field:NotNull(message = "상태는 필수입니다")
    @Schema(description = "공고 상태", example = "CLOSED", allowableValues = ["ACTIVE", "CLOSED", "COMPLETED"])
    val status: RecruitStatus
) 