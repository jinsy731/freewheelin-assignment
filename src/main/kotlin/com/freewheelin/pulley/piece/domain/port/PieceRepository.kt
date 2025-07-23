package com.freewheelin.pulley.piece.domain.port

import com.freewheelin.pulley.piece.domain.model.Piece

/**
 * 학습지 Repository 포트 인터페이스
 */
interface PieceRepository {
    
    /**
     * 학습지 저장
     */
    fun save(piece: Piece): Piece
    
    /**
     * ID로 학습지 조회 (nullable)
     */
    fun findById(id: Long): Piece?
    
    /**
     * ID로 학습지 조회 (non-null, 없으면 예외)
     */
    fun getById(id: Long): Piece
} 