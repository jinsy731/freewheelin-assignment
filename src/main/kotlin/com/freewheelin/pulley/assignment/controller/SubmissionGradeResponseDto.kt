package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.ProblemGradingResult
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeResult
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 답안 제출 및 채점 API 응답 DTO
 */
@Schema(description = "답안 제출 및 채점 응답")
data class SubmissionGradeResponseDto(
    @Schema(description = "학습지 ID", example = "1")
    val pieceId: Long,
    @Schema(description = "학생 ID", example = "4")
    val studentId: Long,
    @Schema(description = "출제 ID", example = "1")
    val assignmentId: Long,
    @Schema(description = "채점 요약 정보")
    val summary: GradingSummaryDto,
    @Schema(description = "점수 정보")
    val score: ScoreDto,
    @Schema(description = "문제별 채점 결과")
    val details: List<ProblemGradingResultDto>,
    @Schema(description = "제출 상태 정보")
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
@Schema(description = "채점 요약 정보")
data class GradingSummaryDto(
    @Schema(description = "총 문제 수", example = "10")
    val totalProblems: Int,
    @Schema(description = "제출된 문제 수", example = "8")
    val submittedProblems: Int,
    @Schema(description = "정답 수", example = "6")
    val correctAnswers: Int,
    @Schema(description = "오답 수", example = "2")
    val incorrectAnswers: Int
)

/**
 * 점수 정보 DTO
 */
@Schema(description = "점수 정보")
data class ScoreDto(
    @Schema(description = "정답률 (0.0 ~ 1.0)", example = "0.75")
    val correctnessRate: Double,     // 정답률 (0.0 ~ 1.0)
    @Schema(description = "점수 백분율 (0.0 ~ 100.0)", example = "75.0")
    val scorePercentage: Double      // 점수 백분율 (0.0 ~ 100.0)
)

/**
 * 개별 문제 채점 결과 DTO
 */
@Schema(description = "개별 문제 채점 결과")
data class ProblemGradingResultDto(
    @Schema(description = "문제 ID", example = "1001")
    val problemId: Long,
    @Schema(description = "제출된 답안", example = "1")
    val submittedAnswer: String,
    @Schema(description = "정답 여부", example = "true")
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
@Schema(description = "제출 상태 정보")
data class SubmissionStatusDto(
    @Schema(description = "제출 시간", example = "2025-07-25T18:30:00")
    val submittedAt: String
) 