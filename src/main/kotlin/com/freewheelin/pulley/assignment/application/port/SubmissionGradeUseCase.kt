package com.freewheelin.pulley.assignment.application.port

import kotlin.collections.distinctBy
import kotlin.collections.isNotEmpty
import kotlin.text.isNotBlank

/**
 * 답안 제출 및 채점 Use Case 포트 인터페이스
 * 
 * Presentation 레이어에서 호출하는 답안 제출 및 자동 채점 기능을 정의합니다.
 */
interface SubmissionGradeUseCase {
    
    /**
     * 답안 제출 및 자동 채점
     * 
     * @param request 답안 제출 요청 정보
     * @return 채점 결과 정보
     */
    fun submitAndGrade(request: SubmissionGradeRequest): SubmissionGradeResult
}

/**
 * 답안 제출 및 채점 요청 DTO
 */
data class SubmissionGradeRequest(
    val pieceId: Long,
    val studentId: Long,
    val answers: List<AnswerSubmission>
) {
    init {
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(studentId > 0) { "학생 ID는 0보다 커야 합니다." }
        require(answers.isNotEmpty()) { "답안 리스트는 비어있을 수 없습니다." }
        require(answers.distinctBy { it.problemId }.size == answers.size) { "중복된 문제 ID가 존재합니다." }
    }
}

/**
 * 개별 문제 답안 제출 DTO
 */
data class AnswerSubmission(
    val problemId: Long,
    val answer: String
) {
    init {
        require(problemId > 0) { "문제 ID는 0보다 커야 합니다." }
        require(answer.isNotBlank()) { "답안은 비어있을 수 없습니다." }
    }
}

/**
 * 답안 제출 및 채점 결과 DTO (간소화된 버전)
 */
data class SubmissionGradeResult(
    val pieceId: Long,                // 학습지 ID
    val studentId: Long,              // 학생 ID
    val assignmentId: Long,           // 과제 ID
    val totalProblems: Int,           // 전체 문제 수
    val submittedProblems: Int,       // 제출된 문제 수  
    val correctAnswers: Int,          // 정답 수
    val correctnessRate: Double,   // 전체 정답률 (0.0 ~ 1.0)
    val gradingDetails: List<ProblemGradingResult>,  // 문제별 채점 결과
    val submittedAt: String           // 제출 시간
) {
    val scorePercentage: Double       // 점수 백분율 (0.0 ~ 100.0)
        get() = correctnessRate * 100.0
    
    val incorrectAnswers: Int         // 오답 수
        get() = submittedProblems - correctAnswers
}

/**
 * 개별 문제 채점 결과 DTO (간소화된 버전)
 */
data class ProblemGradingResult(
    val problemId: Long,
    val submittedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
)