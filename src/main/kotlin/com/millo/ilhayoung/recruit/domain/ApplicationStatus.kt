package com.millo.ilhayoung.recruit.domain

/**
 * 지원 상태
 */
enum class ApplicationStatus(val displayName: String) {
    APPLIED("지원완료"),
    INTERVIEW("면접 요청"),
    HIRED("채용 확정"),
    REJECTED("거절")
} 