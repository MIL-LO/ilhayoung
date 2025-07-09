package com.millo.ilhayoung.recruit.service

import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.Application
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import com.millo.ilhayoung.recruit.domain.RecruitStatus
import com.millo.ilhayoung.recruit.dto.*
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 지원 서비스
 */
@Service
@Transactional
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val recruitRepository: RecruitRepository,
    private val staffRepository: StaffRepository
) {

    /**
     * 채용공고 지원
     */
    fun applyToRecruit(recruitId: String, staffId: String, request: CreateApplicationRequest): ApplicationResponse {
        // Staff 존재 확인
        val staff = staffRepository.findById(staffId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 채용공고 존재 및 상태 확인
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        if (recruit.status != RecruitStatus.ACTIVE) {
            throw BusinessException(ErrorCode.RECRUIT_CLOSED)
        }

        // 중복 지원 확인
        if (applicationRepository.existsByRecruitIdAndStaffId(recruitId, staffId)) {
            throw BusinessException(ErrorCode.ALREADY_APPLIED)
        }

        // 마감일 확인
        if (recruit.deadline.isBefore(java.time.LocalDateTime.now())) {
            throw BusinessException(ErrorCode.RECRUIT_CLOSED)
        }

        val application = Application(
            recruitId = recruitId,
            staffId = staffId,
            name = request.name,
            birthDate = request.birthDate,
            contact = request.contact,
            address = request.address,
            experience = request.experience,
            climateScore = request.climateScore ?: 0
        )

        val savedApplication = applicationRepository.save(application)

        // 채용공고의 지원자 수 업데이트
        val applicationCount = applicationRepository.countByRecruitId(recruitId)
        val updatedRecruit = recruit.copy(applicationCount = applicationCount)
        recruitRepository.save(updatedRecruit)

        return ApplicationResponse.from(savedApplication)
    }

    /**
     * 지원 내역 조회 (Staff용)
     */
    @Transactional(readOnly = true)
    fun getMyApplications(staffId: String, pageable: Pageable): Page<ApplicationHistoryResponse> {
        val applications = applicationRepository.findByStaffIdOrderByCreatedAtDesc(staffId, pageable)

        return applications.map { application ->
            val recruit = recruitRepository.findById(application.recruitId)
                .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

            ApplicationHistoryResponse(
                id = application.id!!,
                recruitTitle = recruit.title,
                companyName = recruit.companyName,
                status = application.status,
                appliedAt = application.createdAt!!,
                recruitDeadline = recruit.deadline
            )
        }
    }

    /**
     * 지원자 목록 조회 (Manager용)
     */
    @Transactional(readOnly = true)
    fun getRecruitApplications(recruitId: String, managerId: String, pageable: Pageable): Page<ApplicantInfoResponse> {
        // 채용공고 존재 및 권한 확인
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val applications = applicationRepository.findByRecruitIdOrderByCreatedAtDesc(recruitId, pageable)
        return applications.map { ApplicantInfoResponse.from(it) }
    }

    /**
     * 지원 상태 변경 (Manager용)
     */
    fun updateApplicationStatus(
        applicationId: String, 
        managerId: String, 
        request: UpdateApplicationStatusRequest
    ): ApplicationResponse {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { BusinessException(ErrorCode.APPLICATION_NOT_FOUND) }

        // 채용공고 권한 확인
        val recruit = recruitRepository.findById(application.recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val updatedApplication = application.copy(status = request.status)
        val savedApplication = applicationRepository.save(updatedApplication)

        return ApplicationResponse.from(savedApplication)
    }

    /**
     * 지원 취소 (Staff용)
     */
    fun cancelApplication(applicationId: String, staffId: String) {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { BusinessException(ErrorCode.APPLICATION_NOT_FOUND) }

        // 지원자 본인 확인
        if (application.staffId != staffId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        // 이미 처리된 지원은 취소 불가
        if (application.status in listOf(ApplicationStatus.HIRED, ApplicationStatus.REJECTED)) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        applicationRepository.deleteById(applicationId)

        // 채용공고의 지원자 수 업데이트
        val applicationCount = applicationRepository.countByRecruitId(application.recruitId)
        val recruit = recruitRepository.findById(application.recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }
        val updatedRecruit = recruit.copy(applicationCount = applicationCount)
        recruitRepository.save(updatedRecruit)
    }

    /**
     * 지원서 상세 조회
     */
    @Transactional(readOnly = true)
    fun getApplication(applicationId: String, userId: String): ApplicationResponse {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { BusinessException(ErrorCode.APPLICATION_NOT_FOUND) }

        // 권한 확인 (지원자 본인 또는 해당 공고 Manager)
        val recruit = recruitRepository.findById(application.recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        if (application.staffId != userId && recruit.managerId != userId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        return ApplicationResponse.from(application)
    }

    /**
     * 특정 상태의 지원자 목록 조회 (Manager용)
     */
    @Transactional(readOnly = true)
    fun getRecruitApplicationsByStatus(
        recruitId: String, 
        managerId: String, 
        status: ApplicationStatus, 
        pageable: Pageable
    ): Page<ApplicantInfoResponse> {
        // 채용공고 존재 및 권한 확인
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val applications = applicationRepository.findByRecruitIdAndStatusOrderByCreatedAtDesc(recruitId, status, pageable)
        return applications.map { ApplicantInfoResponse.from(it) }
    }
} 