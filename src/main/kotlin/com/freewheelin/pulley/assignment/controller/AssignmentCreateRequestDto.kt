package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 학습지 출제 API 요청 DTO
 */
@Schema(description = "학습지 출제 요청")
data class AssignmentCreateRequestDto(
    @field:NotEmpty(message = "학생 ID 리스트는 비어있을 수 없습니다")
    @Schema(description = "출제할 학생 ID 목록", example = "[4, 5, 6]", required = true)
    val studentIds: List<@Positive(message = "학생 ID는 양수여야 합니다") Long>
) {
    /**
     * DTO를 Application 레이어 요청 객체로 변환
     */
    fun toCommand(teacherId: Long, pieceId: Long): AssignmentCreateRequest {
        return AssignmentCreateRequest(
            teacherId = teacherId,
            pieceId = pieceId,
            studentIds = studentIds
        )
    }
} 