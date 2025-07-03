package com.millo.ilhayoung.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

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
    val userType: String?,
    
    /**
     * 추가 정보 입력 필요 여부
     */
    @Schema(description = "추가 정보 입력 필요 여부", example = "false")
    val needAdditionalInfo: Boolean,
    
    /**
     * OAuth에서 받은 사용자 이름 (회원가입 폼 미리 채우기용)
     */
    @Schema(description = "OAuth에서 받은 사용자 이름", example = "홍길동")
    val oauthName: String?
)

/**
 * 활성 세션 응답 DTO
 */
@Schema(description = "활성 세션 정보")
data class ActiveSessionResponse(
    
    /**
     * 토큰 ID (보안을 위해 마지막 8자리만)
     */
    @Schema(description = "토큰 ID", example = "...ab123456")
    val tokenId: String,
    
    /**
     * 세션 생성 시간
     */
    @Schema(description = "세션 생성 시간")
    val createdAt: LocalDateTime,
    
    /**
     * 세션 만료 시간
     */
    @Schema(description = "세션 만료 시간")
    val expiresAt: LocalDateTime,
    
    /**
     * User-Agent 정보
     */
    @Schema(description = "User-Agent 정보", example = "Mozilla/5.0...")
    val userAgent: String?,
    
    /**
     * IP 주소
     */
    @Schema(description = "IP 주소", example = "192.168.1.100")
    val ipAddress: String?
) 