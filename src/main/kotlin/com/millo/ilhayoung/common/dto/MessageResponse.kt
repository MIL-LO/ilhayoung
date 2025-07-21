package com.millo.ilhayoung.common.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 간단한 메시지 응답을 위한 DTO
 * 대부분의 생성/수정/삭제 API에서 사용
 */
@Schema(description = "메시지 응답")
data class MessageResponse(
    
    /**
     * 응답 메시지
     */
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    val message: String
) {
    
    companion object {
        
        /**
         * 기본 성공 메시지를 생성하는 팩토리 메서드
         */
        fun success(message: String): MessageResponse {
            return MessageResponse(message)
        }
        
        // API 스펙에 정의된 메시지들
        fun staffSignupCompleted(): MessageResponse = success("Staff signup completed")
        fun managerSignupCompleted(): MessageResponse = success("Manager signup completed")
        fun staffInfoUpdated(): MessageResponse = success("Staff info updated")
        fun managerInfoUpdated(): MessageResponse = success("Manager info updated")
        fun userDeleted(): MessageResponse = success("User deleted")
        fun loggedOut(): MessageResponse = success("Logged out")
    }
} 