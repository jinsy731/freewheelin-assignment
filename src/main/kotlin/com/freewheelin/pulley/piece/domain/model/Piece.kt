package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.Ownable
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId


/**
 * 학습지 도메인 모델
 * 
 * 학습지 생성, 문제 관리, 출제 등의 비즈니스 로직을 포함
 */
data class Piece(
    val id: PieceId,
    val teacherId: TeacherId,
    val name: PieceName
) : Ownable<Long> {
    
    /**
     * 소유자 확인 (TeacherId 타입)
     */
    fun isOwnedBy(targetTeacherId: TeacherId): Boolean =
        teacherId == targetTeacherId
    
    /**
     * 소유자 확인 (Long 타입) - Ownable 인터페이스 구현
     */
    override fun isOwnedBy(targetTeacherId: Long): Boolean = 
        teacherId.value == targetTeacherId
    
    /**
     * 리소스 식별자 반환 - Ownable 인터페이스 구현
     */
    override fun getResourceId(): Any = id.value

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