package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AnswerSubmission
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.NotBlank

/**
 * 답안 제출 및 채점 API 요청 DTO
 */
@Schema(description = "답안 제출 및 채점 요청")
data class SubmissionGradeRequestDto(
    @field:NotEmpty(message = "답안 리스트는 비어있을 수 없습니다")
    @field:Valid
    @Schema(description = "제출할 답안 목록", required = true)
    val answers: List<AnswerSubmissionDto>
) {
    /**
     * DTO를 Application 레이어 요청 객체로 변환
     */
    fun toApplicationRequest(pieceId: Long, studentId: Long): SubmissionGradeRequest {
        return SubmissionGradeRequest(
            pieceId = pieceId,
            studentId = studentId,
            answers = answers.map { it.toApplicationObject() }
        )
    }
}

/**
 * 개별 문제 답안 제출 DTO
 */
@Schema(description = "개별 문제 답안")
data class AnswerSubmissionDto(
    
    @field:NotNull(message = "문제 ID는 필수입니다")
    @field:Positive(message = "문제 ID는 양수여야 합니다")
    @Schema(description = "문제 ID", example = "1001", required = true)
    val problemId: Long,
    
    @field:NotBlank(message = "답안은 비어있을 수 없습니다")
    @Schema(description = "제출 답안", example = "1", required = true)
    val answer: String
) {
    /**
     * DTO를 Application 레이어 객체로 변환
     */
    fun toApplicationObject(): AnswerSubmission {
        return AnswerSubmission(
            problemId = problemId,
            answer = answer.trim()  // 공백 제거
        )
    }
} 