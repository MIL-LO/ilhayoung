package com.millo.ilhayoung.recruit.domain

/**
 * 채용공고 상태
 */
enum class RecruitStatus(val displayName: String) {
    ACTIVE("모집중"),
    CLOSED("마감"),
    COMPLETED("모집완료")
} 