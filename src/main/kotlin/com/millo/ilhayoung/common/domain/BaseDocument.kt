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
} 