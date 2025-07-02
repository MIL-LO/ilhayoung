package com.millo.ilhayoung.common.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

/**
 * 페이징 처리된 응답을 위한 클래스
 * API 스펙의 pagination 구조에 맞춰 설계
 *
 * @param T 페이징 처리될 데이터의 타입
 */
@Schema(description = "페이징 응답 형식")
data class PageResponse<T>(
    
    /**
     * 현재 페이지의 데이터 목록
     */
    @Schema(description = "데이터 목록")
    val items: List<T>,
    
    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    @Schema(description = "현재 페이지 번호", example = "0")
    val page: Int,
    
    /**
     * 페이지 크기 (한 페이지당 데이터 개수)
     */
    @Schema(description = "페이지 크기", example = "10")
    val size: Int,
    
    /**
     * 전체 데이터 개수
     */
    @Schema(description = "전체 데이터 개수", example = "120")
    val totalElements: Long,
    
    /**
     * 전체 페이지 수
     */
    @Schema(description = "전체 페이지 수", example = "12")
    val totalPages: Int
    
) {
    
    /**
     * Spring Data의 Page 객체로부터 PageResponse를 생성하는 팩토리 메서드
     * 
     * @param page Spring Data Page 객체
     * @return PageResponse 객체
     */
    companion object {
        fun <T> of(page: Page<T>): PageResponse<T> {
            return PageResponse(
                items = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages
            )
        }
        
        /**
         * 빈 페이지를 생성하는 팩토리 메서드
         * 
         * @param page 페이지 번호
         * @param size 페이지 크기
         * @return 빈 PageResponse 객체
         */
        fun <T> empty(page: Int = 0, size: Int = 10): PageResponse<T> {
            return PageResponse(
                items = emptyList(),
                page = page,
                size = size,
                totalElements = 0,
                totalPages = 0
            )
        }
    }
} 