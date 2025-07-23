package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.PieceCreateResult
import com.freewheelin.pulley.piece.domain.model.Piece

/**
 * 학습지 응답 DTO
 */
data class PieceResponseDto(
    val id: Long,
    val teacherId: Long,
    val name: String
) {
    companion object {
        /**
         * 도메인 모델을 응답 DTO로 변환
         */
        fun fromDomain(piece: Piece): PieceResponseDto {
            return PieceResponseDto(
                id = piece.id.value,
                teacherId = piece.teacherId.value,
                name = piece.name.value
            )
        }
    }
}

/**
 * 학습지 생성 응답 DTO
 */
data class PieceCreateResponseDto(
    val pieceId: Long,
    val name: String,
    val message: String = "학습지가 성공적으로 생성되었습니다"
) {
    companion object {
        /**
         * 도메인 모델을 생성 응답 DTO로 변환
         */
        fun fromDomain(piece: Piece): PieceCreateResponseDto {
            return PieceCreateResponseDto(
                pieceId = piece.id.value,
                name = piece.name.value
            )
        }
        
        /**
         * Use Case 결과를 응답 DTO로 변환
         */
        fun fromResult(result: PieceCreateResult): PieceCreateResponseDto {
            return PieceCreateResponseDto(
                pieceId = result.pieceId,
                name = result.name
            )
        }
    }
} 