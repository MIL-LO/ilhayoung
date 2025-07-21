package com.millo.ilhayoung.salary.domain

/**
 * 급여 지급 상태
 */
enum class PayrollStatus(val displayName: String) {
    PENDING("지급 대기"),      // 지급 대기
    CALCULATED("계산 완료"),   // 계산 완료
    APPROVED("승인 완료"),     // 승인 완료
    PAID("지급 완료"),         // 지급 완료
    CANCELLED("취소")          // 취소
} 