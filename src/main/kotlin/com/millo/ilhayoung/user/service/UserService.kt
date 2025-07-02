package com.millo.ilhayoung.user.service

import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.domain.*
import com.millo.ilhayoung.user.dto.*
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import com.millo.ilhayoung.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * User 관련 비즈니스 로직을 담당하는 Service 클래스
 * 회원가입, 정보 수정, 조회, 삭제 기능
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val staffRepository: StaffRepository,
    private val managerRepository: ManagerRepository
) {
    
    /**
     * STAFF 회원가입 처리
     * 
     * @param userId 사용자 ID
     * @param request STAFF 회원가입 요청 정보
     * @throws BusinessException 이미 회원가입된 경우, 전화번호 중복 등
     */
    fun signupStaff(userId: String, request: StaffSignupRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        // 이미 회원가입이 완료된 경우
        if (!user.needAdditionalInfo) {
            throw RuntimeException("이미 회원가입이 완료된 사용자입니다.")
        }
        
        // 전화번호 중복 확인
        if (userRepository.existsByPhone(request.phone)) {
            throw RuntimeException(ErrorCode.PHONE_ALREADY_EXISTS.message)
        }
        
        // User 정보 업데이트
        val updatedUser = user.copy(
            name = request.name,
            birthDate = LocalDate.parse(request.birthDate),
            phone = request.phone,
            userType = UserType.STAFF,
            needAdditionalInfo = false
        )
        userRepository.save(updatedUser)
        
        // Staff 정보 생성
        val staff = Staff(
            userId = userId,
            address = request.address,
            experience = request.experience
        )
        staffRepository.save(staff)
    }
    
    /**
     * MANAGER 회원가입 처리
     * 
     * @param userId 사용자 ID
     * @param request MANAGER 회원가입 요청 정보
     * @throws BusinessException 이미 회원가입된 경우, 전화번호 중복, 사업자등록번호 중복 등
     */
    fun signupManager(userId: String, request: ManagerSignupRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        // 이미 회원가입이 완료된 경우
        if (!user.needAdditionalInfo) {
            throw RuntimeException("이미 회원가입이 완료된 사용자입니다.")
        }
        
        // 전화번호 중복 확인
        if (userRepository.existsByPhone(request.phone)) {
            throw RuntimeException(ErrorCode.PHONE_ALREADY_EXISTS.message)
        }
        
        // 사업자등록번호 중복 확인
        if (managerRepository.existsByBusinessNumber(request.businessNumber)) {
            throw RuntimeException("이미 등록된 사업자등록번호입니다.")
        }
        
        // User 정보 업데이트
        val updatedUser = user.copy(
            name = request.name,
            birthDate = LocalDate.parse(request.birthDate),
            phone = request.phone,
            userType = UserType.MANAGER,
            needAdditionalInfo = false
        )
        userRepository.save(updatedUser)
        
        // Manager 정보 생성
        val manager = Manager(
            userId = userId,
            businessAddress = request.businessAddress,
            businessNumber = request.businessNumber,
            businessType = request.businessType
        )
        managerRepository.save(manager)
    }
    
    /**
     * 현재 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 타입에 따른 응답 DTO
     * @throws BusinessException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getCurrentUserInfo(userId: String): Any {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        return when (user.userType) {
            UserType.STAFF -> {
                val staff = staffRepository.findByUserId(userId)
                    .orElseThrow { RuntimeException("Staff 정보를 찾을 수 없습니다.") }
                
                StaffUserResponse(
                    userId = user.id!!,
                    userType = user.userType!!.code,
                    name = user.name!!,
                    birthDate = user.birthDate.toString(),
                    phone = user.phone!!,
                    address = staff.address,
                    experience = staff.experience
                )
            }
            UserType.MANAGER -> {
                val manager = managerRepository.findByUserId(userId)
                    .orElseThrow { RuntimeException("Manager 정보를 찾을 수 없습니다.") }
                
                ManagerUserResponse(
                    userId = user.id!!,
                    userType = user.userType!!.code,
                    name = user.name!!,
                    birthDate = user.birthDate.toString(),
                    phone = user.phone!!,
                    businessAddress = manager.businessAddress,
                    businessNumber = manager.businessNumber,
                    businessType = manager.businessType
                )
            }
            else -> throw RuntimeException("사용자 타입이 설정되지 않았습니다.")
        }
    }
    
    /**
     * STAFF 정보 수정
     * 
     * @param userId 사용자 ID
     * @param request STAFF 정보 수정 요청
     * @throws BusinessException 사용자를 찾을 수 없는 경우, STAFF가 아닌 경우
     */
    fun updateStaff(userId: String, request: StaffUpdateRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        if (!user.isStaff()) {
            throw RuntimeException("STAFF 사용자만 수정할 수 있습니다.")
        }
        
        val staff = staffRepository.findByUserId(userId)
            .orElseThrow { RuntimeException("Staff 정보를 찾을 수 없습니다.") }
        
        // User 정보 업데이트 (전화번호가 있는 경우)
        request.phone?.let { newPhone ->
            if (newPhone != user.phone && userRepository.existsByPhone(newPhone)) {
                throw RuntimeException(ErrorCode.PHONE_ALREADY_EXISTS.message)
            }
            val updatedUser = user.copy(phone = newPhone)
            userRepository.save(updatedUser)
        }
        
        // Staff 정보 업데이트
        val updatedStaff = staff.copy(
            address = request.address ?: staff.address,
            experience = request.experience ?: staff.experience
        )
        staffRepository.save(updatedStaff)
    }
    
    /**
     * MANAGER 정보 수정
     * 
     * @param userId 사용자 ID
     * @param request MANAGER 정보 수정 요청
     * @throws BusinessException 사용자를 찾을 수 없는 경우, MANAGER가 아닌 경우
     */
    fun updateManager(userId: String, request: ManagerUpdateRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        if (!user.isManager()) {
            throw RuntimeException("MANAGER 사용자만 수정할 수 있습니다.")
        }
        
        val manager = managerRepository.findByUserId(userId)
            .orElseThrow { RuntimeException("Manager 정보를 찾을 수 없습니다.") }
        
        // User 정보 업데이트 (전화번호가 있는 경우)
        request.phone?.let { newPhone ->
            if (newPhone != user.phone && userRepository.existsByPhone(newPhone)) {
                throw RuntimeException(ErrorCode.PHONE_ALREADY_EXISTS.message)
            }
            val updatedUser = user.copy(phone = newPhone)
            userRepository.save(updatedUser)
        }
        
        // Manager 정보 업데이트
        val updatedManager = manager.copy(
            businessAddress = request.businessAddress ?: manager.businessAddress,
            businessType = request.businessType ?: manager.businessType
        )
        managerRepository.save(updatedManager)
    }
    
    /**
     * 사용자 삭제 (회원 탈퇴)
     * 
     * @param userId 사용자 ID
     * @throws BusinessException 사용자를 찾을 수 없는 경우
     */
    fun deleteUser(userId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException(ErrorCode.USER_NOT_FOUND.message) }
        
        // 사용자 타입에 따른 관련 데이터 삭제
        when (user.userType) {
            UserType.STAFF -> {
                staffRepository.deleteByUserId(userId)
            }
            UserType.MANAGER -> {
                managerRepository.deleteByUserId(userId)
                // TODO: 추후 Recruit, Attendance 등 관련 데이터 cascade 삭제 처리
            }
            else -> {
                // 추가 정보가 없는 경우 User만 삭제
            }
        }
        
        // User 삭제 (소프트 삭제)
        user.softDelete()
        userRepository.save(user)
    }
} 