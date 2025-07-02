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
        fun userNotFound(): BusinessException {
            return BusinessException(ErrorCode.USER_NOT_FOUND)
        }
        
        /**
         * 이미 존재하는 사용자인 경우의 예외
         */
        fun userAlreadyExists(): BusinessException {
            return BusinessException(ErrorCode.USER_ALREADY_EXISTS)
        }
        
        /**
         * 이미 사용중인 이메일인 경우의 예외
         */
        fun emailAlreadyExists(): BusinessException {
            return BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        
        /**
         * 이미 사용중인 전화번호인 경우의 예외
         */
        fun phoneAlreadyExists(): BusinessException {
            return BusinessException(ErrorCode.PHONE_ALREADY_EXISTS)
        }
        
        /**
         * 유효하지 않은 토큰인 경우의 예외
         */
        fun invalidToken(): BusinessException {
            return BusinessException(ErrorCode.INVALID_TOKEN)
        }
        
        /**
         * 만료된 토큰인 경우의 예외
         */
        fun expiredToken(): BusinessException {
            return BusinessException(ErrorCode.EXPIRED_TOKEN)
        }
        
        /**
         * 권한이 없는 경우의 예외
         */
        fun forbidden(): BusinessException {
            return BusinessException(ErrorCode.FORBIDDEN)
        }
        
        /**
         * 인증이 필요한 경우의 예외
         */
        fun unauthorized(): BusinessException {
            return BusinessException(ErrorCode.UNAUTHORIZED)
        }
        
        /**
         * 입력값이 올바르지 않은 경우의 예외
         * 
         * @param message 상세 에러 메시지
         */
        fun invalidInputValue(message: String): BusinessException {
            return BusinessException(ErrorCode.INVALID_INPUT_VALUE, message)
        }
    }
} 