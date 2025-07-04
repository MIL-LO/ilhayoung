package com.millo.ilhayoung.user.domain

/**
 * Staff와 Manager의 공통 메서드를 정의하는 인터페이스
 * 중복 코드를 제거하고 일관성을 보장합니다.
 */
interface User {
    
    /**
     * OAuth에서 이메일 가져오기
     */
    fun getEmail(): String
    
    /**
     * OAuth에서 제공자 정보 가져오기
     */
    fun getProvider(): String
    
    /**
     * OAuth에서 제공자 ID 가져오기
     */
    fun getProviderId(): String
    
    /**
     * OAuth에서 이름 가져오기
     */
    fun getName(): String
    
    /**
     * 사용자가 활성 상태인지 확인
     */
    fun isActive(): Boolean
    
    /**
     * 사용자가 대기 상태인지 확인 (회원가입 필요)
     */
    fun isPending(): Boolean
    
    /**
     * 사용자가 삭제된 상태인지 확인
     */
    fun isDeleted(): Boolean
    
    /**
     * 회원가입 완료 처리
     */
    fun completeSignup()
    
    /**
     * 회원 탈퇴 처리
     */
    fun delete()
    
    /**
     * 회원 복구 처리
     */
    fun restore()
} 