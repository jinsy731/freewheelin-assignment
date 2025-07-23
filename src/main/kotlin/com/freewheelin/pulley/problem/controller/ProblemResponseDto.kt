package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.problem.application.port.ProblemSearchResult
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType

/**
 * 문제 응답 DTO
 */
data class ProblemResponseDto(
    val id: Long,
    val answer: String?,  // 학생에게는 null로 설정
    val unitCode: String,
    val problemLevel: Int,
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
data class ProblemSearchResponseDto(
    val problemList: List<ProblemResponseDto>,
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