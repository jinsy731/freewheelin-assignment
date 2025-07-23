package com.freewheelin.pulley.piece.infrastructure.persistence

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.piece.domain.model.Piece
import jakarta.persistence.*

/**
 * 학습지 JPA 엔티티
 */
@Entity
@Table(
    name = "pieces",
    indexes = [
        Index(name = "idx_teacher_id", columnList = "teacher_id")
    ]
)
class PieceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "teacher_id", nullable = false)
    val teacherId: Long,
    
    @Column(name = "name", nullable = false, length = 100)
    val name: String
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): Piece {
        return Piece(
            id = PieceId(id),
            teacherId = TeacherId(teacherId),
            name = PieceName(name)
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(piece: Piece): PieceJpaEntity {
            return PieceJpaEntity(
                id = piece.id.value,
                teacherId = piece.teacherId.value,
                name = piece.name.value
            )
        }
    }
} 