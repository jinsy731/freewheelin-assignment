package com.freewheelin.pulley.statistics.infrastructure.persistence

import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import jakarta.persistence.*

/**
 * 문제별 학습지 통계 JPA 엔티티
 */
@Entity
@Table(
    name = "piece_problem_stats",
    indexes = [
        Index(name = "idx_piece_problem_stats_piece_id", columnList = "piece_id"),
        Index(name = "idx_piece_problem_stats_problem_id", columnList = "problem_id"),
        Index(name = "idx_piece_problem_stats_piece_problem", columnList = "piece_id, problem_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_piece_problem", columnNames = ["piece_id", "problem_id"])
    ]
)
class PieceProblemStatJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "piece_id", nullable = false)
    val pieceId: Long,
    
    @Column(name = "problem_id", nullable = false)
    val problemId: Long,
    
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
    fun toDomain(): PieceProblemStat {
        return PieceProblemStat(
            id = id,
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            totalCount = totalCount,
            correctCount = correctCount,
            correctnessRate = CorrectnessRate(correctnessRate)
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(stat: PieceProblemStat): PieceProblemStatJpaEntity {
            return PieceProblemStatJpaEntity(
                id = stat.id,
                pieceId = stat.pieceId.value,
                problemId = stat.problemId.value,
                totalCount = stat.totalCount,
                correctCount = stat.correctCount,
                correctnessRate = stat.correctnessRate.value
            )
        }
    }
} 