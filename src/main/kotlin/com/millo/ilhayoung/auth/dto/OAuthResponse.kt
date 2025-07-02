package com.millo.ilhayoung.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * OAuth 로그인 URL 응답 DTO
 */
@Schema(description = "OAuth 로그인 URL 응답")
data class LoginUrlResponse(
    
    /**
     * OAuth 로그인 URL
     */
    @Schema(description = "OAuth 로그인 URL", example = "https://accounts.google.com/oauth/authorize?...")
    val loginUrl: String
)

/**
 * OAuth 콜백 요청 DTO
 */
@Schema(description = "OAuth 콜백 요청")
data class OAuthCallbackRequest(
    
    /**
     * OAuth 인증 코드
     */
    @Schema(description = "OAuth 인증 코드", example = "abcde123456")
    val code: String,
    
    /**
     * 리다이렉트 URI
     */
    @Schema(description = "리다이렉트 URI", example = "https://your-frontend.com/oauth/callback")
    val redirectUri: String
)

/**
 * OAuth 콜백 응답 DTO
 */
@Schema(description = "OAuth 콜백 응답")
data class OAuthCallbackResponse(
    
    /**
     * Access Token
     */
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String,
    
    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String,
    
    /**
     * 사용자 타입
     * STAFF 또는 MANAGER, null이면 아직 설정되지 않음
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    val userType: String?,
    
    /**
     * 추가 정보 입력 필요 여부
     */
    @Schema(description = "추가 정보 입력 필요 여부", example = "true")
    val needAdditionalInfo: Boolean
)

/**
 * 토큰 재발급 요청 DTO
 */
@Schema(description = "토큰 재발급 요청")
data class RefreshTokenRequest(
    
    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String
)

/**
 * 토큰 재발급 응답 DTO
 */
@Schema(description = "토큰 재발급 응답")
data class RefreshTokenResponse(
    
    /**
     * 새로운 Access Token
     */
    @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String
)

/**
 * 로그아웃 응답 DTO
 */
@Schema(description = "로그아웃 응답")
data class LogoutResponse(
    
    /**
     * 로그아웃 메시지
     */
    @Schema(description = "로그아웃 메시지", example = "Logged out")
    val message: String = "Logged out"
) 