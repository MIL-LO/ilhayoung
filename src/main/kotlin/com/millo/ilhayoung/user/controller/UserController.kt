package com.millo.ilhayoung.user.controller

import com.millo.ilhayoung.auth.jwt.UserPrincipal
import com.millo.ilhayoung.common.dto.ApiResponse
import com.millo.ilhayoung.common.dto.MessageResponse
import com.millo.ilhayoung.user.dto.*
import com.millo.ilhayoung.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * User 관련 API를 담당하는 Controller 클래스
 * 회원가입, 정보 조회/수정, 회원 탈퇴 기능을 담당
 */
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    
    /**
     * STAFF 회원가입
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @param request STAFF 회원가입 요청 정보
     * @return 회원가입 완료 메시지
     */
    @Operation(
        summary = "STAFF 회원가입",
        description = "직원(STAFF) 추가 정보를 등록하여 회원가입을 완료합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PostMapping("/staff/signup")
    fun signupStaff(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: StaffSignupRequest
    ): ApiResponse<SignupCompleteResponse> {
        val response = userService.signupStaff(userPrincipal.userId, request)
        return ApiResponse.success(response)
    }
    
    /**
     * MANAGER 회원가입
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @param request MANAGER 회원가입 요청 정보
     * @return 회원가입 완료 메시지
     */
    @Operation(
        summary = "MANAGER 회원가입",
        description = "관리자(MANAGER) 추가 정보를 등록하여 회원가입을 완료합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PostMapping("/manager/signup")
    fun signupManager(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ManagerSignupRequest
    ): ApiResponse<SignupCompleteResponse> {
        val response = userService.signupManager(userPrincipal.userId, request)
        return ApiResponse.success(response)
    }
    
    /**
     * 현재 사용자 정보 조회
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 사용자 타입에 따른 사용자 정보
     */
    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다. 사용자 타입(STAFF/MANAGER)에 따라 다른 정보를 반환합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<Any> {
        val userInfo = userService.getCurrentUserInfo(userPrincipal.userId)
        return ApiResponse.success(userInfo)
    }
    
    /**
     * STAFF 정보 수정
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @param request STAFF 정보 수정 요청
     * @return 수정 완료 메시지
     */
    @Operation(
        summary = "STAFF 정보 수정",
        description = "직원(STAFF) 정보를 수정합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PatchMapping("/staff")
    fun updateStaff(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: StaffUpdateRequest
    ): ApiResponse<MessageResponse> {
        userService.updateStaff(userPrincipal.userId, request)
        return ApiResponse.success(MessageResponse.staffInfoUpdated())
    }
    
    /**
     * MANAGER 정보 수정
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @param request MANAGER 정보 수정 요청
     * @return 수정 완료 메시지
     */
    @Operation(
        summary = "MANAGER 정보 수정",
        description = "관리자(MANAGER) 정보를 수정합니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @PatchMapping("/manager")
    fun updateManager(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ManagerUpdateRequest
    ): ApiResponse<MessageResponse> {
        userService.updateManager(userPrincipal.userId, request)
        return ApiResponse.success(MessageResponse.managerInfoUpdated())
    }
    
    /**
     * 회원 탈퇴
     * 
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 탈퇴 완료 메시지
     */
    @Operation(
        summary = "회원 탈퇴",
        description = "현재 사용자의 계정을 삭제합니다. MANAGER의 경우 관련된 채용공고, 출근기록 등도 함께 삭제됩니다.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @DeleteMapping
    fun deleteUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ApiResponse<MessageResponse> {
        userService.deleteUser(userPrincipal.userId)
        return ApiResponse.success(MessageResponse.userDeleted())
    }
} 