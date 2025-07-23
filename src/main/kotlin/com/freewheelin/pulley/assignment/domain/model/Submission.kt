package com.freewheelin.pulley.assignment.domain.model

/**
 * 제출 도메인 모델
 *
 * 학생이 특정 문제에 제출한 답안과 채점 결과를 나타냅니다.
 */
data class Submission(
    val id: Long,
    val assignmentId: Long,
    val problemId: Long,
    val answer: String,
    val isCorrect: Boolean
) {
    init {
        require(answer.isNotBlank()) { "제출한 답안은 비어있을 수 없습니다." }
    }
}