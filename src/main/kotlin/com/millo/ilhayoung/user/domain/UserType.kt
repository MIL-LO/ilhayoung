package com.millo.ilhayoung.user.domain

/**
 * 사용자 타입을 정의하는 enum 클래스
 * API 스펙에 정의된 STAFF, MANAGER 타입을 구분
 */
enum class UserType(
    val description: String,
    val code: String
) {
    
    /**
     * 직원 (Staff)
     * 일자리를 찾는 구직자
     */
    STAFF("직원", "STAFF"),
    
    /**
     * 관리자 (Manager)
     * 사업장을 운영하며 직원을 고용하는 사업자
     */
    MANAGER("관리자", "MANAGER");
    
    companion object {
        
        /**
         * 코드 문자열로부터 UserType을 찾는 메서드
         * 
         * @param code 사용자 타입 코드
         * @return 해당하는 UserType, 없으면 null
         */
        fun fromCode(code: String?): UserType? {
            return values().find { it.code == code }
        }
        
        /**
         * 코드 문자열로부터 UserType을 찾는 메서드 (예외 발생)
         * 
         * @param code 사용자 타입 코드
         * @return 해당하는 UserType
         * @throws IllegalArgumentException 유효하지 않은 코드인 경우
         */
        fun fromCodeOrThrow(code: String): UserType {
            return fromCode(code) ?: throw IllegalArgumentException("유효하지 않은 사용자 타입입니다: $code")
        }
    }
} 