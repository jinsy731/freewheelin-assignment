package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.problem.application.port.ProblemSearchResult
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 문제 응답 DTO
 */
@Schema(description = "문제 정보")
data class ProblemResponseDto(
    @Schema(description = "문제 ID", example = "1001")
    val id: Long,
    @Schema(description = "정답 (선생님에게만 제공)", example = "1", nullable = true)
    val answer: String?,  // 학생에게는 null로 설정
    @Schema(description = "유형 코드", example = "uc1580")
    val unitCode: String,
    @Schema(description = "난이도 (1-5)", example = "2")
    val problemLevel: Int,
    @Schema(description = "문제 유형", example = "SELECTION")
    val problemType: ProblemType
) {
    companion object {
        /**
         * 도메인 모델을 응답 DTO로 변환
         */
        fun fromDomain(problem: Problem, includeAnswer: Boolean = true): ProblemResponseDto {
            return ProblemResponseDto(
                id = problem.id,
                answer = if (includeAnswer) problem.answer else null,
                unitCode = problem.unitCode,
                problemLevel = problem.level,
                problemType = problem.problemType
            )
        }
    }
}

/**
 * 문제 조회 응답 DTO
 */
@Schema(description = "문제 검색 응답")
data class ProblemSearchResponseDto(
    @Schema(description = "검색된 문제 목록")
    val problemList: List<ProblemResponseDto>,
    @Schema(description = "총 문제 수", example = "10")
    val totalCount: Int
) {
    companion object {
        /**
         * Application 레이어 결과를 응답 DTO로 변환
         */
        fun from(result: ProblemSearchResult, isTeacher: Boolean = true): ProblemSearchResponseDto {
            return ProblemSearchResponseDto(
                problemList = result.problems.map { problemInfo ->
                    ProblemResponseDto(
                        id = problemInfo.id,
                        answer = if (isTeacher) problemInfo.answer else null,
                        unitCode = problemInfo.unitCode,
                        problemLevel = problemInfo.level,
                        problemType = problemInfo.problemType
                    )
                },
                totalCount = result.totalCount
            )
        }
    }
} 