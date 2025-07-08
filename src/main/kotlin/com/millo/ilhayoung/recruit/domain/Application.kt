package com.millo.ilhayoung.recruit.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 * 지원 도메인
 */
@Document(collection = "applications")
data class Application(
    val recruitId: String,                // 공고 ID
    val staffId: String,                  // 지원자 (STAFF)
    val name: String,                     // 이름
    val birthDate: LocalDate,             // 생년월일
    val contact: String,                  // 연락처
    val address: String,                  // 주소
    val experience: String,               // 경력 또는 관련 경험
    val climateScore: Int? = null,        // 오름지수
    val status: ApplicationStatus = ApplicationStatus.APPLIED // 지원 상태
) : BaseDocument() 