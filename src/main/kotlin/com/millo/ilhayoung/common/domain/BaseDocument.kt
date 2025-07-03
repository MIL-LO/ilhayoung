package com.millo.ilhayoung.common.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB Document의 기본이 되는 추상 클래스
 * 모든 Document는 이 클래스를 상속받아 공통 필드를 사용
 */
@Document
abstract class BaseDocument {
    
    /**
     * MongoDB의 고유 식별자
     */
    @Id
    var id: String? = null
    
    /**
     * 문서 생성 일시
     * MongoDB의 @CreatedDate 어노테이션을 통해 자동으로 설정
     */
    @CreatedDate
    var createdAt: LocalDateTime? = null
    
    /**
     * 문서 수정 일시
     * MongoDB의 @LastModifiedDate 어노테이션을 통해 자동으로 설정
     */
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
    
    /**
     * 문서 삭제 여부 (소프트 삭제)
     * true: 삭제됨, false: 활성 상태
     */
    var isDeleted: Boolean = false
    
    /**
     * 문서 삭제 일시
     * isDeleted가 true로 변경될 때 설정
     */
    var deletedAt: LocalDateTime? = null
    
    /**
     * 소프트 삭제를 수행하는 메서드
     * isDeleted를 true로 설정하고 deletedAt에 현재 시간을 기록
     */
    fun softDelete() {
        this.isDeleted = true
        this.deletedAt = LocalDateTime.now()
    }
    
    /**
     * 소프트 삭제를 복구하는 메서드
     * isDeleted를 false로 설정하고 deletedAt을 null로 초기화
     */
    fun restore() {
        this.isDeleted = false
        this.deletedAt = null
    }
} 