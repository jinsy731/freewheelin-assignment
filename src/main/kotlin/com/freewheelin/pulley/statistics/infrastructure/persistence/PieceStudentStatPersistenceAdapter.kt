package com.freewheelin.pulley.statistics.infrastructure.persistence

import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import com.freewheelin.pulley.statistics.domain.port.PieceStudentStatRepository
import org.springframework.stereotype.Repository

/**
 * 학생별 학습지 통계 Repository 구현체
 */
@Repository
class PieceStudentStatPersistenceAdapter(
    private val jpaRepository: PieceStudentStatJpaRepository
) : PieceStudentStatRepository {
    
    override fun save(stat: PieceStudentStat): PieceStudentStat {
        val entity = PieceStudentStatJpaEntity.fromDomain(stat)
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun saveAll(stats: List<PieceStudentStat>): List<PieceStudentStat> {
        val entities = stats.map { PieceStudentStatJpaEntity.fromDomain(it) }
        val savedEntities = jpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByAssignmentId(assignmentId: Long): PieceStudentStat? {
        return jpaRepository.findByAssignmentId(assignmentId)?.toDomain()
    }
    
    override fun findByPieceId(pieceId: Long): List<PieceStudentStat> {
        return jpaRepository.findByPieceId(pieceId).map { it.toDomain() }
    }
} 