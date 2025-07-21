package com.millo.ilhayoung.user.dto

data class DashboardSummaryDto(
    val todayAttendance: Int,
    val totalStaff: Int,
    val activeJobs: Int,
    val pendingApplications: Int,
    val thisWeekSales: Int,
    val thisWeekWages: Int,
    val attendanceRate: Double
) 