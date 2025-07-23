package com.freewheelin.pulley.assignment.domain.port

import com.freewheelin.pulley.assignment.domain.model.Submission


/**
 * Submission Repository Port 인터페이스
 */
interface SubmissionRepository {
    
    /**
     * 제출 저장
     */
    fun save(submission: Submission): Submission
    
    /**
     * 여러 제출 저장
     */
    fun saveAll(submissions: List<Submission>): List<Submission>
    
    /**
     * 출제 ID로 제출 조회
     */
    fun findByAssignmentId(assignmentId: Long): List<Submission>
    
    /**
     * 여러 출제 ID로 제출 조회
     */
    fun findByAssignmentIds(assignmentIds: List<Long>): List<Submission>
    
    /**
     * 문제 ID로 제출 조회
     */
    fun findByProblemId(problemId: Long): List<Submission>
    
    /**
     * 출제와 문제 ID로 제출 조회
     */
    fun findByAssignmentIdAndProblemId(assignmentId: Long, problemId: Long): Submission?
    
    /**
     * 출제와 여러 문제 ID로 제출 조회
     */
    fun findByAssignmentIdAndProblemIdIn(assignmentId: Long, problemIds: List<Long>): List<Submission>
    
    /**
     * 학습지와 문제 ID로 모든 제출 조회 (통계용)
     */
    fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): List<Submission>
    
    /**
     * 출제별 제출 개수 조회
     */
    fun countByAssignmentId(assignmentId: Long): Long
} 