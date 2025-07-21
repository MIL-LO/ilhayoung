package com.millo.ilhayoung.recruit.repository

import com.millo.ilhayoung.recruit.domain.RecruitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * 채용공고 템플릿 리포지토리
 */
@Repository
interface RecruitTemplateRepository : MongoRepository<RecruitTemplate, String> {

    /**
     * Manager의 템플릿 목록 조회
     */
    fun findByManagerIdOrderByCreatedAtDesc(managerId: String, pageable: Pageable): Page<RecruitTemplate>

    /**
     * Manager의 템플릿 목록 조회 (전체)
     */
    fun findByManagerIdOrderByCreatedAtDesc(managerId: String): List<RecruitTemplate>

    /**
     * Manager의 템플릿 수 조회
     */
    fun countByManagerId(managerId: String): Long

    /**
     * 템플릿 이름으로 검색 (Manager별)
     */
    fun findByManagerIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(managerId: String, name: String): List<RecruitTemplate>
} 