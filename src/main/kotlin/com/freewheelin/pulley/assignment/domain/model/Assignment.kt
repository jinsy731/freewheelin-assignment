package com.freewheelin.pulley.assignment.domain.model

import com.freewheelin.pulley.common.domain.*
import java.time.LocalDateTime

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
) : Ownable<Long> {
    
    /**
     * 제출 완료 처리
     * 
     * @param submissions 제출된 답안들
     * @return 업데이트된 Assignment
     */
    fun submit(submissions: List<Submission>): Assignment {
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
    
    /**
     * 소유자 확인 - Ownable 인터페이스 구현
     */
    override fun isOwnedBy(ownerId: Long): Boolean = studentId.value == ownerId
    
    /**
     * 리소스 식별자 반환 - Ownable 인터페이스 구현
     */
    override fun getResourceId(): Any = id.value
    
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