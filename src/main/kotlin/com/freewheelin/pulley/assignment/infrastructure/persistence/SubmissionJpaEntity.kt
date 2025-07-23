package com.freewheelin.pulley.assignment.infrastructure.persistence

import com.freewheelin.pulley.assignment.domain.model.Submission
import jakarta.persistence.*

/**
 * 제출 JPA 엔티티
 */
@Entity
@Table(
    name = "submissions",
    indexes = [
        Index(name = "idx_assignment_id", columnList = "assignment_id"),
        Index(name = "idx_problem_id", columnList = "problem_id"),
        Index(name = "idx_assignment_problem", columnList = "assignment_id, problem_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_assignment_problem", columnNames = ["assignment_id", "problem_id"])
    ]
)
class SubmissionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "assignment_id", nullable = false)
    val assignmentId: Long,
    
    @Column(name = "problem_id", nullable = false)
    val problemId: Long,
    
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    val answer: String,
    
    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): Submission {
        return Submission(
            id = id,
            assignmentId = assignmentId,
            problemId = problemId,
            answer = answer,
            isCorrect = isCorrect
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(submission: Submission): SubmissionJpaEntity {
            return SubmissionJpaEntity(
                id = submission.id,
                assignmentId = submission.assignmentId,
                problemId = submission.problemId,
                answer = submission.answer,
                isCorrect = submission.isCorrect
            )
        }
    }
} 