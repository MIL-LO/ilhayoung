package com.millo.ilhayoung.attendance.domain

/**
 * 근무 상태
 */
enum class WorkStatus(val displayName: String) {
    SCHEDULED("예정"),     // 근무 예정
    PRESENT("출근"),       // 출근
    ABSENT("결근"),        // 결근
    LATE("지각"),          // 지각
    COMPLETED("완료")      // 근무 완료
} 