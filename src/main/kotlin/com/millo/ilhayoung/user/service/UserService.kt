package com.millo.ilhayoung.user.service

import com.millo.ilhayoung.auth.domain.OAuth
import com.millo.ilhayoung.auth.domain.RefreshToken
import com.millo.ilhayoung.auth.jwt.JwtTokenProvider
import com.millo.ilhayoung.auth.repository.RefreshTokenRepository
import com.millo.ilhayoung.auth.dto.SignupCompleteResponse
import com.millo.ilhayoung.common.exception.BusinessException
import com.millo.ilhayoung.common.exception.ErrorCode
import com.millo.ilhayoung.user.domain.*
import com.millo.ilhayoung.user.dto.*
import com.millo.ilhayoung.user.repository.ManagerRepository
import com.millo.ilhayoung.user.repository.StaffRepository
import com.millo.ilhayoung.auth.repository.OAuthRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

/**
 * User 관련 비즈니스 로직을 담당하는 Service 클래스
 * 회원가입, 정보 수정, 조회, 삭제 기능
 */
@Service
@Transactional
class UserService(
    private val oauthRepository: OAuthRepository,
    private val staffRepository: StaffRepository,
    private val managerRepository: ManagerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    
    /**
     * OAuth 사용자 존재 확인 및 반환 (공통 메서드)
     */
    private fun validateAndGetOAuthUser(userId: String): OAuth {
        return oauthRepository.findById(userId)
            .orElseThrow { BusinessException.userNotFound() }
    }
    
    /**
     * STAFF 회원가입 처리
     * 
     * @param userId 사용자 ID
     * @param request STAFF 회원가입 요청 정보
     * @return 새로운 JWT 토큰이 포함된 회원가입 완료 응답
     * @throws BusinessException 이미 회원가입된 경우, 전화번호 중복 등
     */
    fun signupStaff(userId: String, request: StaffSignupRequest): SignupCompleteResponse {
        val user = validateAndGetOAuthUser(userId)
        
        // 이미 STAFF로 회원가입이 완료된 경우
        if (staffRepository.existsByUserId(userId)) {
            throw BusinessException(ErrorCode.USER_ALREADY_EXISTS, "이미 STAFF로 회원가입이 완료된 사용자입니다.")
        }
        
        // 전화번호 중복 확인
        if (staffRepository.existsByPhone(request.phone)) {
            throw BusinessException.emailAlreadyExists()
        }
        
        // Staff 정보 생성 (회원가입 완료 상태로)
        val staff = Staff.create(
            oauth = user,
            birthDate = request.birthDate,
            phone = request.phone,
            address = request.address,
            experience = request.experience
        )
        val savedStaff = staffRepository.save(staff)
        
        // JWT 토큰 발급 (userType, status 포함)
        val accessToken = jwtTokenProvider.createAccessToken(
            userId = savedStaff.userId,
            userType = savedStaff.userType.code,
            status = savedStaff.status.code,
            email = savedStaff.getEmail()
        )
        
        // Refresh Token 생성 및 저장
        val refreshToken = jwtTokenProvider.createRefreshToken(savedStaff.userId)
        saveRefreshToken(savedStaff.userId, refreshToken)

        return SignupCompleteResponse(
            message = "STAFF 회원가입이 완료되었습니다.",
            userType = savedStaff.userType.code,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
    
    /**
     * MANAGER 회원가입 처리
     * 
     * @param userId 사용자 ID
     * @param request MANAGER 회원가입 요청 정보
     * @return 새로운 JWT 토큰이 포함된 회원가입 완료 응답
     * @throws BusinessException 이미 회원가입된 경우, 전화번호 중복, 사업자등록번호 중복 등
     */
    fun signupManager(userId: String, request: ManagerSignupRequest): SignupCompleteResponse {
        val user = validateAndGetOAuthUser(userId)
        
        // 이미 MANAGER로 회원가입이 완료된 경우
        if (managerRepository.existsByUserId(userId)) {
            throw BusinessException(ErrorCode.USER_ALREADY_EXISTS, "이미 MANAGER로 회원가입이 완료된 사용자입니다.")
        }
        
        // 전화번호 중복 확인
        if (managerRepository.existsByPhone(request.phone)) {
            throw BusinessException.emailAlreadyExists()
        }
        
        // 사업자등록번호 중복 확인
        if (managerRepository.existsByBusinessNumber(request.businessNumber)) {
            throw BusinessException(ErrorCode.INVALID_BUSINESS_NUMBER, "이미 등록된 사업자등록번호입니다.")
        }
        
        // Manager 정보 생성 (회원가입 완료 상태로)
        val manager = Manager.create(
            oauth = user,
            birthDate = request.birthDate,
            phone = request.phone,
            businessAddress = request.businessAddress,
            businessNumber = request.businessNumber,
            businessType = request.businessType
        )
        val savedManager = managerRepository.save(manager)
        
        // JWT 토큰 발급 (userType, status 포함)
        val accessToken = jwtTokenProvider.createAccessToken(
            userId = savedManager.userId,
            userType = savedManager.userType.code,
            status = savedManager.status.code,
            email = savedManager.getEmail()
        )
        
        // Refresh Token 생성 및 저장
        val refreshToken = jwtTokenProvider.createRefreshToken(savedManager.userId)
        saveRefreshToken(savedManager.userId, refreshToken)

        return SignupCompleteResponse(
            message = "MANAGER 회원가입이 완료되었습니다.",
            userType = savedManager.userType.code,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
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
        // OAuth 사용자 존재 확인
        validateAndGetOAuthUser(userId)

        val staffOpt = staffRepository.findByUserId(userId)
        if (staffOpt.isPresent) {
            val staff = staffOpt.get()
            return StaffUserResponse(
                userId = staff.userId,
                userType = staff.userType.code,
                name = staff.getName(),
                email = staff.getEmail(),
                provider = staff.getProvider(),
                providerId = staff.getProviderId(),
                birthDate = staff.birthDate,
                phone = staff.phone,
                address = staff.address,
                experience = staff.experience
            )
        }
        val managerOpt = managerRepository.findByUserId(userId)
        if (managerOpt.isPresent) {
            val manager = managerOpt.get()
            return ManagerUserResponse(
                userId = manager.userId,
                userType = manager.userType.code,
                name = manager.getName(),
                email = manager.getEmail(),
                provider = manager.getProvider(),
                providerId = manager.getProviderId(),
                birthDate = manager.birthDate,
                phone = manager.phone,
                businessAddress = manager.businessAddress,
                businessNumber = manager.businessNumber,
                businessType = manager.businessType
            )
        }
        throw BusinessException(ErrorCode.USER_NOT_ACTIVE, "사용자 타입이 설정되지 않았습니다.")
    }
    
    /**
     * STAFF 정보 수정
     * 
     * @param userId 사용자 ID
     * @param request STAFF 정보 수정 요청
     * @throws BusinessException 사용자를 찾을 수 없는 경우, STAFF가 아닌 경우
     */
    fun updateStaff(userId: String, request: StaffUpdateRequest) {
        // OAuth 사용자 존재 확인
        validateAndGetOAuthUser(userId)
        
        val staff = staffRepository.findByUserId(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "Staff 정보를 찾을 수 없습니다.") }
        
        // 전화번호 중복 확인
        request.phone?.let { newPhone ->
            if (newPhone != staff.phone && staffRepository.existsByPhone(newPhone)) {
                throw BusinessException.emailAlreadyExists()
            }
        }
        
        // Staff 정보 업데이트
        staff.update(
            phone = request.phone,
            address = request.address,
            experience = request.experience
        )
        staffRepository.save(staff)
    }
    
    /**
     * MANAGER 정보 수정
     * 
     * @param userId 사용자 ID
     * @param request MANAGER 정보 수정 요청
     * @throws BusinessException 사용자를 찾을 수 없는 경우, MANAGER가 아닌 경우
     */
    fun updateManager(userId: String, request: ManagerUpdateRequest) {
        // OAuth 사용자 존재 확인
        validateAndGetOAuthUser(userId)
        
        val manager = managerRepository.findByUserId(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND, "Manager 정보를 찾을 수 없습니다.") }
        
        // 전화번호 중복 확인
        request.phone?.let { newPhone ->
            if (newPhone != manager.phone && managerRepository.existsByPhone(newPhone)) {
                throw BusinessException.emailAlreadyExists()
            }
        }
        
        // Manager 정보 업데이트
        manager.update(
            phone = request.phone,
            businessAddress = request.businessAddress,
            businessType = request.businessType
        )
        managerRepository.save(manager)
    }
    
    /**
     * 사용자 삭제 (회원 탈퇴)
     * 
     * @param userId 사용자 ID
     * @throws BusinessException 사용자를 찾을 수 없는 경우
     */
    fun deleteUser(userId: String) {
        // OAuth 사용자 존재 확인
        validateAndGetOAuthUser(userId)
        
        // Staff 또는 Manager에서 상태 변경
        val staffOpt = staffRepository.findByUserId(userId)
        val managerOpt = managerRepository.findByUserId(userId)
        
        when {
            staffOpt.isPresent -> {
                val staff = staffOpt.get()
                staff.delete()
                staffRepository.save(staff)
            }
            managerOpt.isPresent -> {
                val manager = managerOpt.get()
                manager.delete()
                managerRepository.save(manager)
                // TODO: 추후 Recruit, Attendance 등 관련 데이터 cascade 삭제 처리
            }
            else -> {
                // 추가 정보가 없는 경우 OAuth만 삭제
                // OAuth는 삭제하지 않고 그대로 둠 (인증 정보 보존)
            }
        }
    }
    
    /**
     * Refresh Token을 Redis에 저장하는 메서드
     * Redis TTL로 자동 만료되므로 별도 만료 처리 불필요
     * 
     * @param userId 사용자 ID
     * @param refreshTokenValue 리프레시 토큰 값
     */
    private fun saveRefreshToken(userId: String, refreshTokenValue: String) {
        // 기존 리프레시 토큰들 삭제 (단일 세션 정책)
        refreshTokenRepository.deleteByUserId(userId)
        
        // 새로운 리프레시 토큰 저장
        val expiresAt = jwtTokenProvider.getExpiration(refreshTokenValue)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        
        val refreshToken = RefreshToken.create(
            token = refreshTokenValue,
            userId = userId,
            expiresAt = expiresAt
        )
        
        refreshTokenRepository.save(refreshToken)
    }
} 