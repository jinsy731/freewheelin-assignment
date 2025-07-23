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

} 