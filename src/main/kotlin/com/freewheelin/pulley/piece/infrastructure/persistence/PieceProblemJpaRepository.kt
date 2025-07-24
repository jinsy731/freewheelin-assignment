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
     * 학습지 ID와 문제 ID로 매핑 조회
     */
    fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): PieceProblemJpaEntity?

    /**
     * 학습지 ID와 문제 ID 리스트로 기존 매핑 조회
     */
    fun findByPieceIdAndProblemIdIn(pieceId: Long, problemIds: List<Long>): List<PieceProblemJpaEntity>

    /**
     * 순서 변경을 위한 필요한 문제들만 조회 (성능 최적화)
     *
     * 로직:
     * 1. 지정된 문제들(target, prev, next) 조회
     * 2. prev와 next의 position이 모두 있으면, 그 사이의 문제들도 조회
     *
     * @param pieceId 학습지 ID
     * @param targetProblemId 이동할 문제 ID
     * @param prevProblemId 이전 문제 ID (null 가능)
     * @param nextProblemId 다음 문제 ID (null 가능)
     * @param prevPosition 이전 문제의 position (null 가능)
     * @param nextPosition 다음 문제의 position (null 가능)
     */
    @Query("""
        SELECT pp FROM PieceProblemJpaEntity pp 
        WHERE pp.pieceId = :pieceId 
        AND (
            pp.problemId = :targetProblemId
            OR (:prevProblemId IS NOT NULL AND pp.problemId = :prevProblemId)
            OR (:nextProblemId IS NOT NULL AND pp.problemId = :nextProblemId)
            OR (
                :prevPosition IS NOT NULL AND :nextPosition IS NOT NULL 
                AND pp.position > :prevPosition AND pp.position < :nextPosition
            )
        )
        ORDER BY pp.position
    """)
    fun findProblemsForOrderUpdate(
        pieceId: Long,
        targetProblemId: Long,
        prevProblemId: Long?,
        nextProblemId: Long?,
        prevPosition: Double?,
        nextPosition: Double?
    ): List<PieceProblemJpaEntity>

    /**
     * PieceProblem ID 기반 순서 변경을 위한 필요한 문제들만 조회 (성능 최적화)
     *
     * 로직:
     * 1. 지정된 PieceProblem들(target, prev, next) 조회
     * 2. prev와 next의 position이 모두 있으면, 그 사이의 문제들도 조회
     *
     * @param pieceId 학습지 ID
     * @param targetPieceProblemId 이동할 PieceProblem ID
     * @param prevPieceProblemId 이전 PieceProblem ID (null 가능)
     * @param nextPieceProblemId 다음 PieceProblem ID (null 가능)
     * @param prevPosition 이전 PieceProblem의 position (null 가능)
     * @param nextPosition 다음 PieceProblem의 position (null 가능)
     */
    @Query("""
        SELECT pp FROM PieceProblemJpaEntity pp 
        WHERE pp.pieceId = :pieceId 
        AND (
            pp.id = :targetPieceProblemId
            OR (:prevPieceProblemId IS NOT NULL AND pp.id = :prevPieceProblemId)
            OR (:nextPieceProblemId IS NOT NULL AND pp.id = :nextPieceProblemId)
            OR (
                :prevPosition IS NOT NULL AND :nextPosition IS NOT NULL 
                AND pp.position > :prevPosition AND pp.position < :nextPosition
            )
        )
        ORDER BY pp.position
    """)
    fun findPieceProblemsForOrderUpdate(
        pieceId: Long,
        targetPieceProblemId: Long,
        prevPieceProblemId: Long?,
        nextPieceProblemId: Long?,
        prevPosition: Double?,
        nextPosition: Double?
    ): List<PieceProblemJpaEntity>
} 