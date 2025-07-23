package com.freewheelin.pulley.piece.infrastructure.persistence

import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import org.springframework.stereotype.Repository

/**
 * PieceProblem Repository Port 구현체 (Adapter)
 */
@Repository
class PieceProblemPersistenceAdapter(
    private val pieceProblemJpaRepository: PieceProblemJpaRepository
) : PieceProblemRepository {
    
    override fun save(pieceProblem: PieceProblem): PieceProblem {
        val entity = PieceProblemJpaEntity.fromDomain(pieceProblem)
        val savedEntity = pieceProblemJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun saveAll(pieceProblems: List<PieceProblem>): List<PieceProblem> {
        val entities = pieceProblems.map { PieceProblemJpaEntity.fromDomain(it) }
        val savedEntities = pieceProblemJpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByPieceIdOrderByPosition(pieceId: Long): List<PieceProblem> {
        return pieceProblemJpaRepository.findByPieceIdOrderByPosition(pieceId)
            .map { it.toDomain() }
    }
    
    override fun findByPieceIdAndProblemIdIn(pieceId: Long, problemIds: List<Long>): List<PieceProblem> {
        return pieceProblemJpaRepository.findByPieceIdAndProblemIdIn(pieceId, problemIds)
            .map { it.toDomain() }
    }

} 