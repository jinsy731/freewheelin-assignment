package com.freewheelin.pulley.statistics.domain.port

import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat

/**
 * 학생별 학습지 통계 Repository 포트
 */
interface PieceStudentStatRepository {
    
    /**
     * 통계 저장
     */
    fun save(stat: PieceStudentStat): PieceStudentStat
    
    /**
     * 여러 통계 저장
     */
    fun saveAll(stats: List<PieceStudentStat>): List<PieceStudentStat>
    
    /**
     * 출제 ID로 통계 조회
     */
    fun findByAssignmentId(assignmentId: Long): PieceStudentStat?
    
    /**
     * 학습지별 모든 학생 통계 조회
     */
    fun findByPieceId(pieceId: Long): List<PieceStudentStat>

} 