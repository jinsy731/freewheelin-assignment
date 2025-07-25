package com.freewheelin.pulley.assignment.domain.model

import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.ProblemCount
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.assignment.domain.model.Submission
import java.time.LocalDateTime
import kotlin.collections.isNotEmpty

/**
 * 출제 도메인 모델
 * 
 * 학습지 출제에 대한 도메인 로직과 비즈니스 규칙을 포함합니다.
 */
data class Assignment(
    val id: AssignmentId,
    val pieceId: PieceId,
    val studentId: StudentId,
    val assignedAt: LocalDateTime,
    val submittedAt: LocalDateTime? = null,
    val correctnessRate: CorrectnessRate? = null
) {
    
    /**
     * 제출 완료 처리
     * 
     * @param submissions 제출된 답안들
     * @param totalProblems 전체 문제 수
     * @return 업데이트된 Assignment
     */
    fun submit(submissions: List<Submission>, totalProblems: ProblemCount): Assignment {
        require(submissions.isNotEmpty()) { "제출할 답안이 없습니다." }
        
        val newCorrectness = CorrectnessRate.calculate(submissions)
        val newSubmittedAt = LocalDateTime.now()
        
        return copy(
            submittedAt = newSubmittedAt,
            correctnessRate = newCorrectness
        )
    }
    
    /**
     * 제출 여부 확인
     */
    fun isSubmitted(): Boolean = submittedAt != null
    
    companion object {
        /**
         * 새로운 출제 생성
         */
        fun create(
            pieceId: PieceId,
            studentId: StudentId,
            assignedAt: LocalDateTime = LocalDateTime.now()
        ): Assignment {
            return Assignment(
                id = AssignmentId(0),
                pieceId = pieceId,
                studentId = studentId,
                assignedAt = assignedAt,
                submittedAt = null,
                correctnessRate = null
            )
        }
    }
} 