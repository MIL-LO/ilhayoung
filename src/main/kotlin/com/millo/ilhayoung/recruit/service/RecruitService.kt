package com.millo.ilhayoung.recruit.service

import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.*
import com.millo.ilhayoung.recruit.dto.*
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.user.domain.Manager
import com.millo.ilhayoung.user.repository.ManagerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채용공고 서비스
 */
@Service
@Transactional
class RecruitService(
    private val recruitRepository: RecruitRepository,
    private val applicationRepository: ApplicationRepository,
    private val managerRepository: ManagerRepository
) {

    /**
     * 채용공고 등록
     */
    fun createRecruit(managerId: String, request: CreateRecruitRequest): RecruitResponse {
        // Manager 존재 확인
        val manager = managerRepository.findById(managerId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val recruit = Recruit(
            title = request.title,
            workLocation = request.workLocation,
            salary = request.salary,
            jobType = request.jobType,
            position = request.position,
            workSchedule = request.workSchedule.toDomain(),
            gender = request.gender,
            description = request.description,
            images = request.images,
            deadline = request.deadline,
            paymentDate = request.paymentDate,
            managerId = managerId,
            companyName = request.companyName,
            companyAddress = request.companyAddress,
            companyContact = request.companyContact,
            representativeName = request.representativeName
        )

        val savedRecruit = recruitRepository.save(recruit)
        return RecruitResponse.from(savedRecruit)
    }

    /**
     * 채용공고 목록 조회 (검색/필터링)
     */
    @Transactional(readOnly = true)
    fun getRecruits(searchRequest: RecruitSearchRequest): Page<RecruitSummaryResponse> {
        val activeStatuses = listOf(RecruitStatus.ACTIVE)
        val pageable = searchRequest.toPageable()

        val recruits = when {
            // 키워드 검색
            !searchRequest.keyword.isNullOrBlank() -> {
                recruitRepository.findByTitleContainingIgnoreCaseAndStatusIn(searchRequest.keyword, activeStatuses, pageable)
            }
            // 지역 검색
            !searchRequest.location.isNullOrBlank() -> {
                recruitRepository.findByWorkLocationContainingIgnoreCaseAndStatusIn(searchRequest.location, activeStatuses, pageable)
            }
            // 근무기간 검색
            searchRequest.workPeriod != null -> {
                recruitRepository.findByWorkScheduleWorkPeriodAndStatusIn(searchRequest.workPeriod, activeStatuses, pageable)
            }
            // 급여 범위 검색
            searchRequest.minSalary != null && searchRequest.maxSalary != null -> {
                recruitRepository.findBySalaryBetweenAndStatusIn(searchRequest.minSalary, searchRequest.maxSalary, activeStatuses, pageable)
            }
            // 직무 검색
            !searchRequest.jobType.isNullOrBlank() -> {
                recruitRepository.findByJobTypeContainingIgnoreCaseAndStatusIn(searchRequest.jobType, activeStatuses, pageable)
            }
            // 기본 목록 조회
            else -> {
                recruitRepository.findByStatusInOrderByCreatedAtDesc(activeStatuses, pageable)
            }
        }

        return recruits.map { RecruitSummaryResponse.from(it) }
    }

    /**
     * 채용공고 상세 조회
     */
    @Transactional(readOnly = true)
    fun getRecruit(recruitId: String): RecruitResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        // 조회수 증가
        val updatedRecruit = recruit.copy(viewCount = recruit.viewCount + 1)
        recruitRepository.save(updatedRecruit)

        return RecruitResponse.from(recruit)
    }

    /**
     * Manager의 채용공고 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyRecruits(managerId: String, pageable: Pageable): Page<RecruitSummaryResponse> {
        val recruits = recruitRepository.findByManagerIdAndStatusNotOrderByCreatedAtDesc(
            managerId, 
            RecruitStatus.CLOSED, 
            pageable
        )

        return recruits.map { recruit ->
            // 각 공고의 지원자 수 업데이트
            val recruitId = recruit.id ?: ""
            val applicationCount = if (recruitId.isNotEmpty()) {
                applicationRepository.countByRecruitId(recruitId).toInt()
            } else 0
            val updatedRecruit = recruit.copy(applicationCount = applicationCount)
            RecruitSummaryResponse.from(updatedRecruit)
        }
    }

    /**
     * 채용공고 수정
     */
    fun updateRecruit(recruitId: String, managerId: String, request: UpdateRecruitRequest): RecruitResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        // 작성자 확인
        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val updatedRecruit = recruit.copy(
            title = request.title ?: recruit.title,
            workLocation = request.workLocation ?: recruit.workLocation,
            salary = request.salary ?: recruit.salary,
            jobType = request.jobType ?: recruit.jobType,
            position = request.position ?: recruit.position,
            workSchedule = request.workSchedule?.toDomain() ?: recruit.workSchedule,
            gender = request.gender ?: recruit.gender,
            description = request.description ?: recruit.description,
            images = request.images ?: recruit.images,
            deadline = request.deadline ?: recruit.deadline,
            paymentDate = request.paymentDate ?: recruit.paymentDate,
            companyName = request.companyName ?: recruit.companyName,
            companyAddress = request.companyAddress ?: recruit.companyAddress,
            companyContact = request.companyContact ?: recruit.companyContact,
            representativeName = request.representativeName ?: recruit.representativeName
        )

        val savedRecruit = recruitRepository.save(updatedRecruit)
        return RecruitResponse.from(savedRecruit)
    }

    /**
     * 채용공고 상태 변경
     */
    fun updateRecruitStatus(recruitId: String, managerId: String, request: UpdateRecruitStatusRequest): RecruitResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        // 작성자 확인
        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val updatedRecruit = recruit.copy(status = request.status)
        val savedRecruit = recruitRepository.save(updatedRecruit)
        return RecruitResponse.from(savedRecruit)
    }

    /**
     * 채용공고 삭제
     */
    fun deleteRecruit(recruitId: String, managerId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        // 작성자 확인
        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        // 지원자가 있는 경우 삭제 불가
        val applicationCount = applicationRepository.countByRecruitId(recruitId)
        if (applicationCount > 0) {
            throw BusinessException(ErrorCode.RECRUIT_HAS_APPLICATIONS)
        }

        recruitRepository.deleteById(recruitId)
    }

    /**
     * 인기/추천 공고 조회
     */
    @Transactional(readOnly = true)
    fun getFeaturedRecruits(pageable: Pageable): Page<RecruitSummaryResponse> {
        val activeStatuses = listOf(RecruitStatus.ACTIVE)
        val recruits = recruitRepository.findByStatusInOrderByViewCountDescCreatedAtDesc(activeStatuses, pageable)
        return recruits.map { RecruitSummaryResponse.from(it) }
    }

    /**
     * 지원 현황 요약 조회
     */
    @Transactional(readOnly = true)
    fun getApplicationSummary(managerId: String): List<ApplicationSummaryResponse> {
        // Manager의 모든 공고 조회
        val allRecruits = recruitRepository.findByManagerIdAndStatusNotOrderByCreatedAtDesc(
            managerId, 
            RecruitStatus.CLOSED, 
            PageRequest.of(0, 1000)
        ).content

        return allRecruits.map { recruit ->
            val recruitId = recruit.id ?: ""
            val totalApplications = if (recruitId.isNotEmpty()) {
                applicationRepository.countByRecruitId(recruitId).toInt()
            } else 0
            val reviewingCount = if (recruitId.isNotEmpty()) {
                applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.REVIEWING).toInt()
            } else 0
            val interviewCount = if (recruitId.isNotEmpty()) {
                applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.INTERVIEW).toInt()
            } else 0
            val hiredCount = if (recruitId.isNotEmpty()) {
                applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.HIRED).toInt()
            } else 0

            ApplicationSummaryResponse(
                recruitId = recruitId,
                recruitTitle = recruit.title,
                totalApplications = totalApplications,
                reviewingCount = reviewingCount,
                interviewCount = interviewCount,
                hiredCount = hiredCount,
                createdAt = recruit.createdAt!!,
                deadline = recruit.deadline
            )
        }
    }
} 