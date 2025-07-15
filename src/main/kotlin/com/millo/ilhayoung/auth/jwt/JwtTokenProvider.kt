package com.millo.ilhayoung.auth.jwt

import com.millo.ilhayoung.auth.repository.BlacklistedTokenRepository
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.Key
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 * Access Token과 Refresh Token의 생성, 검증, 파싱 기능을 제공한다.
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.access-token-validity-in-seconds}") private val accessTokenValidityInSeconds: Long,
    @Value("\${jwt.refresh-token-validity-in-seconds}") private val refreshTokenValidityInSeconds: Long,
    private val blacklistedTokenRepository: BlacklistedTokenRepository
) {

    private val key: Key by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    /**
     * Access Token 생성 (userType, status 포함)
     * 
     * @param userId 사용자 ID
     * @param userType 사용자 타입 (STAFF/MANAGER)
     * @param status 사용자 상태 (PENDING/ACTIVE/DELETED)
     * @param email 사용자 이메일
     * @return JWT Access Token
     */
    fun createAccessToken(userId: String, userType: String, status: String, email: String): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidityInSeconds * 1000)

        return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("userType", userType)
            .claim("status", status)
            .claim("tokenType", "ACCESS")
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Refresh Token 생성
     * 
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    fun createRefreshToken(userId: String): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidityInSeconds * 1000)

        return Jwts.builder()
            .setSubject(userId)
            .claim("tokenType", "REFRESH")
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰에서 Authentication 객체 생성
     * 
     * @param token JWT 토큰
     * @return Spring Security Authentication 객체
     */
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val userId = claims.subject
        val email = claims["email"] as? String
        val userTypeCode = claims["userType"] as? String
        val status = claims["status"] as? String
        
        // userType을 먼저 파싱
        val userType = userTypeCode?.let { com.millo.ilhayoung.user.domain.UserType.fromCode(it) }
        
        // PENDING 상태일 때는 권한을 제한하되, userType은 유지
        val authorities = if (status == "PENDING") {
            emptyList() // PENDING 상태에서는 권한 없음
        } else {
            userType?.let { listOf(SimpleGrantedAuthority("ROLE_${it.code}")) } ?: emptyList()
        }

        val principal = UserPrincipal(
            userId = userId,
            email = email ?: "",
            userType = userType
        )

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    /**
     * 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    fun getUserId(token: String): String {
        return parseClaims(token).subject
    }

    /**
     * 토큰 유효성 검증 (블랙리스트 체크 포함)
     * 
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean {
        return try {
            // JWT 기본 검증 먼저 수행
            val claims = parseClaims(token)
            val isValid = !claims.expiration.before(Date())
            
            if (!isValid) {
                return false
            }
            
            // 블랙리스트 체크 (해시 기반)
            val tokenHash = hashToken(token)
            val isBlacklisted = blacklistedTokenRepository.existsByToken(tokenHash)
            
            if (isBlacklisted) {
                return false
            }
            
            true
            
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: Exception) {
            // 블랙리스트 체크 실패 시에도 JWT 기본 검증은 수행
            try {
                val claims = parseClaims(token)
                !claims.expiration.before(Date())
            } catch (e2: Exception) {
                false
            }
        }
    }

    /**
     * Access Token인지 확인
     * 
     * @param token JWT 토큰
     * @return Access Token이면 true
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            claims["tokenType"] == "ACCESS"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Refresh Token인지 확인
     * 
     * @param token JWT 토큰
     * @return Refresh Token이면 true
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            claims["tokenType"] == "REFRESH"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰의 만료 시간 반환
     * 
     * @param token JWT 토큰
     * @return 만료 시간
     */
    fun getExpiration(token: String): Date {
        return parseClaims(token).expiration
    }

    /**
     * JWT 토큰 파싱하여 Claims 반환
     * 
     * @param token JWT 토큰
     * @return JWT Claims
     */
    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }

    /**
     * 토큰에서 userType 추출
     */
    fun getUserType(token: String): String? {
        return parseClaims(token)["userType"] as? String
    }

    /**
     * 토큰에서 status 추출
     */
    fun getStatus(token: String): String? {
        return parseClaims(token)["status"] as? String
    }
    
    /**
     * 토큰의 발급 시간을 LocalDateTime으로 반환
     */
    fun getIssuedAt(token: String): LocalDateTime {
        val issuedAt = parseClaims(token).issuedAt
        return issuedAt.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    }
    
    /**
     * 토큰의 만료 시간을 LocalDateTime으로 반환
     */
    fun getExpirationAsLocalDateTime(token: String): LocalDateTime {
        val expiration = parseClaims(token).expiration
        return expiration.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    }
    
    /**
     * 토큰 해시 생성 (SHA-256 + Base64)
     */
    fun hashToken(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return Base64.getEncoder().encodeToString(md.digest(token.toByteArray()))
    }
} 