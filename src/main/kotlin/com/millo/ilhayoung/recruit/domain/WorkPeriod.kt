package com.millo.ilhayoung.recruit.domain

/**
 * 근무 기간 타입
 */
enum class WorkPeriod(val displayName: String) {
    ONE_DAY("하루"),
    WITHIN_WEEK("1주일이내"),
    ONE_MONTH("1개월"),
    ONE_TO_THREE("1-3개월"),
    THREE_TO_SIX("3-6개월"),
    LONG_TERM("장기")
} 