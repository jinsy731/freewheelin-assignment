package com.freewheelin.pulley.assignment.domain.model

import java.time.LocalDateTime
import kotlin.collections.count
import kotlin.collections.groupBy
import kotlin.collections.isNotEmpty
import kotlin.collections.mapValues

/**
 * 학습지 채점 완료 도메인 이벤트
 * 
 * 학생이 학습지의 문제들을 제출하고 전체 채점이 완료되었을 때 발생하는 이벤트입니다.
 * 이 이벤트를 통해 통계 테이블이 효율적으로 업데이트됩니다.
 */
data class SubmissionGradedEvent(
    val assignmentId: Long,
    val pieceId: Long,
    val studentId: Long,
    val submissionResults: List<SubmissionResult>,
    val occurredAt: LocalDateTime = LocalDateTime.now()
) {
    
    init {
        require(assignmentId > 0) { "출제 ID는 0보다 커야 합니다." }
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(studentId > 0) { "학생 ID는 0보다 커야 합니다." }
        require(submissionResults.isNotEmpty()) { "제출 결과는 비어있을 수 없습니다." }
    }
    
    /**
     * 학생의 전체 통계 계산
     */
    fun calculateStudentStats(): StudentSubmissionStats {
        return StudentSubmissionStats(
            totalCount = submissionResults.size,
            correctCount = submissionResults.count { it.isCorrect }
        )
    }
    
    /**
     * 문제별 통계 맵 생성
     */
    fun calculateProblemStatsMap(): Map<Long, ProblemSubmissionStats> {
        return submissionResults.groupBy { it.problemId }
            .mapValues { (_, results) ->
                ProblemSubmissionStats(
                    totalCount = results.size,
                    correctCount = results.count { it.isCorrect }
                )
            }
    }
}

/**
 * 개별 제출 결과
 */
data class SubmissionResult(
    val problemId: Long,
    val isCorrect: Boolean
) {
    init {
        require(problemId > 0) { "문제 ID는 0보다 커야 합니다." }
    }
}

/**
 * 학생 제출 통계
 */
data class StudentSubmissionStats(
    val totalCount: Int,
    val correctCount: Int
) {
    init {
        require(totalCount >= 0) { "전체 수는 0 이상이어야 합니다." }
        require(correctCount >= 0) { "정답 수는 0 이상이어야 합니다." }
        require(correctCount <= totalCount) { "정답 수는 전체 수를 초과할 수 없습니다." }
    }
}

/**
 * 문제 제출 통계
 */
data class ProblemSubmissionStats(
    val totalCount: Int,
    val correctCount: Int
) {
    init {
        require(totalCount >= 0) { "전체 수는 0 이상이어야 합니다." }
        require(correctCount >= 0) { "정답 수는 0 이상이어야 합니다." }
        require(correctCount <= totalCount) { "정답 수는 전체 수를 초과할 수 없습니다." }
    }
} 