package com.freewheelin.pulley.assignment.domain.port

import com.freewheelin.pulley.assignment.domain.model.Assignment

/**
 * 출제 Repository 포트 인터페이스
 */
interface AssignmentRepository {
    
    /**
     * 출제 정보 저장
     */
    fun save(assignment: Assignment): Assignment
    
    /**
     * 여러 출제 정보 저장
     */
    fun saveAll(assignments: List<Assignment>): List<Assignment>
    
    /**
     * 학습지 ID와 학생 ID로 출제 정보 조회 (nullable)
     */
    fun findByPieceIdAndStudentId(pieceId: Long, studentId: Long): Assignment?
    
    /**
     * 학습지 ID와 학생 ID로 출제 정보 조회 (non-null, 없으면 예외)
     */
    fun getByPieceIdAndStudentId(pieceId: Long, studentId: Long): Assignment
    
    /**
     * 학습지 ID와 학생 ID 리스트로 출제 정보 조회
     */
    fun findByPieceIdAndStudentIdIn(pieceId: Long, studentIds: List<Long>): List<Assignment>
} 