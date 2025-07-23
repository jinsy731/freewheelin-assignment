package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.piece.application.port.PieceProblemsResult
import com.freewheelin.pulley.piece.application.port.ProblemDetail

/**
 * 학습지 문제 조회 응답 DTO
 */
data class PieceProblemsResponseDto(
    val problems: List<ProblemDto>
) {
    companion object {
        /**
         * UseCase 결과를 응답 DTO로 변환
         */
        fun from(result: PieceProblemsResult): PieceProblemsResponseDto {
            val problemDtos = result.problems.map { problemDetail ->
                ProblemDto.from(problemDetail)
            }
            
            return PieceProblemsResponseDto(
                problems = problemDtos
            )
        }
    }
}

/**
 * 문제 정보 DTO
 */
data class ProblemDto(
    val id: Long,
    val unitCode: String,
    val problemLevel: Int,
    val type: ProblemType
) {
    companion object {
        /**
         * ProblemDetail을 DTO로 변환
         */
        fun from(problemDetail: ProblemDetail): ProblemDto {
            return ProblemDto(
                id = problemDetail.id,
                unitCode = problemDetail.unitCode,
                problemLevel = problemDetail.level,
                type = problemDetail.type
            )
        }
    }
}

/**
 * 학습지 문제 상세 조회 응답 DTO
 */
data class PieceProblemsDetailResponseDto(
    val pieceId: Long,
    val problems: List<PieceProblemDto>,
    val totalCount: Int
) {
    companion object {
        /**
         * Problem 리스트를 응답 DTO로 변환
         * 
         * @param pieceId 학습지 ID
         * @param problems 문제 리스트 (이미 순서대로 정렬됨)
         */
        fun fromProblems(pieceId: Long, problems: List<Problem>): PieceProblemsDetailResponseDto {
            val problemDtos = problems.mapIndexed { index, problem ->
                PieceProblemDto.fromDomain(problem, index + 1)
            }
            
            return PieceProblemsDetailResponseDto(
                pieceId = pieceId,
                problems = problemDtos,
                totalCount = problems.size
            )
        }
        
        /**
         * Problem 리스트를 응답 DTO로 변환 (별칭)
         */
        fun from(pieceId: Long, problems: List<Problem>): PieceProblemsDetailResponseDto {
            return fromProblems(pieceId, problems)
        }
    }
}

/**
 * 학습지 내 개별 문제 DTO
 * 
 * 학생에게 제공되는 문제 정보 (정답은 숨김)
 */
data class PieceProblemDto(
    val problemId: Long,
    val questionNumber: Int,  // 문제 번호 (1부터 시작)
    val unitCode: String,
    val level: Int,
    val problemType: ProblemType
) {
    companion object {
        /**
         * Problem 도메인 객체를 DTO로 변환
         * 
         * @param problem 문제 도메인 객체
         * @param questionNumber 문제 번호 (1부터 시작)
         */
        fun fromDomain(problem: Problem, questionNumber: Int): PieceProblemDto {
            return PieceProblemDto(
                problemId = problem.id,
                questionNumber = questionNumber,
                unitCode = problem.unitCode,
                level = problem.level,
                problemType = problem.problemType
            )
        }
    }
} 