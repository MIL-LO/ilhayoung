package com.millo.ilhayoung.dashboard.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 사업자 대시보드 응답 DTO
 */
@Schema(description = "사업자 대시보드 응답")
data class EmployerDashboardResponse(
    
    /**
     * 사업장 이름
     */
    @Schema(description = "사업장 이름", example = "제주카페")
    @JsonProperty("businessName")
    val businessName: String,
    
    /**
     * 사업자 이름
     */
    @Schema(description = "사업자 이름", example = "김사업")
    @JsonProperty("ownerName")
    val ownerName: String,
    
    /**
     * 오늘 출근 인원
     */
    @Schema(description = "오늘 출근 인원", example = "5")
    @JsonProperty("todayAttendance")
    val todayAttendance: Int,
    
    /**
     * 전체 직원 수
     */
    @Schema(description = "전체 직원 수", example = "8")
    @JsonProperty("totalStaff")
    val totalStaff: Int,
    
    /**
     * 활성 공고 수
     */
    @Schema(description = "활성 공고 수", example = "3")
    @JsonProperty("activeJobs")
    val activeJobs: Int,
    
    /**
     * 대기 중인 지원서 수
     */
    @Schema(description = "대기 중인 지원서 수", example = "12")
    @JsonProperty("pendingApplications")
    val pendingApplications: Int,
    
    /**
     * 이번 주 지급 할 급여
     */
    @Schema(description = "이번 주 지급 할 급여", example = "680000")
    @JsonProperty("thisWeekWages")
    val thisWeekWages: Int,
    
    /**
     * 최근 활동 목록
     */
    @Schema(description = "최근 활동 목록")
    @JsonProperty("recentActivities")
    val recentActivities: List<RecentActivityDto>
)

/**
 * 최근 활동 DTO
 */
@Schema(description = "최근 활동")
data class RecentActivityDto(
    
    /**
     * 활동 내용
     */
    @Schema(description = "활동 내용", example = "김○○님이 출근했어요")
    @JsonProperty("activity")
    val activity: String,
    
    /**
     * 활동 시간
     */
    @Schema(description = "활동 시간", example = "30분 전")
    @JsonProperty("time")
    val time: String,
    
    /**
     * 아이콘 타입
     */
    @Schema(description = "아이콘 타입", example = "login")
    @JsonProperty("icon")
    val icon: String,
    
    /**
     * 색상
     */
    @Schema(description = "색상", example = "green")
    @JsonProperty("color")
    val color: String
) 