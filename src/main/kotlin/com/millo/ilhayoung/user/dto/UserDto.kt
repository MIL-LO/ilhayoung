package com.millo.ilhayoung.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * STAFF 회원가입 요청 DTO
 */
@Schema(description = "STAFF 회원가입 요청", example = """
{
  "birthDate": "1998-07-01",
  "phone": "010-1234-5678",
  "address": "제주시 노형동 456-78",
  "experience": "한식 주점 홀 아르바이트 3개월, 패스트푸드점 카운터 6개월"
}
""")
data class StaffSignupRequest(
    

    
    /**
     * 생년월일 (YYYY-MM-DD)
     */
    @field:NotBlank(message = "생년월일은 필수입니다.")
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일은 YYYY-MM-DD 형식이어야 합니다.")
    @Schema(description = "생년월일", example = "1998-07-01")
    @JsonProperty("birthDate")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone")
    val phone: String,
    
    /**
     * 주소
     */
    @field:NotBlank(message = "주소는 필수입니다.")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    @JsonProperty("address")
    val address: String,
    
    /**
     * 경력/경험
     */
    @field:NotBlank(message = "경력/경험은 필수입니다.")
    @Schema(description = "경력/경험", example = "한식 주점 홀 아르바이트 3개월")
    @JsonProperty("experience")
    val experience: String
)

/**
 * MANAGER 회원가입 요청 DTO
 */
@Schema(description = "MANAGER 회원가입 요청", example = """
{
  "birthDate": "1980-02-15",
  "phone": "010-2222-3333",
  "businessName": "제주 힐링 카페",
  "businessAddress": "제주시 연동 789-12",
  "businessNumber": "1234567890",
  "businessType": "요식업"
}
""")
data class ManagerSignupRequest(
    

    
    /**
     * 생년월일 (YYYY-MM-DD)
     */
    @field:NotBlank(message = "생년월일은 필수입니다.")
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일은 YYYY-MM-DD 형식이어야 합니다.")
    @Schema(description = "생년월일", example = "1980-02-15")
    @JsonProperty("birthDate")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-2222-3333")
    @JsonProperty("phone")
    val phone: String,
    
    /**
     * 업장이름
     */
    @field:NotBlank(message = "업장이름은 필수입니다.")
    @Schema(description = "업장이름", example = "제주 힐링 카페")
    @JsonProperty("businessName")
    val businessName: String,
    
    /**
     * 사업지 주소
     */
    @field:NotBlank(message = "사업지 주소는 필수입니다.")
    @Schema(description = "사업지 주소", example = "서울시 마포구 서교동 321")
    @JsonProperty("businessAddress")
    val businessAddress: String,
    
    /**
     * 사업자등록번호
     */
    @field:NotBlank(message = "사업자등록번호는 필수입니다.")
    @field:Pattern(regexp = "\\d{10}", message = "사업자등록번호는 10자리 숫자여야 합니다.")
    @Schema(description = "사업자등록번호", example = "1234567890")
    @JsonProperty("businessNumber")
    val businessNumber: String,
    
    /**
     * 업종
     */
    @field:NotBlank(message = "업종은 필수입니다.")
    @Schema(description = "업종", example = "요식업")
    @JsonProperty("businessType")
    val businessType: String
)

/**
 * 현재 사용자 정보 응답 DTO (STAFF)
 */
@Schema(description = "STAFF 사용자 정보 응답")
data class StaffUserResponse(
    
    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "uuid-1234")
    @JsonProperty("userId")
    val userId: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    @JsonProperty("userType")
    val userType: String,
    
    /**
     * 이름
     */
    @Schema(description = "이름", example = "홍길동")
    @JsonProperty("name")
    val name: String,
    
    /**
     * 이메일
     */
    @Schema(description = "이메일", example = "user@example.com")
    @JsonProperty("email")
    val email: String,
    
    /**
     * OAuth 제공자
     */
    @Schema(description = "OAuth 제공자", example = "google")
    @JsonProperty("provider")
    val provider: String,
    
    /**
     * OAuth 제공자 ID
     */
    @Schema(description = "OAuth 제공자 ID", example = "123456789")
    @JsonProperty("providerId")
    val providerId: String,
    
    /**
     * 생년월일
     */
    @Schema(description = "생년월일", example = "1998-07-01")
    @JsonProperty("birthDate")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone")
    val phone: String,
    
    /**
     * 주소
     */
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    @JsonProperty("address")
    val address: String,
    
    /**
     * 경력/경험
     */
    @Schema(description = "경력/경험", example = "한식 주점 홀 아르바이트 3개월")
    @JsonProperty("experience")
    val experience: String
)

/**
 * 현재 사용자 정보 응답 DTO (MANAGER)
 */
@Schema(description = "MANAGER 사용자 정보 응답")
data class ManagerUserResponse(
    
    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "uuid-1234")
    @JsonProperty("userId")
    val userId: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "MANAGER")
    @JsonProperty("userType")
    val userType: String,
    
    /**
     * 이름
     */
    @Schema(description = "이름", example = "이사장")
    @JsonProperty("name")
    val name: String,
    
    /**
     * 이메일
     */
    @Schema(description = "이메일", example = "user@example.com")
    @JsonProperty("email")
    val email: String,
    
    /**
     * OAuth 제공자
     */
    @Schema(description = "OAuth 제공자", example = "google")
    @JsonProperty("provider")
    val provider: String,
    
    /**
     * OAuth 제공자 ID
     */
    @Schema(description = "OAuth 제공자 ID", example = "123456789")
    @JsonProperty("providerId")
    val providerId: String,
    
    /**
     * 생년월일
     */
    @Schema(description = "생년월일", example = "1980-02-15")
    @JsonProperty("birthDate")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @Schema(description = "전화번호", example = "010-2222-3333")
    @JsonProperty("phone")
    val phone: String,
    
    /**
     * 업장이름
     */
    @Schema(description = "업장이름", example = "제주 힐링 카페")
    @JsonProperty("businessName")
    val businessName: String,
    
    /**
     * 사업지 주소
     */
    @Schema(description = "사업지 주소", example = "서울시 마포구 서교동 321")
    @JsonProperty("businessAddress")
    val businessAddress: String,
    
    /**
     * 사업자등록번호
     */
    @Schema(description = "사업자등록번호", example = "1234567890")
    @JsonProperty("businessNumber")
    val businessNumber: String,
    
    /**
     * 업종
     */
    @Schema(description = "업종", example = "요식업")
    @JsonProperty("businessType")
    val businessType: String
)

/**
 * STAFF 정보 수정 요청 DTO
 */
@Schema(description = "STAFF 정보 수정 요청")
data class StaffUpdateRequest(
    
    /**
     * 전화번호
     */
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-2222-1111")
    @JsonProperty("phone")
    val phone: String?,
    
    /**
     * 주소
     */
    @Schema(description = "주소", example = "서울시 송파구 방이동 999")
    @JsonProperty("address")
    val address: String?,
    
    /**
     * 경력/경험
     */
    @Schema(description = "경력/경험", example = "일식 주점 서빙 6개월")
    @JsonProperty("experience")
    val experience: String?
)

/**
 * MANAGER 정보 수정 요청 DTO
 */
@Schema(description = "MANAGER 정보 수정 요청")
data class ManagerUpdateRequest(
    
    /**
     * 전화번호
     */
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-1111-2222")
    @JsonProperty("phone")
    val phone: String?,
    
    /**
     * 업장이름
     */
    @Schema(description = "업장이름", example = "제주 힐링 카페")
    @JsonProperty("businessName")
    val businessName: String?,
    
    /**
     * 사업지 주소
     */
    @Schema(description = "사업지 주소", example = "서울시 마포구 합정동 11")
    @JsonProperty("businessAddress")
    val businessAddress: String?,
    
    /**
     * 업종
     */
    @Schema(description = "업종", example = "카페")
    @JsonProperty("businessType")
    val businessType: String?
) 