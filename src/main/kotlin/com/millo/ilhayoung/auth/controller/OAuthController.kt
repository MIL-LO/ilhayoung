package com.millo.ilhayoung.auth.controller

import com.millo.ilhayoung.auth.dto.LoginUrlResponse
import com.millo.ilhayoung.auth.dto.OAuthCallbackRequest
import com.millo.ilhayoung.auth.dto.OAuthCallbackResponse
import com.millo.ilhayoung.auth.service.AuthService
import com.millo.ilhayoung.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * OAuth 관련 API를 담당하는 Controller 클래스
 * 소셜 로그인 URL 제공 및 콜백 처리를 담당
 */
@Tag(name = "OAuth", description = "OAuth 소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/oauth")
class OAuthController(
    private val authService: AuthService
) {
    
    /**
     * OAuth 로그인 URL 조회
     * 
     * @param provider OAuth2 제공자 (google, kakao, naver)
     * @return 로그인 URL 응답
     */
    @Operation(
        summary = "OAuth 로그인 URL 조회",
        description = "지정된 OAuth2 제공자의 로그인 URL을 반환합니다."
    )
    @GetMapping("/{provider}/login")
    fun getLoginUrl(
        @Parameter(description = "OAuth2 제공자", example = "google")
        @PathVariable provider: String
    ): ApiResponse<LoginUrlResponse> {
        val response = authService.getLoginUrl(provider)
        return ApiResponse.success(response)
    }
    
    /**
     * OAuth 콜백 처리 및 토큰 발급
     * 
     * @param provider OAuth2 제공자
     * @param request OAuth 콜백 요청 정보
     * @return 토큰 및 사용자 정보 응답
     */
    @Operation(
        summary = "OAuth 콜백 처리",
        description = "OAuth2 인증 완료 후 콜백을 처리하고 JWT 토큰을 발급합니다."
    )
    @PostMapping("/{provider}/callback")
    fun handleCallback(
        @Parameter(description = "OAuth2 제공자", example = "google")
        @PathVariable provider: String,
        @Valid @RequestBody request: OAuthCallbackRequest
    ): ApiResponse<OAuthCallbackResponse> {
        val response = authService.handleOAuthCallback(provider, request)
        return ApiResponse.success(response)
    }
} 