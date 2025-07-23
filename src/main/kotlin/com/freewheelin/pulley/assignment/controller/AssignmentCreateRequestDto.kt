package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 학습지 출제 API 요청 DTO
 */
data class AssignmentCreateRequestDto(
    @field:NotNull(message = "학습지 ID는 필수입니다")
    @field:Positive(message = "학습지 ID는 양수여야 합니다")
    val pieceId: Long,
    
    @field:NotEmpty(message = "학생 ID 리스트는 비어있을 수 없습니다")
    val studentIds: List<@Positive(message = "학생 ID는 양수여야 합니다") Long>
) {
    /**
     * DTO를 Application 레이어 요청 객체로 변환
     */
    fun toCommand(teacherId: Long): AssignmentCreateRequest {
        return AssignmentCreateRequest(
            teacherId = teacherId,
            pieceId = pieceId,
            studentIds = studentIds
        )
    }
} 