package com.millo.ilhayoung.user.domain

import com.millo.ilhayoung.common.domain.BaseDocument
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 매니저(사업자) 특화 정보를 담는 도메인 클래스
 * User 도메인과 별도로 관리되는 Manager 전용 정보를 포함
 */
@Document(collection = "managers")
data class Manager(
    
    /**
     * 연결된 사용자 ID
     * User 도메인의 ID를 참조
     */
    @Indexed(unique = true)
    val userId: String,
    
    /**
     * 사업지 주소
     * 사업장이 위치한 실제 주소
     */
    val businessAddress: String,
    
    /**
     * 사업자등록번호
     * 10자리 숫자 (하이픈 포함 또는 제외)
     */
    @Indexed(unique = true)
    val businessNumber: String,
    
    /**
     * 업종
     * 예: 요식업, 카페, 소매업 등
     */
    val businessType: String,
    
    /**
     * 사업자등록번호 검증 상태
     * true: 검증 완료, false: 검증 필요 또는 실패
     */
    val isBusinessNumberVerified: Boolean = false,
    
    /**
     * 사업장명
     * 실제 운영하는 사업장의 이름
     */
    val businessName: String? = null,
    
    /**
     * 사업 설명
     * 사업에 대한 간단한 설명이나 소개
     */
    val businessDescription: String? = null,
    
    /**
     * 직원 모집 활성화 여부
     * true: 직원 모집 중, false: 모집 중단
     */
    val isRecruitingActive: Boolean = true,
    
    /**
     * 신뢰도 점수
     * 1.0 ~ 5.0 점 (기본값: 3.0)
     */
    val trustScore: Double = 3.0,
    
    /**
     * 평가 받은 횟수
     * 신뢰도 점수 계산을 위한 평가 횟수
     */
    val reviewCount: Int = 0
    
) : BaseDocument() {
    
    /**
     * 포맷팅된 사업자등록번호를 반환하는 메서드
     * 
     * @return xxx-xx-xxxxx 형식의 사업자등록번호
     */
    fun getFormattedBusinessNumber(): String {
        val numbers = businessNumber.replace("-", "")
        return if (numbers.length == 10) {
            "${numbers.substring(0, 3)}-${numbers.substring(3, 5)}-${numbers.substring(5)}"
        } else {
            businessNumber
        }
    }
    
    /**
     * 사업자등록번호 검증을 완료 처리하는 메서드
     * 
     * @return 검증 상태가 변경된 Manager 객체
     */
    fun verifyBusinessNumber(): Manager {
        return this.copy(isBusinessNumberVerified = true)
    }
    
    /**
     * 신뢰도 점수를 업데이트하는 메서드
     * 
     * @param newScore 새로운 평가 점수 (1.0 ~ 5.0)
     * @return 신뢰도가 업데이트된 Manager 객체
     */
    fun updateTrustScore(newScore: Double): Manager {
        require(newScore in 1.0..5.0) { "신뢰도 점수는 1.0에서 5.0 사이여야 합니다." }
        
        val totalScore = (trustScore * reviewCount) + newScore
        val newReviewCount = reviewCount + 1
        val newTrustScore = totalScore / newReviewCount
        
        return this.copy(
            trustScore = newTrustScore,
            reviewCount = newReviewCount
        )
    }
    
    /**
     * 직원 모집 상태를 변경하는 메서드
     * 
     * @param isActive 모집 활성화 여부
     * @return 모집 상태가 변경된 Manager 객체
     */
    fun updateRecruitingStatus(isActive: Boolean): Manager {
        return this.copy(isRecruitingActive = isActive)
    }
    
    /**
     * 신뢰도 등급을 반환하는 메서드
     * 
     * @return 신뢰도 등급 문자열
     */
    fun getTrustGrade(): String {
        return when {
            trustScore >= 4.5 -> "최우수"
            trustScore >= 4.0 -> "우수"
            trustScore >= 3.5 -> "보통"
            trustScore >= 3.0 -> "미흡"
            else -> "부족"
        }
    }
    
    /**
     * 사업자 정보가 완전히 등록되었는지 확인하는 메서드
     * 
     * @return 모든 필수 정보가 입력되었으면 true
     */
    fun isFullyRegistered(): Boolean {
        return businessName != null && 
               businessDescription != null && 
               isBusinessNumberVerified
    }
} 