package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 직원(Staff) 특화 정보를 담는 도메인 클래스
 * User 도메인과 별도로 관리되는 Staff 전용 정보를 포함
 */
@Document(collection = "staffs")
data class Staff(
    
    /**
     * 연결된 사용자 ID
     * User 도메인의 ID를 참조
     */
    @Indexed(unique = true)
    val userId: String,
    
    /**
     * 거주 주소
     * 직원의 거주지 주소
     */
    val address: String,
    
    /**
     * 경력/경험
     * 이전 근무 경험이나 관련 경력 사항
     */
    val experience: String,
    
    /**
     * 신뢰도 점수 (오름지수)
     * 0 ~ 100 점 (기본값: 50)
     */
    val trustScore: Int = 50,
    
    /**
     * 구직 활성화 여부
     * true: 구직 중, false: 구직 중단
     */
    val isJobSeeking: Boolean = true,
    
    /**
     * 선호 업종
     * 선호하는 업종 리스트
     */
    val preferredJobTypes: List<String> = emptyList(),
    
    /**
     * 자기소개
     * 간단한 자기소개 또는 어필 포인트
     */
    val introduction: String? = null
    
) : BaseDocument() {
    
    /**
     * 신뢰도 점수를 업데이트하는 메서드
     * 
     * @param scoreChange 점수 변동 (양수: 증가, 음수: 감소)
     * @return 신뢰도가 업데이트된 Staff 객체
     */
    fun updateTrustScore(scoreChange: Int): Staff {
        val newScore = (trustScore + scoreChange).coerceIn(0, 100)
        return this.copy(trustScore = newScore)
    }
    
    /**
     * 구직 상태를 변경하는 메서드
     * 
     * @param isActive 구직 활성화 여부
     * @return 구직 상태가 변경된 Staff 객체
     */
    fun updateJobSeekingStatus(isActive: Boolean): Staff {
        return this.copy(isJobSeeking = isActive)
    }
    
    /**
     * 신뢰도 등급을 반환하는 메서드
     * 
     * @return 신뢰도 등급 문자열
     */
    fun getTrustGrade(): String {
        return when {
            trustScore >= 90 -> "최우수"
            trustScore >= 80 -> "우수"
            trustScore >= 70 -> "양호"
            trustScore >= 60 -> "보통"
            trustScore >= 50 -> "미흡"
            else -> "부족"
        }
    }
    
    /**
     * 선호 업종을 업데이트하는 메서드
     * 
     * @param jobTypes 새로운 선호 업종 리스트
     * @return 선호 업종이 업데이트된 Staff 객체
     */
    fun updatePreferredJobTypes(jobTypes: List<String>): Staff {
        return this.copy(preferredJobTypes = jobTypes)
    }
    
    /**
     * 자기소개를 업데이트하는 메서드
     * 
     * @param introduction 새로운 자기소개
     * @return 자기소개가 업데이트된 Staff 객체
     */
    fun updateIntroduction(introduction: String?): Staff {
        return this.copy(introduction = introduction)
    }
} 