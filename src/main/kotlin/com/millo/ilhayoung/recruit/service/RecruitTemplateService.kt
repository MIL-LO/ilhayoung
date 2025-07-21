package com.millo.ilhayoung.recruit.service

import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.RecruitTemplate
import com.millo.ilhayoung.recruit.dto.CreateTemplateRequest
import com.millo.ilhayoung.recruit.dto.TemplateResponse
import com.millo.ilhayoung.recruit.repository.RecruitTemplateRepository
import com.millo.ilhayoung.user.repository.ManagerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채용공고 템플릿 서비스
 */
@Service
@Transactional
class RecruitTemplateService(
    private val templateRepository: RecruitTemplateRepository,
    private val managerRepository: ManagerRepository
) {

    /**
     * 템플릿 생성
     */
    fun createTemplate(managerId: String, request: CreateTemplateRequest): TemplateResponse {
        // Manager 존재 확인
        val manager = managerRepository.findById(managerId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val template = RecruitTemplate(
            managerId = managerId,
            name = request.name,
            title = request.title,
            workSchedule = request.workSchedule.toDomain()
        )

        val savedTemplate = templateRepository.save(template)
        return TemplateResponse.from(savedTemplate)
    }

    /**
     * Manager의 템플릿 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyTemplates(managerId: String, pageable: Pageable): Page<TemplateResponse> {
        val templates = templateRepository.findByManagerIdOrderByCreatedAtDesc(managerId, pageable)
        return templates.map { TemplateResponse.from(it) }
    }

    /**
     * Manager의 모든 템플릿 조회 (페이징 없음)
     */
    @Transactional(readOnly = true)
    fun getAllMyTemplates(managerId: String): List<TemplateResponse> {
        val templates = templateRepository.findByManagerIdOrderByCreatedAtDesc(managerId)
        return templates.map { TemplateResponse.from(it) }
    }

    /**
     * 템플릿 상세 조회
     */
    @Transactional(readOnly = true)
    fun getTemplate(templateId: String, managerId: String): TemplateResponse {
        val template = templateRepository.findById(templateId)
            .orElseThrow { BusinessException(ErrorCode.TEMPLATE_NOT_FOUND) }

        // 권한 확인
        if (template.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        return TemplateResponse.from(template)
    }

    /**
     * 템플릿 수정
     */
    fun updateTemplate(templateId: String, managerId: String, request: CreateTemplateRequest): TemplateResponse {
        val template = templateRepository.findById(templateId)
            .orElseThrow { BusinessException(ErrorCode.TEMPLATE_NOT_FOUND) }

        // 권한 확인
        if (template.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val updatedTemplate = template.copy(
            name = request.name,
            title = request.title,
            workSchedule = request.workSchedule.toDomain()
        )

        val savedTemplate = templateRepository.save(updatedTemplate)
        return TemplateResponse.from(savedTemplate)
    }

    /**
     * 템플릿 삭제
     */
    fun deleteTemplate(templateId: String, managerId: String) {
        val template = templateRepository.findById(templateId)
            .orElseThrow { BusinessException(ErrorCode.TEMPLATE_NOT_FOUND) }

        // 권한 확인
        if (template.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        templateRepository.deleteById(templateId)
    }

    /**
     * 템플릿 이름으로 검색
     */
    @Transactional(readOnly = true)
    fun searchTemplatesByName(managerId: String, name: String): List<TemplateResponse> {
        val templates = templateRepository.findByManagerIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(managerId, name)
        return templates.map { TemplateResponse.from(it) }
    }
} 