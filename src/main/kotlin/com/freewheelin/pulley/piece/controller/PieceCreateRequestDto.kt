package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.PieceCreateRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * 학습지 생성 API 요청 DTO
 */
@Schema(description = "학습지 생성 요청")
data class PieceCreateRequestDto(
    @field:NotBlank(message = "학습지 이름은 필수입니다")
    @field:Size(max = 100, message = "학습지 이름은 최대 100자까지 입력 가능합니다")
    @Schema(description = "학습지 제목", example = "중간고사 대비 수학 문제집", required = true)
    val title: String,

    @field:NotEmpty(message = "문제 ID 리스트는 비어있을 수 없습니다")
    @field:Size(max = 50, message = "학습지에 포함될 수 있는 최대 문제 수는 50개입니다")
    @Schema(description = "포함할 문제 ID 목록", example = "[1001, 1002, 1003, 1051, 1052]", required = true)
    val problemIds: List<Long>
) {
    /**
     * DTO를 Application 레이어 요청 객체로 변환
     */
    fun toApplicationRequest(teacherId: Long): PieceCreateRequest {
        return PieceCreateRequest(
            teacherId = teacherId,
            title = title,
            problemIds = problemIds
        )
    }
    
    /**
     * DTO를 Use Case 요청 객체로 변환 (별칭)
     */
    fun toUseCaseRequest(teacherId: Long): PieceCreateRequest {
        return toApplicationRequest(teacherId)
    }
} 