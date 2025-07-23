package com.freewheelin.pulley.statistics.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 문제별 학습지 통계 JPA Repository
 */
interface PieceProblemStatJpaRepository : JpaRepository<PieceProblemStatJpaEntity, Long> {
    
    fun findByPieceId(pieceId: Long): List<PieceProblemStatJpaEntity>
    
    fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): PieceProblemStatJpaEntity?
}