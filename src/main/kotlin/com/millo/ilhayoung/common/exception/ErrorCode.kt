package com.millo.ilhayoung.common.exception

/**
 * 시스템에서 사용하는 에러 코드를 정의하는 enum
 */
enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: Int = 400
) {
    
    // 공통 에러
    SUCCESS("SUCCESS", "요청이 성공적으로 처리되었습니다.", 200),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.", 400),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", 500),
    
    // 사용자 관련 에러
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "이미 존재하는 사용자입니다.", 409),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 사용중인 이메일입니다.", 409),
    PHONE_ALREADY_EXISTS("PHONE_ALREADY_EXISTS", "이미 사용중인 전화번호입니다.", 409),
    INVALID_BUSINESS_NUMBER("INVALID_BUSINESS_NUMBER", "유효하지 않은 사업자등록번호입니다.", 400),
    
    // 인증 관련 에러
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", 401),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다.", 401),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다.", 401),
    ALREADY_REGISTERED("ALREADY_REGISTERED", "이미 등록된 사용자입니다.", 409),
    INVALID_USER_TYPE("INVALID_USER_TYPE", "유효하지 않은 사용자 타입입니다.", 400),
    USER_NOT_ACTIVE("USER_NOT_ACTIVE", "활성화되지 않은 사용자입니다.", 403),
    
    // 채용 관련 에러
    RECRUIT_NOT_FOUND("RECRUIT_NOT_FOUND", "채용 공고를 찾을 수 없습니다.", 404),
    APPLICATION_NOT_FOUND("APPLICATION_NOT_FOUND", "지원서를 찾을 수 없습니다.", 404),
    ALREADY_APPLIED("ALREADY_APPLIED", "이미 지원한 공고입니다.", 409),
    RECRUIT_CLOSED("RECRUIT_CLOSED", "마감된 채용 공고입니다.", 400),
    RECRUIT_HAS_APPLICATIONS("RECRUIT_HAS_APPLICATIONS", "지원자가 있는 공고는 삭제할 수 없습니다.", 400),
    ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다.", 403),
    TEMPLATE_NOT_FOUND("TEMPLATE_NOT_FOUND", "템플릿을 찾을 수 없습니다.", 404),
    
    // 출근 관련 에러
    ATTENDANCE_NOT_FOUND("ATTENDANCE_NOT_FOUND", "출근 기록을 찾을 수 없습니다.", 404),
    ALREADY_CLOCKED_IN("ALREADY_CLOCKED_IN", "이미 출근 처리되었습니다.", 409),
    ALREADY_CLOCKED_OUT("ALREADY_CLOCKED_OUT", "이미 퇴근 처리되었습니다.", 409),
    NOT_CLOCKED_IN("NOT_CLOCKED_IN", "출근 기록이 없습니다.", 400),
    
    // 급여 관련 에러
    SALARY_NOT_FOUND("SALARY_NOT_FOUND", "급여 정보를 찾을 수 없습니다.", 404),
    SALARY_ALREADY_PAID("SALARY_ALREADY_PAID", "이미 지급된 급여입니다.", 409),
    INVALID_SALARY_AMOUNT("INVALID_SALARY_AMOUNT", "유효하지 않은 급여 금액입니다.", 400),
    
    // 신뢰도 관련 에러
    TRUST_SCORE_NOT_FOUND("TRUST_SCORE_NOT_FOUND", "신뢰도 점수를 찾을 수 없습니다.", 404),
    INVALID_TRUST_SCORE("INVALID_TRUST_SCORE", "유효하지 않은 신뢰도 점수입니다.", 400)
} 