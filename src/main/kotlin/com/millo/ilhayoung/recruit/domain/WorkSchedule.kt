package com.millo.ilhayoung.recruit.domain

/**
 * 근무 일정
 */
data class WorkSchedule(
    val days: List<String>,              // 근무 요일 (월, 화, 수, 목, 금, 토, 일)
    val startTime: String,               // 시작 시간 (HH:mm 형식)
    val endTime: String,                 // 종료 시간 (HH:mm 형식)
    val workPeriod: WorkPeriod           // 근무 기간
) 