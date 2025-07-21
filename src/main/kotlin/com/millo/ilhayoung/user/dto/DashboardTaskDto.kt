package com.millo.ilhayoung.user.dto

data class DashboardTaskDto(
    val title: String,
    val description: String,
    val type: String,
    val isUrgent: Boolean
) 