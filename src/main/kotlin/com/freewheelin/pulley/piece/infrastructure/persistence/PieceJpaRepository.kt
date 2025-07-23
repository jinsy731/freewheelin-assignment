package com.freewheelin.pulley.piece.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Piece JPA Repository 인터페이스
 */
interface PieceJpaRepository : JpaRepository<PieceJpaEntity, Long> {
    
    /**
     * 선생님 ID로 학습지 목록 조회
     */
    fun findByTeacherId(teacherId: Long): List<PieceJpaEntity>
} 