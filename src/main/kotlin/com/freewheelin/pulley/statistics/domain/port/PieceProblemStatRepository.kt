package com.freewheelin.pulley.statistics.domain.port

import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat

/**
 * 문제별 학습지 통계 Repository 포트
 */
interface PieceProblemStatRepository {
    
    /**
     * 통계 저장
     */
    fun save(stat: PieceProblemStat): PieceProblemStat
    
    /**
     * 여러 통계 저장
     */
    fun saveAll(stats: List<PieceProblemStat>): List<PieceProblemStat>
    
    /**
     * 학습지별 모든 문제 통계 조회
     */
    fun findByPieceId(pieceId: Long): List<PieceProblemStat>
    
    /**
     * 학습지와 문제로 통계 조회
     */
    fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): PieceProblemStat?
} 