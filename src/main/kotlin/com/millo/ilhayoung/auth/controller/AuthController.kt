package com.millo.ilhayoung.auth.controller

import com.millo.ilhayoung.auth.dto.LogoutResponse
import com.millo.ilhayoung.auth.dto.RefreshTokenRequest
import com.millo.ilhayoung.auth.dto.RefreshTokenResponse
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
} 