package com.freewheelin.pulley.statistics.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 학생별 학습지 통계 JPA Repository
 */
interface PieceStudentStatJpaRepository : JpaRepository<PieceStudentStatJpaEntity, Long> {
    
    fun findByAssignmentId(assignmentId: Long): PieceStudentStatJpaEntity?
    
    fun findByPieceId(pieceId: Long): List<PieceStudentStatJpaEntity>
} 