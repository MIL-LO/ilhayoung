package com.millo.ilhayoung.recruit.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 채용공고 도메인
 */
@Document(collection = "recruits")
data class Recruit(
    val title: String,                    // 공고 제목
    val workLocation: String,             // 근무지
    val salary: Long,                     // 급여
    val jobType: String,                  // 직무
    val position: String,                 // 직책
    val workSchedule: WorkSchedule,       // 근무일정
    val gender: String? = null,           // 성별 (선택)
    val description: String,              // 상세 설명
    val images: List<String> = emptyList(), // 이미지 URL 목록
    val deadline: LocalDateTime,          // 공고 마감 기한
    val paymentDate: String,              // 급여 정산일
    val managerId: String,                // 작성자 (MANAGER)
    val companyName: String,              // 기업명
    val companyAddress: String,           // 회사 주소
    val companyContact: String,           // 기업 연락처
    val representativeName: String,       // 대표자 이름
    val status: RecruitStatus = RecruitStatus.ACTIVE, // 공고 상태
    val applicationCount: Long = 0L,      // 지원자 수
    val viewCount: Long = 0,              // 조회수
    
    @Version
    val version: Long = 0,               // 버전 관리를 위한 필드
    
    @Id
    val id: String? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 