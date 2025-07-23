package com.freewheelin.pulley.statistics.infrastructure.persistence

import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import com.freewheelin.pulley.statistics.domain.port.PieceProblemStatRepository
import org.springframework.stereotype.Repository

/**
 * 문제별 학습지 통계 Repository 구현체
 */
@Repository
class PieceProblemStatPersistenceAdapter(
    private val jpaRepository: PieceProblemStatJpaRepository
) : PieceProblemStatRepository {
    
    override fun save(stat: PieceProblemStat): PieceProblemStat {
        val entity = PieceProblemStatJpaEntity.fromDomain(stat)
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun saveAll(stats: List<PieceProblemStat>): List<PieceProblemStat> {
        val entities = stats.map { PieceProblemStatJpaEntity.fromDomain(it) }
        val savedEntities = jpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByPieceId(pieceId: Long): List<PieceProblemStat> {
        return jpaRepository.findByPieceId(pieceId).map { it.toDomain() }
    }
    
    override fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): PieceProblemStat? {
        return jpaRepository.findByPieceIdAndProblemId(pieceId, problemId)?.toDomain()
    }
} 