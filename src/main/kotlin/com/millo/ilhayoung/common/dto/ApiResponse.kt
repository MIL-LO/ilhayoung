package com.millo.ilhayoung.common.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * API 응답의 기본 형태를 정의하는 클래스
 * 제공된 API 스펙에 맞춰 code, message, data 구조를 사용
 *
 * @param T 실제 응답 데이터의 타입
 */
@Schema(description = "API 응답 형식")
data class ApiResponse<T>(
    
    /**
     * 응답 코드
     * 성공: "SUCCESS", 실패: 각종 에러 코드
     */
    @Schema(description = "응답 코드", example = "SUCCESS")
    val code: String,
    
    /**
     * 응답 메시지
     * 성공/실패에 대한 설명 메시지
     */
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    val message: String,
    
    /**
     * 실제 응답 데이터
     * 성공 시에만 데이터가 포함됨
     */
    @Schema(description = "응답 데이터")
    val data: T? = null
    
) {
    
    companion object {
        
        /**
         * 성공 응답을 생성하는 팩토리 메서드
         * 
         * @param data 응답 데이터
         * @param message 성공 메시지 (기본값: "요청이 성공적으로 처리되었습니다.")
         * @return 성공 응답 객체
         */
        fun <T> success(data: T? = null, message: String = "요청이 성공적으로 처리되었습니다."): ApiResponse<T> {
            return ApiResponse(
                code = "SUCCESS",
                message = message,
                data = data
            )
        }
        
        /**
         * 실패 응답을 생성하는 팩토리 메서드
         * 
         * @param code 에러 코드
         * @param message 에러 메시지
         * @return 실패 응답 객체
         */
        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(
                code = code,
                message = message,
                data = null
            )
        }
    }
} 