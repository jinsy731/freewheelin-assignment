package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.PieceCreateResult
import com.freewheelin.pulley.piece.domain.model.Piece
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 학습지 응답 DTO
 */
@Schema(description = "학습지 정보")
data class PieceResponseDto(
    @Schema(description = "학습지 ID", example = "1")
    val id: Long,
    @Schema(description = "선생님 ID", example = "1")
    val teacherId: Long,
    @Schema(description = "학습지 이름", example = "중간고사 대비 수학 문제집")
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
@Schema(description = "학습지 생성 응답")
data class PieceCreateResponseDto(
    @Schema(description = "생성된 학습지 ID", example = "1")
    val pieceId: Long,
    @Schema(description = "학습지 이름", example = "중간고사 대비 수학 문제집")
    val name: String,
    @Schema(description = "응답 메시지", example = "학습지가 성공적으로 생성되었습니다")
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