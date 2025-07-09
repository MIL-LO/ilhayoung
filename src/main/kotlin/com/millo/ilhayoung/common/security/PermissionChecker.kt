package com.millo.ilhayoung.common.security

import com.millo.ilhayoung.auth.repository.OAuthRepository
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.domain.UserType
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * 실시간 권한 체크 유틸리티
 * 현업에서 가장 많이 사용하는 패턴: JWT에 권한을 넣지 않고 DB에서 실시간으로 확인
 */
@Component
class PermissionChecker(
    private val oauthRepository: OAuthRepository,
    private val staffRepository: StaffRepository,
    private val managerRepository: ManagerRepository
) {
    
    /**
     * 현재 인증된 사용자의 ID를 가져옵니다.
     */
    fun getCurrentUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }
    
    /**
     * 현재 사용자가 STAFF 권한이 있는지 확인합니다.
     */
    fun requireStaffPermission() {
        val userId = getCurrentUserId()
        val staff = staffRepository.findById(userId)
        
        if (staff.isEmpty || !staff.get().isActive()) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }
    
    /**
     * 현재 사용자가 MANAGER 권한이 있는지 확인합니다.
     */
    fun requireManagerPermission() {
        val userId = getCurrentUserId()
        val manager = managerRepository.findById(userId)
        
        if (manager.isEmpty || !manager.get().isActive()) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }
    
    /**
     * 현재 사용자가 특정 사용자인지 확인합니다. (본인 정보 접근용)
     */
    fun requireSameUser(targetUserId: String) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != targetUserId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }
    
    /**
     * 현재 사용자의 UserType을 가져옵니다.
     */
    fun getCurrentUserType(): UserType? {
        val userId = getCurrentUserId()
        val staff = staffRepository.findById(userId)
        val manager = managerRepository.findById(userId)
        
        return when {
            staff.isPresent && staff.get().isActive() -> UserType.STAFF
            manager.isPresent && manager.get().isActive() -> UserType.MANAGER
            else -> null
        }
    }
} 