package com.millo.ilhayoung.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 사업자등록번호 검증 요청 DTO
 */
@Schema(description = "사업자등록번호 검증 요청", example = """
{
  "businessNumber": "1234567890"
}
""")
data class BusinessVerificationRequest(
    @field:NotBlank(message = "사업자등록번호를 입력해주세요.")
    @Schema(description = "사업자등록번호", example = "1234567890")
    val businessNumber: String = ""
)

/**
 * 사업자등록번호 검증 응답 DTO
 */
data class BusinessVerificationResponse(
    val isValid: Boolean,
    val businessNumber: String,
    val status: String? = null,
    val message: String
)

/**
 * 국세청 API 응답 DTO
 */
data class NtsApiResponse(
    @JsonProperty("status_code")
    val statusCode: String,
    
    @JsonProperty("match_cnt")
    val matchCount: Int,
    
    @JsonProperty("request_cnt")
    val requestCount: Int,
    
    @JsonProperty("data")
    val data: List<NtsBusinessData>?
)

/**
 * 국세청 API 사업자 데이터 DTO
 */
data class NtsBusinessData(
    @JsonProperty("b_no")
    val businessNumber: String,
    
    @JsonProperty("b_stt")
    val businessStatus: String,
    
    @JsonProperty("b_stt_cd")
    val businessStatusCode: String,
    
    @JsonProperty("tax_type")
    val taxType: String,
    
    @JsonProperty("tax_type_cd")
    val taxTypeCode: String,
    
    @JsonProperty("end_dt")
    val endDate: String?,
    
    @JsonProperty("utcc_yn")
    val utccYn: String,
    
    @JsonProperty("tax_type_change_dt")
    val taxTypeChangeDate: String?,
    
    @JsonProperty("invoice_apply_dt")
    val invoiceApplyDate: String?
) 