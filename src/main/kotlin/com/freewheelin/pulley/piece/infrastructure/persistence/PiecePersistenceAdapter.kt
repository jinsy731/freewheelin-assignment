package com.freewheelin.pulley.piece.infrastructure.persistence

import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import org.springframework.stereotype.Component

/**
 * 학습지 영속성 어댑터
 */
@Component
class PiecePersistenceAdapter(
    private val pieceJpaRepository: PieceJpaRepository
) : PieceRepository {
    
    override fun save(piece: Piece): Piece {
        val entity = PieceJpaEntity.fromDomain(piece)
        val savedEntity = pieceJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: Long): Piece? {
        return pieceJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun getById(id: Long): Piece {
        return findById(id) ?: throw NotFoundException(
            ErrorCode.PIECE_NOT_FOUND,
            id
        )
    }
} 