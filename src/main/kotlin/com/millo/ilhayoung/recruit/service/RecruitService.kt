package com.millo.ilhayoung.recruit.service

import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import com.millo.ilhayoung.recruit.domain.Recruit
import com.millo.ilhayoung.recruit.domain.RecruitStatus
import com.millo.ilhayoung.recruit.dto.*
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.user.repository.ManagerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
    fun createRecruit(userId: String, request: CreateRecruitRequest): RecruitResponse {
        // userId는 이미 Manager의 _id임
        val managerId = userId

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
            managerId = managerId, // Manager의 _id를 사용
            companyName = request.companyName,
            companyAddress = request.companyAddress,
            companyContact = request.companyContact,
            representativeName = request.representativeName,
            workStartDate = request.workStartDate,
            workEndDate = request.workEndDate,
            workDurationMonths = request.workDurationMonths,
            recruitmentCount = request.recruitmentCount
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

        // 삭제된 공고인지 확인
        if (recruit.status == RecruitStatus.DELETED) {
            throw BusinessException(ErrorCode.RECRUIT_DELETED)
        }

        // 조회수 증가
        val updatedRecruit = recruit.copy(viewCount = recruit.viewCount + 1)
        val savedRecruit = recruitRepository.save(updatedRecruit)

        return RecruitResponse.from(savedRecruit)
    }

    /**
     * Manager의 채용공고 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyRecruits(userId: String, pageable: Pageable): Page<RecruitSummaryResponse> {
        // userId는 이미 Manager의 _id임
        val managerId = userId

        // 정렬 순서 설정 (생성일 기준 내림차순)
        val pageRequest = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        )

        // Manager의 _id로 채용공고 조회
        val recruits = recruitRepository.findByManagerIdAndStatusIn(
            managerId,
            listOf(RecruitStatus.ACTIVE),
            pageRequest
        )

        return recruits.map { recruit ->
            try {
                // 각 공고의 지원자 수 업데이트
                val applicationCount = applicationRepository.countByRecruitId(recruit.id.orEmpty())
                val updatedRecruit = recruit.copy(applicationCount = applicationCount)
                RecruitSummaryResponse.from(updatedRecruit)
            } catch (e: Exception) {
                // 에러 로깅 및 기본값 반환
                RecruitSummaryResponse.from(recruit)
            }
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
            representativeName = request.representativeName ?: recruit.representativeName,
            workStartDate = request.workStartDate ?: recruit.workStartDate,
            workEndDate = request.workEndDate ?: recruit.workEndDate,
            workDurationMonths = request.workDurationMonths ?: recruit.workDurationMonths,
            recruitmentCount = request.recruitmentCount ?: recruit.recruitmentCount
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
     * 채용공고 삭제 (소프트 삭제)
     */
    fun deleteRecruit(recruitId: String, managerId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { BusinessException(ErrorCode.RECRUIT_NOT_FOUND) }

        // 작성자 확인
        if (recruit.managerId != managerId) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        // 이미 삭제된 공고인지 확인
        if (recruit.status == RecruitStatus.DELETED) {
            throw BusinessException(ErrorCode.RECRUIT_ALREADY_DELETED)
        }

        // 상태를 DELETED로 변경 (소프트 삭제)
        val deletedRecruit = recruit.copy(status = RecruitStatus.DELETED)
        recruitRepository.save(deletedRecruit)
    }

    /**
     * 인기/추천 공고 조회
     */
    @Transactional(readOnly = true)
    fun getFeaturedRecruits(pageable: Pageable): Page<RecruitSummaryResponse> {
        val activeStatuses = listOf<RecruitStatus>(RecruitStatus.ACTIVE)
        val recruits = recruitRepository.findByStatusInOrderByViewCountDescCreatedAtDesc(activeStatuses, pageable)
        return recruits.map { recruit -> RecruitSummaryResponse.from(recruit) }
    }

    /**
     * 지원 현황 요약 조회
     */
    @Transactional(readOnly = true)
    fun getApplicationSummary(managerId: String): List<ApplicationSummaryResponse> {
        val pageSize = 100 // 한 번에 가져올 데이터 수 제한
        var currentPage = 0
        val result = mutableListOf<ApplicationSummaryResponse>()
        
        while (true) {
            val pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val recruitsPage = recruitRepository.findByManagerIdAndStatusNotOrderByCreatedAtDesc(
                managerId,
                RecruitStatus.CLOSED,
                pageable
            )
            
            val pageResult = recruitsPage.content.map { recruit ->
                val recruitId = recruit.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "채용공고 ID가 없습니다.")
                
                val totalApplications = applicationRepository.countByRecruitId(recruitId)
                val reviewingCount = applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.APPLIED)
                val interviewCount = applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.INTERVIEW)
                val hiredCount = applicationRepository.countByRecruitIdAndStatus(recruitId, ApplicationStatus.HIRED)

                ApplicationSummaryResponse(
                    recruitId = recruitId,
                    recruitTitle = recruit.title,
                    totalApplications = totalApplications,
                    reviewingCount = reviewingCount,
                    interviewCount = interviewCount,
                    hiredCount = hiredCount,
                    createdAt = recruit.createdAt ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "생성일자가 없습니다."),
                    deadline = recruit.deadline
                )
            }
            
            result.addAll(pageResult)
            
            if (!recruitsPage.hasNext()) break
            currentPage++
        }
        
        return result
    }

} 