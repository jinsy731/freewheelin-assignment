package com.freewheelin.pulley.piece.domain.port

import com.freewheelin.pulley.piece.domain.model.PieceProblem

/**
 * 학습지-문제 매핑 저장소 포트 (Repository Interface)
 */
interface PieceProblemRepository {
    
    /**
     * 학습지-문제 매핑 저장
     */
    fun save(pieceProblem: PieceProblem): PieceProblem
    
    /**
     * 여러 학습지-문제 매핑 일괄 저장
     */
    fun saveAll(pieceProblems: List<PieceProblem>): List<PieceProblem>
    
    /**
     * 학습지 ID로 문제 매핑 조회 (position 순서대로)
     */
    fun findByPieceIdOrderByPosition(pieceId: Long): List<PieceProblem>
    

    /**
     * 학습지 ID와 여러 문제 ID로 매핑 조회
     */
    fun findByPieceIdAndProblemIdIn(pieceId: Long, problemIds: List<Long>): List<PieceProblem>

    /**
     * 순서 변경을 위한 필요한 문제들만 조회 (성능 최적화)
     *
     * 다음 문제들을 조회합니다:
     * 1. 이동할 문제 (problemId)
     * 2. 이전 문제 (prevProblemId, null 가능)
     * 3. 다음 문제 (nextProblemId, null 가능)
     * 4. 이전과 다음 사이에 있는 모든 문제들 (연속성 검증용)
     *
     * @param pieceId 학습지 ID
     * @param problemId 이동할 문제 ID
     * @param prevProblemId 이전 문제 ID (null 가능)
     * @param nextProblemId 다음 문제 ID (null 가능)
     * @return 순서 변경 검증에 필요한 문제들
     */
    fun findProblemsForOrderUpdate(
        pieceId: Long,
        problemId: Long,
        prevProblemId: Long?,
        nextProblemId: Long?
    ): List<PieceProblem>

    /**
     * PieceProblem ID 기반 순서 변경을 위한 필요한 문제들만 조회 (성능 최적화)
     *
     * 다음 문제들을 조회합니다:
     * 1. 이동할 PieceProblem (pieceProblemId)
     * 2. 이전 PieceProblem (prevPieceProblemId, null 가능)
     * 3. 다음 PieceProblem (nextPieceProblemId, null 가능)
     * 4. 이전과 다음 사이에 있는 모든 PieceProblem들 (연속성 검증용)
     *
     * @param pieceId 학습지 ID
     * @param pieceProblemId 이동할 PieceProblem ID
     * @param prevPieceProblemId 이전 PieceProblem ID (null 가능)
     * @param nextPieceProblemId 다음 PieceProblem ID (null 가능)
     * @return 순서 변경 검증에 필요한 PieceProblem들
     */
    fun findPieceProblemsForOrderUpdate(
        pieceId: Long,
        pieceProblemId: Long,
        prevPieceProblemId: Long?,
        nextPieceProblemId: Long?
    ): List<PieceProblem>
} 