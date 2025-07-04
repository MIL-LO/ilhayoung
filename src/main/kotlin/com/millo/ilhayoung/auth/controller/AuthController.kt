package com.millo.ilhayoung.auth.controller

import com.millo.ilhayoung.auth.dto.*
import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.auth.service.AuthService
import com.millo.ilhayoung.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * 인증 관련 API를 담당하는 Controller 클래스
 * 토큰 재발급, 로그아웃 기능을 담당
 * (역할 선택은 OAuth 로그인 시 프론트엔드에서 파라미터로 전달)
 */
@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * Access Token과 Refresh Token 재발급
     * 기존 AccessToken을 Authorization 헤더로 받아 블랙리스트에 추가
     * 
     * @param request Refresh Token 요청 정보
     * @param httpRequest HTTP 요청 (Authorization 헤더에서 기존 AccessToken 추출용)
     * @return 새로운 Access Token과 Refresh Token 응답
     */
    @Operation(
        summary = "토큰 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다. 기존 AccessToken과 RefreshToken은 무효화되고 블랙리스트에 등록됩니다."
    )
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ApiResponse<TokenResponse> {
        val response = authService.refreshTokensWithAccessToken(request.refreshToken, httpRequest)
        return ApiResponse.success(response)
    }
    
    /**
     * 로그아웃
     * 
     * @param user 현재 인증된 사용자 정보
     * @param request HTTP 요청 (Authorization 헤더에서 토큰 추출용)
     * @return 로그아웃 응답
     */
    @Operation(
        summary = "로그아웃",
        description = "AccessToken을 블랙리스트에 등록하고 모든 RefreshToken을 삭제합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal user: UserPrincipal?,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.")
        }
        
        authService.logout(user.userId, request)
        
        return ResponseEntity.noContent().build()
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
    ): ApiResponse<TokenValidationResponse> {
        val response = authService.validateAccessToken(userPrincipal.userId)
        return ApiResponse.success(response)
    }
} 