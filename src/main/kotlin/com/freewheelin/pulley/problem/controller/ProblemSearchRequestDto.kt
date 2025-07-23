package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * 문제 조회 API 요청 DTO
 */
data class ProblemSearchRequestDto(
    
    @field:NotNull(message = "총 문제 수는 필수입니다")
    @field:Min(value = 1, message = "총 문제 수는 1 이상이어야 합니다")
    val totalCount: Int,
    
    @field:NotEmpty(message = "유형코드 리스트는 비어있을 수 없습니다")
    val unitCodeList: List<String>,
    
    @field:NotNull(message = "난이도는 필수입니다")
    val problemLevel: Level,
    
    @field:NotNull(message = "문제 유형은 필수입니다")
    val problemType: ProblemTypeFilter
) {
    /**
     * DTO를 Application 레이어 쿼리 객체로 변환
     */
    fun toApplicationQuery(): ProblemSearchQuery {
        return ProblemSearchQuery(
            totalCount = totalCount,
            unitCodeList = unitCodeList,
            level = problemLevel,
            problemType = problemType
        )
    }
} 