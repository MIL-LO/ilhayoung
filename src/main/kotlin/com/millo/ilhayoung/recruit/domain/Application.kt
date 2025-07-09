package com.millo.ilhayoung.recruit.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 * 지원 도메인
 */
@Document(collection = "applications")
@CompoundIndexes(
    CompoundIndex(
        name = "recruit_staff_idx",
        def = "{'recruitId': 1, 'staffId': 1}",
        unique = true
    )
)
data class Application(
    val recruitId: String,                // 채용공고 ID
    val staffId: String,                  // 지원자 ID
    val name: String,                     // 이름
    val birthDate: LocalDate,             // 생년월일
    val contact: String,                  // 연락처
    val address: String,                  // 주소
    val experience: String,               // 경력사항
    val climateScore: Int,               // 기후점수
    val status: ApplicationStatus = ApplicationStatus.REVIEWING // 지원 상태
) : BaseDocument() 