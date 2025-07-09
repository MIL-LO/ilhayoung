package com.millo.ilhayoung.recruit.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 채용공고 템플릿 도메인
 */
@Document(collection = "recruit_templates")
data class RecruitTemplate(
    val managerId: String,                // 템플릿 작성자
    val name: String,                     // 템플릿 이름
    val title: String,                    // 공고 제목 템플릿
    val workSchedule: WorkSchedule        // 근무 스케줄
) : BaseDocument() 