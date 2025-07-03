package com.millo.ilhayoung.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * STAFF 회원가입 요청 DTO
 */
@Schema(description = "STAFF 회원가입 요청")
data class StaffSignupRequest(
    

    
    /**
     * 생년월일 (YYYY-MM-DD)
     */
    @field:NotBlank(message = "생년월일은 필수입니다.")
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일은 YYYY-MM-DD 형식이어야 합니다.")
    @Schema(description = "생년월일", example = "1998-07-01")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String,
    
    /**
     * 주소
     */
    @field:NotBlank(message = "주소는 필수입니다.")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String,
    
    /**
     * 경력/경험
     */
    @field:NotBlank(message = "경력/경험은 필수입니다.")
    @Schema(description = "경력/경험", example = "한식 주점 홀 아르바이트 3개월")
    val experience: String
)

/**
 * MANAGER 회원가입 요청 DTO
 */
@Schema(description = "MANAGER 회원가입 요청")
data class ManagerSignupRequest(
    

    
    /**
     * 생년월일 (YYYY-MM-DD)
     */
    @field:NotBlank(message = "생년월일은 필수입니다.")
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일은 YYYY-MM-DD 형식이어야 합니다.")
    @Schema(description = "생년월일", example = "1980-02-15")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    @Schema(description = "전화번호", example = "010-2222-3333")
    val phone: String,
    
    /**
     * 사업지 주소
     */
    @field:NotBlank(message = "사업지 주소는 필수입니다.")
    @Schema(description = "사업지 주소", example = "서울시 마포구 서교동 321")
    val businessAddress: String,
    
    /**
     * 사업자등록번호
     */
    @field:NotBlank(message = "사업자등록번호는 필수입니다.")
    @field:Pattern(regexp = "\\d{10}", message = "사업자등록번호는 10자리 숫자여야 합니다.")
    @Schema(description = "사업자등록번호", example = "1234567890")
    val businessNumber: String,
    
    /**
     * 업종
     */
    @field:NotBlank(message = "업종은 필수입니다.")
    @Schema(description = "업종", example = "요식업")
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
    val userId: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    val userType: String,
    
    /**
     * 이름
     */
    @Schema(description = "이름", example = "홍길동")
    val name: String,
    
    /**
     * 생년월일
     */
    @Schema(description = "생년월일", example = "1998-07-01")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String,
    
    /**
     * 주소
     */
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String,
    
    /**
     * 경력/경험
     */
    @Schema(description = "경력/경험", example = "한식 주점 홀 아르바이트 3개월")
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
    val userId: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "MANAGER")
    val userType: String,
    
    /**
     * 이름
     */
    @Schema(description = "이름", example = "이사장")
    val name: String,
    
    /**
     * 생년월일
     */
    @Schema(description = "생년월일", example = "1980-02-15")
    val birthDate: String,
    
    /**
     * 전화번호
     */
    @Schema(description = "전화번호", example = "010-2222-3333")
    val phone: String,
    
    /**
     * 사업지 주소
     */
    @Schema(description = "사업지 주소", example = "서울시 마포구 서교동 321")
    val businessAddress: String,
    
    /**
     * 사업자등록번호
     */
    @Schema(description = "사업자등록번호", example = "1234567890")
    val businessNumber: String,
    
    /**
     * 업종
     */
    @Schema(description = "업종", example = "요식업")
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
    val phone: String?,
    
    /**
     * 주소
     */
    @Schema(description = "주소", example = "서울시 송파구 방이동 999")
    val address: String?,
    
    /**
     * 경력/경험
     */
    @Schema(description = "경력/경험", example = "일식 주점 서빙 6개월")
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
    val phone: String?,
    
    /**
     * 사업지 주소
     */
    @Schema(description = "사업지 주소", example = "서울시 마포구 합정동 11")
    val businessAddress: String?,
    
    /**
     * 업종
     */
    @Schema(description = "업종", example = "카페")
    val businessType: String?
)

/**
 * 회원가입 완료 응답
 */
@Schema(description = "회원가입 완료 응답")
data class SignupCompleteResponse(
    
    /**
     * 완료 메시지
     */
    @Schema(description = "완료 메시지", example = "STAFF 회원가입이 완료되었습니다.")
    val message: String,
    
    /**
     * 새로운 Access Token (userType 포함)
     */
    @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
    
    /**
     * 새로운 Refresh Token
     */
    @Schema(description = "새로운 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    val userType: String
) 