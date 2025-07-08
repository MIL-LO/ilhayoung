package com.millo.ilhayoung.user.service

import com.millo.ilhayoung.user.dto.BusinessVerificationResponse
import com.millo.ilhayoung.user.dto.NtsApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.regex.Pattern

/**
 * 사업자등록번호 검증 서비스
 * 국세청 사업자등록정보 진위확인 API 활용
 */
@Service
class BusinessVerificationService {

    @Value("\${nts.api.key:}")
    private lateinit var ntsApiKey: String

    @Value("\${nts.api.url:https://api.odcloud.kr/api/nts-businessman/v1/status}")
    private lateinit var ntsApiUrl: String

    private val restTemplate = RestTemplate()

    /**
     * 사업자등록번호 검증
     */
    fun verifyBusinessNumber(businessNumber: String): BusinessVerificationResponse {
        // 형식 검증
        val formatValidation = validateBusinessNumberFormat(businessNumber)
        if (!formatValidation.isValid) {
            return formatValidation
        }

        // 국세청 API 호출
        return try {
            callNtsApi(businessNumber)
        } catch (e: Exception) {
            BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "사업자등록번호 검증 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    /**
     * 사업자등록번호 형식 검증
     */
    private fun validateBusinessNumberFormat(businessNumber: String): BusinessVerificationResponse {
        val cleanNumber = businessNumber.replace("-", "")

        // 길이 검증 (10자리)
        if (cleanNumber.length != 10) {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "사업자등록번호는 10자리여야 합니다."
            )
        }

        // 숫자만 포함하는지 검증
        if (!Pattern.matches("\\d{10}", cleanNumber)) {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "사업자등록번호는 숫자만 포함해야 합니다."
            )
        }

        // 체크썸 검증
        if (!validateChecksum(cleanNumber)) {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "유효하지 않은 사업자등록번호입니다."
            )
        }

        return BusinessVerificationResponse(
            isValid = true,
            businessNumber = businessNumber,
            message = "형식 검증 통과"
        )
    }

    /**
     * 사업자등록번호 체크썸 검증
     */
    private fun validateChecksum(businessNumber: String): Boolean {
        if (businessNumber.length != 10) return false

        val checkKey = intArrayOf(1, 3, 7, 1, 3, 7, 1, 3, 5)
        var sum = 0

        for (i in 0..8) {
            sum += Character.getNumericValue(businessNumber[i]) * checkKey[i]
        }

        sum += (Character.getNumericValue(businessNumber[8]) * 5) / 10
        val remainder = sum % 10
        val checkDigit = if (remainder == 0) 0 else 10 - remainder

        return checkDigit == Character.getNumericValue(businessNumber[9])
    }

    /**
     * 국세청 API 호출
     */
    private fun callNtsApi(businessNumber: String): BusinessVerificationResponse {
        if (ntsApiKey.isBlank()) {
            return BusinessVerificationResponse(
                isValid = true,
                businessNumber = businessNumber,
                message = "국세청 API 키가 설정되지 않았습니다. 형식 검증만 수행되었습니다."
            )
        }

        val cleanNumber = businessNumber.replace("-", "")
        
        // 국세청 API는 serviceKey 쿼리 파라미터로 API 키를 전달
        val apiUrlWithKey = "$ntsApiUrl?serviceKey=$ntsApiKey"
        
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = mapOf(
            "b_no" to listOf(cleanNumber)
        )

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.exchange(
                apiUrlWithKey,
                HttpMethod.POST,
                entity,
                NtsApiResponse::class.java
            )
            parseNtsResponse(response.body, businessNumber)
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            when (e.statusCode.value()) {
                401 -> BusinessVerificationResponse(
                    isValid = true,
                    businessNumber = businessNumber,
                    message = "국세청 API 인증 오류입니다. 형식 검증만 수행되었습니다. (API 키 확인 필요)"
                )
                else -> BusinessVerificationResponse(
                    isValid = false,
                    businessNumber = businessNumber,
                    message = "국세청 API 오류: ${e.statusCode} - 형식 검증은 통과했습니다."
                )
            }
        }
    }

    /**
     * 국세청 API 응답 파싱
     */
    private fun parseNtsResponse(response: NtsApiResponse?, businessNumber: String): BusinessVerificationResponse {
        if (response == null) {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "국세청 API 응답이 없습니다."
            )
        }

        if (response.statusCode != "OK") {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "국세청 API 오류: ${response.statusCode}"
            )
        }

        if (response.data.isNullOrEmpty()) {
            return BusinessVerificationResponse(
                isValid = false,
                businessNumber = businessNumber,
                message = "등록되지 않은 사업자등록번호입니다."
            )
        }

        val businessData = response.data[0]
        val isActive = businessData.businessStatusCode == "01"  // 01: 계속사업자

        return BusinessVerificationResponse(
            isValid = isActive,
            businessNumber = businessNumber,
            status = businessData.businessStatus,
            message = when {
                isActive -> "유효한 사업자등록번호입니다."
                businessData.businessStatusCode == "02" -> "휴업중인 사업자입니다."
                businessData.businessStatusCode == "03" -> "폐업한 사업자입니다."
                else -> "사업자 상태: ${businessData.businessStatus}"
            }
        )
    }
} 