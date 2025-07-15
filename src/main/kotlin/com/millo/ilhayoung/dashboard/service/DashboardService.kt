package com.millo.ilhayoung.dashboard.service

import com.millo.ilhayoung.dashboard.dto.EmployerDashboardResponse
import com.millo.ilhayoung.dashboard.dto.RecentActivityDto
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import com.millo.ilhayoung.recruit.repository.RecruitRepository
import com.millo.ilhayoung.recruit.repository.ApplicationRepository
import com.millo.ilhayoung.attendance.repository.AttendanceRecordRepository
import com.millo.ilhayoung.salary.repository.PayrollRecordRepository
import com.millo.ilhayoung.recruit.domain.RecruitStatus
import com.millo.ilhayoung.recruit.domain.ApplicationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

/**
 * 대시보드 관련 서비스
 */
@Service
class DashboardService(
    private val managerRepository: ManagerRepository,
    private val staffRepository: StaffRepository,
    private val recruitRepository: RecruitRepository,
    private val applicationRepository: ApplicationRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val payrollRecordRepository: PayrollRecordRepository
) {
    private val log = LoggerFactory.getLogger(DashboardService::class.java)

    /**
     * 사업자 대시보드 데이터 조회
     * 
     * @param userId 사용자 ID
     * @return 사업자 대시보드 데이터
     */
    @Transactional(readOnly = true)
    fun getEmployerDashboard(userId: String): EmployerDashboardResponse {
        log.info("[대시보드] 매니저 대시보드 조회 시작 - userId: $userId")
        
        // Manager 정보 조회 (null-safe)
        val managerOpt = managerRepository.findById(userId)
        if (!managerOpt.isPresent) {
            log.error("[대시보드] 매니저 정보 없음! userId: $userId (DB managers id와 불일치 가능)")
            return EmployerDashboardResponse(
                businessName = "미등록 사업장",
                ownerName = "미등록",
                todayAttendance = 0,
                totalStaff = 0,
                activeJobs = 0,
                pendingApplications = 0,
                thisWeekWages = 0,
                recentActivities = emptyList()
            )
        }
        val manager = managerOpt.get()
        log.info("[대시보드] 매니저 정보 조회 성공: ${manager.businessName}")
        
        // 오늘 날짜
        val today = LocalDate.now()
        
        // 현재 매니저의 사업장에서 근무하는 직원들의 오늘 출근 현황 조회
        val todayAttendance = try {
            attendanceRecordRepository.countByManagerIdAndDateAndStatus(userId, today, "CLOCKED_IN").toInt()
        } catch (e: Exception) {
            log.warn("[대시보드] 출근 기록 조회 실패, 기본값 사용: ${e.message}")
            0
        }
        log.info("[대시보드] 오늘 출근 인원: $todayAttendance")
        
        // 현재 매니저의 사업장에서 근무하는 전체 직원 수 조회
        // (매니저가 작성한 공고에 지원한 직원들)
        val totalStaff = try {
            getTotalStaffForManager(userId)
        } catch (e: Exception) {
            log.warn("[대시보드] 전체 직원 수 조회 실패, 기본값 사용: ${e.message}")
            0
        }
        log.info("[대시보드] 전체 직원 수: $totalStaff")
        
        // 활성 공고 수 조회 (현재 Manager가 작성한 공고만)
        val activeJobs = try {
            recruitRepository.countByManagerIdAndStatus(userId, RecruitStatus.ACTIVE).toInt()
        } catch (e: Exception) {
            log.warn("[대시보드] 활성 공고 수 조회 실패, 기본값 사용: ${e.message}")
            0
        }
        log.info("[대시보드] 활성 공고 수: $activeJobs")
        
        // 대기 중인 지원서 수 조회 (현재 Manager가 작성한 공고에 대한 지원서만)
        val pendingApplications = try {
            applicationRepository.countByRecruitManagerIdAndStatus(userId, ApplicationStatus.APPLIED).toInt()
        } catch (e: Exception) {
            log.warn("[대시보드] 대기 중인 지원서 수 조회 실패, 기본값 사용: ${e.message}")
            0
        }
        log.info("[대시보드] 대기 중인 지원서 수: $pendingApplications")
        
        // 이번 주 지급 할 급여 계산 (임시로 고정값 사용)
        val thisWeekWages = try {
            calculateThisWeekWages(userId)
        } catch (e: Exception) {
            log.warn("[대시보드] 급여 계산 실패, 기본값 사용: ${e.message}")
            0
        }
        log.info("[대시보드] 이번 주 급여: $thisWeekWages")
        
        // 최근 활동 목록 생성 (임시 데이터)
        val recentActivities = try {
            generateMockRecentActivities()
        } catch (e: Exception) {
            log.warn("[대시보드] 최근 활동 생성 실패, 기본값 사용: ${e.message}")
            emptyList()
        }
        
        val response = EmployerDashboardResponse(
            businessName = manager.businessName,
            ownerName = manager.oauth.getDisplayName(),
            todayAttendance = todayAttendance,
            totalStaff = totalStaff,
            activeJobs = activeJobs,
            pendingApplications = pendingApplications,
            thisWeekWages = thisWeekWages,
            recentActivities = recentActivities
        )
        
        log.info("[대시보드] 대시보드 데이터 생성 완료: $response")
        return response
    }
    
    /**
     * 이번 주 지급 할 급여 계산
     * 
     * @param userId 사용자 ID
     * @return 이번 주 급여 총액
     */
    private fun calculateThisWeekWages(userId: String): Int {
        // TODO: 실제 급여 계산 로직 구현
        // 현재는 임시로 고정값 반환
        return 680000
    }
    
    /**
     * 최근 활동 목록 생성 (임시 데이터)
     * 
     * @return 최근 활동 목록
     */
    private fun generateMockRecentActivities(): List<RecentActivityDto> {
        return listOf(
            RecentActivityDto(
                activity = "김○○님이 출근했어요",
                time = "30분 전",
                icon = "login",
                color = "green"
            ),
            RecentActivityDto(
                activity = "새로운 지원이 있어요",
                time = "1시간 전",
                icon = "person_add",
                color = "blue"
            ),
            RecentActivityDto(
                activity = "이○○님이 퇴근했어요",
                time = "2시간 전",
                icon = "logout",
                color = "orange"
            ),
            RecentActivityDto(
                activity = "공고가 게시되었어요",
                time = "3시간 전",
                icon = "work",
                color = "purple"
            )
        )
    }
    
    /**
     * 현재 매니저의 사업장에서 근무하는 전체 직원 수 조회
     * 
     * @param managerId 매니저 ID
     * @return 해당 매니저의 사업장에서 근무하는 직원 수
     */
    private fun getTotalStaffForManager(managerId: String): Int {
        try {
            // 현재 매니저가 작성한 활성 공고들 조회
            val activeRecruits = recruitRepository.findByManagerIdAndStatusIn(
                managerId, 
                listOf(RecruitStatus.ACTIVE), 
                org.springframework.data.domain.PageRequest.of(0, 1000)
            ).content
            
            if (activeRecruits.isEmpty()) {
                log.info("[대시보드] 매니저의 활성 공고 없음: $managerId")
                return 0
            }
            
            // 해당 공고들에 지원한 고유한 직원 수 계산
            val recruitIds = activeRecruits.map { it.id!! }
            val applications = applicationRepository.findByRecruitIdIn(recruitIds)
            
            // 고유한 staffId 개수 반환
            val uniqueStaffCount = applications.map { it.staffId }.distinct().size
            log.info("[대시보드] 매니저 {}의 고유 직원 수: {} (공고 수: {})", managerId, uniqueStaffCount, activeRecruits.size)
            return uniqueStaffCount
        } catch (e: Exception) {
            log.error("[대시보드] 직원 수 조회 중 오류 발생: ${e.message}", e)
            return 0
        }
    }
    
    /**
     * 시간 포맷팅 (예: "30분 전", "1시간 전")
     * 
     * @param dateTime 날짜시간
     * @return 포맷팅된 시간 문자열
     */
    private fun formatTimeAgo(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val diffMinutes = java.time.Duration.between(dateTime, now).toMinutes()
        
        return when {
            diffMinutes < 1 -> "방금 전"
            diffMinutes < 60 -> "${diffMinutes}분 전"
            diffMinutes < 1440 -> "${diffMinutes / 60}시간 전"
            else -> "${diffMinutes / 1440}일 전"
        }
    }
} 