package com.freewheelin.pulley.statistics.infrastructure.persistence

import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import jakarta.persistence.*

/**
 * 학생별 학습지 통계 JPA 엔티티
 */
@Entity
@Table(
    name = "piece_student_stats",
    indexes = [
        Index(name = "idx_piece_student_stats_piece_id", columnList = "piece_id"),
        Index(name = "idx_piece_student_stats_student_id", columnList = "student_id"),
        Index(name = "idx_piece_student_stats_piece_student", columnList = "piece_id, student_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_assignment_id", columnNames = ["assignment_id"]),
        UniqueConstraint(name = "uk_piece_student", columnNames = ["piece_id", "student_id"])
    ]
)
class PieceStudentStatJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "assignment_id", nullable = false, unique = true)
    val assignmentId: Long,
    
    @Column(name = "piece_id", nullable = false)
    val pieceId: Long,
    
    @Column(name = "student_id", nullable = false)
    val studentId: Long,
    
    @Column(name = "total_count", nullable = false)
    val totalCount: Int,
    
    @Column(name = "correct_count", nullable = false)
    val correctCount: Int,
    
    @Column(name = "correctness_rate", nullable = false)
    val correctnessRate: Double
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): PieceStudentStat {
        return PieceStudentStat(
            id = id,
            assignmentId = AssignmentId(assignmentId),
            pieceId = PieceId(pieceId),
            studentId = StudentId(studentId),
            totalCount = totalCount,
            correctCount = correctCount,
            correctnessRate = CorrectnessRate(correctnessRate)
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(stat: PieceStudentStat): PieceStudentStatJpaEntity {
            return PieceStudentStatJpaEntity(
                id = stat.id,
                assignmentId = stat.assignmentId.value,
                pieceId = stat.pieceId.value,
                studentId = stat.studentId.value,
                totalCount = stat.totalCount,
                correctCount = stat.correctCount,
                correctnessRate = stat.correctnessRate.value
            )
        }
    }
} 