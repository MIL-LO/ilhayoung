package com.millo.ilhayoung.auth.controller

import com.millo.ilhayoung.auth.dto.*
import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.auth.service.AuthService
import com.millo.ilhayoung.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API를 담당하는 Controller 클래스
 * 토큰 재발급, 로그아웃 기능을 담당
 */
@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * Access Token 재발급
     * 
     * @param request Refresh Token 요청 정보
     * @return 새로운 Access Token 응답
     */
    @Operation(
        summary = "Access Token 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
    )
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse> {
        val response = authService.refreshAccessToken(request)
        return ApiResponse.success(response)
    }
    
    /**
     * 로그아웃
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 로그아웃 응답
     */
    @Operation(
        summary = "로그아웃",
        description = "현재 사용자를 로그아웃하고 Refresh Token을 무효화합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<LogoutResponse> {
        val response = authService.logout(userPrincipal.userId)
        return ApiResponse.success(response)
    }

    /**
     * 토큰 유효성 검증 (Flutter 앱용)
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 토큰 유효성 및 사용자 정보
     */
    @Operation(
        summary = "토큰 유효성 검증",
        description = "현재 Access Token의 유효성을 검증하고 사용자 정보를 반환합니다. Flutter 앱에서 앱 시작 시 토큰 검증용으로 사용됩니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @GetMapping("/validate")
    fun validateToken(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<Map<String, Any?>> {
        // 사용자 정보 조회하여 OAuth 이름 포함
        val user = authService.getUserById(userPrincipal.userId)
        val response = mapOf(
            "valid" to true,
            "userId" to userPrincipal.userId,
            "email" to userPrincipal.email,
            "userType" to userPrincipal.userType?.code,
            "needAdditionalInfo" to user.needAdditionalInfo,
            "oauthName" to user.oauthName,
            "authorities" to userPrincipal.authorities.map { it.authority }
        )
        return ApiResponse.success(response)
    }

    /**
     * 특정 디바이스 로그아웃 (Flutter 앱용)
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @param request 로그아웃할 특정 토큰 정보
     * @return 로그아웃 응답
     */
    @Operation(
        summary = "특정 디바이스 로그아웃",
        description = "현재 사용자의 특정 디바이스(RefreshToken)만 로그아웃 처리합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PostMapping("/logout-device")
    fun logoutDevice(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: RefreshTokenRequest
    ): ApiResponse<LogoutResponse> {
        val response = authService.logoutDevice(userPrincipal.userId, request.refreshToken)
        return ApiResponse.success(response)
    }

    /**
     * 활성 세션 목록 조회
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 활성 세션 목록
     */
    @Operation(
        summary = "활성 세션 목록 조회",
        description = "현재 사용자의 모든 활성 세션(로그인된 디바이스) 목록을 조회합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @GetMapping("/sessions")
    fun getActiveSessions(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<List<ActiveSessionResponse>> {
        val response = authService.getActiveSessions(userPrincipal.userId)
        return ApiResponse.success(response)
    }
} 