package com.freewheelin.pulley.assignment.infrastructure.persistence

import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Assignment JPA Entity
 * 
 * 학습지 출제 정보를 데이터베이스에 저장하기 위한 JPA 엔티티입니다.
 */
@Entity
@Table(
    name = "assignments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_assignments_piece_student", columnNames = ["piece_id", "student_id"])
    ]
)
data class AssignmentJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    @Column(name = "piece_id", nullable = false)
    val pieceId: Long,
    
    @Column(name = "student_id", nullable = false)
    val studentId: Long,
    
    @Column(name = "assigned_at", nullable = false)
    val assignedAt: LocalDateTime,
    
    @Column(name = "submitted_at")
    val submittedAt: LocalDateTime? = null,
    
    @Column(name = "correctness_rate")
    val correctnessRate: Double? = null
) {
    
    /**
     * JPA Entity를 Domain Model로 변환
     */
    fun toDomain(): Assignment {
        return Assignment(
            id = AssignmentId(id),
            pieceId = PieceId(pieceId),
            studentId = StudentId(studentId),
            assignedAt = assignedAt,
            submittedAt = submittedAt,
            correctnessRate = correctnessRate?.let { CorrectnessRate(it) }
        )
    }
    
    companion object {
        /**
         * Domain Model을 JPA Entity로 변환
         */
        fun fromDomain(assignment: Assignment): AssignmentJpaEntity {
            return AssignmentJpaEntity(
                id = assignment.id.value,
                pieceId = assignment.pieceId.value,
                studentId = assignment.studentId.value,
                assignedAt = assignment.assignedAt,
                submittedAt = assignment.submittedAt,
                correctnessRate = assignment.correctnessRate?.value
            )
        }
    }
} 