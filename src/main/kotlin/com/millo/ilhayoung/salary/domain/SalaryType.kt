package com.millo.ilhayoung.salary.domain

/**
 * 급여 유형
 */
enum class SalaryType(val displayName: String) {
    HOURLY("시급"),        // 시급
    DAILY("일급"),         // 일급
    MONTHLY("월급"),       // 월급
    PROJECT("프로젝트")    // 프로젝트 단위
} 