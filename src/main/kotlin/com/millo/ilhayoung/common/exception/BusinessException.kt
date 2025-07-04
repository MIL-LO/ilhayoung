package com.millo.ilhayoung.common.exception

/**
 * 비즈니스 로직에서 발생하는 예외를 나타내는 클래스
 * 
 * @param errorCode 에러 코드
 * @param message 에러 메시지 (null인 경우 errorCode의 기본 메시지 사용)
 */
class BusinessException(
    val errorCode: ErrorCode,
    message: String? = null
) : RuntimeException(message ?: errorCode.message) {
    
    companion object {
        
        /**
         * 사용자를 찾을 수 없는 경우의 예외
         */
        fun userNotFound(): BusinessException = BusinessException(ErrorCode.USER_NOT_FOUND)
        
        /**
         * 이미 사용중인 이메일인 경우의 예외
         */
        fun emailAlreadyExists(): BusinessException = BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)
        
        /**
         * 유효하지 않은 토큰인 경우의 예외
         */
        fun invalidToken(): BusinessException = BusinessException(ErrorCode.INVALID_TOKEN)
        
        /**
         * 권한이 없는 경우의 예외
         */
        fun forbidden(): BusinessException = BusinessException(ErrorCode.FORBIDDEN)
        
        /**
         * 인증이 필요한 경우의 예외
         */
        fun unauthorized(): BusinessException = BusinessException(ErrorCode.UNAUTHORIZED)
    }
} 