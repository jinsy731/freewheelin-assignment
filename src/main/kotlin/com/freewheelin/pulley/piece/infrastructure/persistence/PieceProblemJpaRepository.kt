package com.freewheelin.pulley.piece.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * PieceProblem JPA Repository 인터페이스
 */
interface PieceProblemJpaRepository : JpaRepository<PieceProblemJpaEntity, Long> {
    
    /**
     * 학습지 ID로 문제 매핑 조회 (position 순서대로)
     */
    @Query("SELECT pp FROM PieceProblemJpaEntity pp WHERE pp.pieceId = :pieceId ORDER BY pp.position")
    fun findByPieceIdOrderByPosition(pieceId: Long): List<PieceProblemJpaEntity>

    /**
     * 학습지 ID와 문제 ID 리스트로 기존 매핑 조회
     */
    fun findByPieceIdAndProblemIdIn(pieceId: Long, problemIds: List<Long>): List<PieceProblemJpaEntity>
} 