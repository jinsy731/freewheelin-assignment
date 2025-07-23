package com.freewheelin.pulley.assignment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Assignment JPA Repository 인터페이스
 */
interface AssignmentJpaRepository : JpaRepository<AssignmentJpaEntity, Long> {
    
    /**
     * 학습지 ID와 학생 ID로 출제 조회
     */
    fun findByPieceIdAndStudentId(pieceId: Long, studentId: Long): AssignmentJpaEntity?
    
    /**
     * 학습지 ID로 모든 출제 조회
     */
    fun findByPieceId(pieceId: Long): List<AssignmentJpaEntity>
    
    /**
     * 학생 ID로 출제받은 학습지들 조회
     */
    fun findByStudentId(studentId: Long): List<AssignmentJpaEntity>
    
    /**
     * 학습지 ID와 학생 ID 리스트로 기존 출제들 조회
     */
    fun findByPieceIdAndStudentIdIn(pieceId: Long, studentIds: List<Long>): List<AssignmentJpaEntity>
    
    /**
     * 출제 존재 여부 확인
     */
    fun existsByPieceIdAndStudentId(pieceId: Long, studentId: Long): Boolean
} 