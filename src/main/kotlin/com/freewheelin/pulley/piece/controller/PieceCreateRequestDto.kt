package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.PieceCreateRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * 학습지 생성 API 요청 DTO
 */
data class PieceCreateRequestDto(
    @field:NotBlank(message = "학습지 이름은 필수입니다")
    @field:Size(max = 100, message = "학습지 이름은 최대 100자까지 입력 가능합니다")
    val title: String,

    @field:NotEmpty(message = "문제 ID 리스트는 비어있을 수 없습니다")
    @field:Size(max = 50, message = "학습지에 포함될 수 있는 최대 문제 수는 50개입니다")
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