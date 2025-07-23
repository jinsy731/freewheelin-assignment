package com.freewheelin.pulley.piece.infrastructure.persistence

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import jakarta.persistence.*

/**
 * 학습지-문제 매핑 JPA 엔티티
 */
@Entity
@Table(
    name = "piece_problems",
    indexes = [
        Index(name = "idx_piece_id", columnList = "piece_id"),
        Index(name = "idx_piece_position", columnList = "piece_id, position"),
        Index(name = "uk_piece_problem", columnList = "piece_id, problem_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_piece_problem", columnNames = ["piece_id", "problem_id"])
    ]
)
class PieceProblemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "piece_id", nullable = false)
    val pieceId: Long,
    
    @Column(name = "problem_id", nullable = false)
    val problemId: Long,
    
    @Column(name = "position", nullable = false)
    val position: Double
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): PieceProblem {
        return PieceProblem(
            id = id,
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(position)
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(pieceProblem: PieceProblem): PieceProblemJpaEntity {
            return PieceProblemJpaEntity(
                id = pieceProblem.id,
                pieceId = pieceProblem.pieceId.value,
                problemId = pieceProblem.problemId.value,
                position = pieceProblem.position.value
            )
        }
    }
} 