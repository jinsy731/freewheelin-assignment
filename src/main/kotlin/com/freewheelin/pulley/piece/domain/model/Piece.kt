package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode

/**
 * 학습지 도메인 모델
 * 
 * 학습지 생성, 문제 관리, 출제 등의 비즈니스 로직을 포함
 */
data class Piece(
    val id: PieceId,
    val teacherId: TeacherId,
    val name: PieceName
) {
    
    /**
     * 소유자 확인
     */
    fun isOwnedBy(targetTeacherId: TeacherId): Boolean =
        teacherId == targetTeacherId
    
    /**
     * 소유자 확인 (Long 타입)
     */
    fun isOwnedBy(targetTeacherId: Long): Boolean = 
        teacherId.value == targetTeacherId
    
    /**
     * 소유권 검증 (실패시 예외)
     */
    fun validateOwnership(targetTeacherId: Long) {
        if (!isOwnedBy(targetTeacherId)) {
            throw AuthorizationException(
                ErrorCode.PIECE_UNAUTHORIZED,
                targetTeacherId,
                "Piece",
                id.value
            )
        }
    }

    companion object {
        /**
         * 새로운 학습지 생성
         */
        fun create(
            teacherId: TeacherId,
            name: PieceName
        ): Piece {
            require(name.isValid()) { "유효한 학습지 이름을 입력해주세요." }
            
            return Piece(
                id = PieceId(0), // JPA가 자동 생성할 임시 ID
                teacherId = teacherId,
                name = name
            )
        }
    }
} 