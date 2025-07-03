package com.millo.ilhayoung.common.exception

import com.millo.ilhayoung.common.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리를 담당하는 클래스
 * 모든 Controller에서 발생하는 예외를 통합적으로 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    /**
     * 비즈니스 예외 처리
     * 
     * @param e BusinessException
     * @return 에러 응답
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(e.errorCode.code, e.message ?: e.errorCode.message)
        return ResponseEntity.status(e.errorCode.httpStatus).body(response)
    }
    
    /**
     * 일반 RuntimeException 처리
     * 
     * @param e RuntimeException
     * @return 에러 응답
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            ErrorCode.INTERNAL_SERVER_ERROR.code,
            e.message ?: ErrorCode.INTERNAL_SERVER_ERROR.message
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
    
    /**
     * Validation 예외 처리
     * 
     * @param e MethodArgumentNotValidException
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = e.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        
        val response = ApiResponse.error<Nothing>(
            ErrorCode.INVALID_INPUT_VALUE.code,
            errorMessage.ifEmpty { ErrorCode.INVALID_INPUT_VALUE.message }
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
    
    /**
     * IllegalArgumentException 처리
     * 
     * @param e IllegalArgumentException
     * @return 에러 응답
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            ErrorCode.INVALID_INPUT_VALUE.code,
            e.message ?: ErrorCode.INVALID_INPUT_VALUE.message
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
    
    /**
     * 기타 모든 예외 처리
     * 
     * @param e Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            ErrorCode.INTERNAL_SERVER_ERROR.code,
            "서버 내부 오류가 발생했습니다."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
} 