package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.ProblemGradingResult
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeResult

/**
 * 답안 제출 및 채점 API 응답 DTO
 */
data class SubmissionGradeResponseDto(
    val pieceId: Long,
    val studentId: Long,
    val assignmentId: Long,
    val summary: GradingSummaryDto,
    val score: ScoreDto,
    val details: List<ProblemGradingResultDto>,
    val status: SubmissionStatusDto,
) {
    companion object {
        /**
         * Application 레이어 결과 객체를 응답 DTO로 변환
         */
        fun fromResult(result: SubmissionGradeResult): SubmissionGradeResponseDto {
            
            return SubmissionGradeResponseDto(
                pieceId = result.pieceId,
                studentId = result.studentId,
                assignmentId = result.assignmentId,
                summary = GradingSummaryDto(
                    totalProblems = result.totalProblems,
                    submittedProblems = result.submittedProblems,
                    correctAnswers = result.correctAnswers,
                    incorrectAnswers = result.incorrectAnswers
                ),
                score = ScoreDto(
                    correctnessRate = result.correctnessRate,
                    scorePercentage = result.scorePercentage
                ),
                details = result.gradingDetails.map { ProblemGradingResultDto.fromResult(it) },
                status = SubmissionStatusDto(
                    submittedAt = result.submittedAt
                ),
            )
        }
    }
}

/**
 * 채점 요약 정보 DTO
 */
data class GradingSummaryDto(
    val totalProblems: Int,
    val submittedProblems: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int
)

/**
 * 점수 정보 DTO
 */
data class ScoreDto(
    val correctnessRate: Double,     // 정답률 (0.0 ~ 1.0)
    val scorePercentage: Double      // 점수 백분율 (0.0 ~ 100.0)
)

/**
 * 개별 문제 채점 결과 DTO
 */
data class ProblemGradingResultDto(
    val problemId: Long,
    val submittedAnswer: String,
    val isCorrect: Boolean,
) {
    companion object {
        fun fromResult(result: ProblemGradingResult): ProblemGradingResultDto {
            return ProblemGradingResultDto(
                problemId = result.problemId,
                submittedAnswer = result.submittedAnswer,
                isCorrect = result.isCorrect
            )
        }
    }
}

/**
 * 제출 상태 정보 DTO
 */
data class SubmissionStatusDto(
    val submittedAt: String
) 