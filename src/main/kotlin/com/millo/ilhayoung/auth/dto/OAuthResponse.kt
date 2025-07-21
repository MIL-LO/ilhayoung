package com.millo.ilhayoung.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 토큰 재발급 요청 DTO
 */
@Schema(description = "토큰 재발급 요청")
data class RefreshTokenRequest(
    
    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1...")
    @JsonProperty("refreshToken")
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
    val message: String = "로그아웃이 완료되었습니다.",
    
    /**
     * 무효화된 토큰 수
     */
    @Schema(description = "무효화된 토큰 수", example = "2")
    val invalidatedTokens: Int = 0
)

/**
 * 토큰 유효성 검증 응답 DTO (Flutter 앱용)
 */
@Schema(description = "토큰 유효성 검증 응답")
data class TokenValidationResponse(
    
    /**
     * 토큰 유효성
     */
    @Schema(description = "토큰 유효성", example = "true")
    val valid: Boolean,
    
    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "60f1b2b3b3b3b3b3b3b3b3b3")
    val userId: String,
    
    /**
     * 사용자 이메일
     */
    @Schema(description = "사용자 이메일", example = "user@example.com")
    val email: String,
    
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    val userType: String,
    
    /**
     * 사용자 상태
     */
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val status: String,
    
    /**
     * OAuth 이름
     */
    @Schema(description = "OAuth 이름", example = "홍길동")
    val oauthName: String,
    
    /**
     * 권한 목록
     */
    @Schema(description = "권한 목록", example = "[\"ROLE_STAFF\"]")
    val authorities: List<String>
)

/**
 * 회원가입 완료 응답 DTO
 */
@Schema(description = "회원가입 완료 응답")
data class SignupCompleteResponse(
    /**
     * 완료 메시지
     */
    @Schema(description = "완료 메시지", example = "STAFF 회원가입이 완료되었습니다.")
    val message: String,
    /**
     * 사용자 타입
     */
    @Schema(description = "사용자 타입", example = "STAFF")
    val userType: String,
    /**
     * Access Token
     */
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String,
    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String
)

/**
 * 토큰 응답 DTO
 */
@Schema(description = "토큰 응답")
data class TokenResponse(
    
    /**
     * 액세스 토큰
     */
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String,
    
    /**
     * 리프레시 토큰
     */
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String
)

/**
 * 간소화된 OAuth 로그인 응답 DTO (JWT 토큰만 포함)
 */
@Schema(description = "간소화된 OAuth 로그인 응답")
data class SimpleOAuthResponse(
    
    /**
     * 성공 여부
     */
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
    
    /**
     * 응답 메시지
     */
    @Schema(description = "응답 메시지", example = "로그인 성공")
    val message: String,
    
    /**
     * Access Token (JWT에 모든 정보 포함)
     */
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String
)

/**
 * OAuth 로그인 성공 응답 DTO (refreshToken 포함)
 */
@Schema(description = "OAuth 로그인 성공 응답 (refreshToken 포함)")
data class OAuthLoginSuccessResponse(
    
    /**
     * 성공 여부
     */
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
    
    /**
     * 응답 메시지
     */
    @Schema(description = "응답 메시지", example = "로그인 성공")
    val message: String,
    
    /**
     * Access Token (JWT에 모든 정보 포함)
     */
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String,
    
    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String
)

 