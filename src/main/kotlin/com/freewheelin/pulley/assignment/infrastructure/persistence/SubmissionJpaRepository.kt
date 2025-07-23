package com.freewheelin.pulley.assignment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Submission JPA Repository 인터페이스
 */
interface SubmissionJpaRepository : JpaRepository<SubmissionJpaEntity, Long> {
    
    /**
     * 과제 ID로 모든 제출 조회
     */
    fun findByAssignmentId(assignmentId: Long): List<SubmissionJpaEntity>
    
    /**
     * 여러 과제 ID로 제출 조회
     */
    fun findByAssignmentIdIn(assignmentIds: List<Long>): List<SubmissionJpaEntity>
    
    /**
     * 문제 ID로 제출 조회
     */
    fun findByProblemId(problemId: Long): List<SubmissionJpaEntity>
    
    /**
     * 과제 ID와 문제 ID로 제출 조회
     */
    fun findByAssignmentIdAndProblemId(assignmentId: Long, problemId: Long): SubmissionJpaEntity?
    
    /**
     * 과제 ID와 여러 문제 ID로 제출 조회
     */
    fun findByAssignmentIdAndProblemIdIn(assignmentId: Long, problemIds: List<Long>): List<SubmissionJpaEntity>
    
    /**
     * 학습지와 문제 ID로 모든 제출 조회 (통계용)
     * Assignment를 통해 piece_id와 연결
     */
    @Query("""
        SELECT s FROM SubmissionJpaEntity s 
        JOIN AssignmentJpaEntity a ON s.assignmentId = a.id
        WHERE a.pieceId = :pieceId AND s.problemId = :problemId
    """)
    fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): List<SubmissionJpaEntity>
    
    /**
     * 과제 ID로 제출 개수 조회
     */
    fun countByAssignmentId(assignmentId: Long): Long
    
    /**
     * 과제 ID로 정답 개수 조회
     */
    @Query("SELECT COUNT(s) FROM SubmissionJpaEntity s WHERE s.assignmentId = :assignmentId AND s.isCorrect = true")
    fun countCorrectByAssignmentId(assignmentId: Long): Long
    
    /**
     * 과제 ID와 문제 ID로 제출 존재 여부 확인
     */
    fun existsByAssignmentIdAndProblemId(assignmentId: Long, problemId: Long): Boolean
    
    /**
     * 과제 ID로 제출 삭제
     */
    fun deleteByAssignmentId(assignmentId: Long)
} 