package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateResult
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 학습지 출제 API 응답 DTO
 */
@Schema(description = "학습지 출제 응답")
data class AssignmentCreateResponseDto(
    @Schema(description = "학습지 ID", example = "1")
    val pieceId: Long,
    @Schema(description = "출제 요청된 총 학생 수", example = "5")
    val totalRequestedStudents: Int,
    @Schema(description = "새로 출제된 학생 수", example = "3")
    val newlyAssignedStudents: Int,
    @Schema(description = "이미 출제된 학생 수", example = "2")
    val alreadyAssignedStudents: Int,
    @Schema(description = "새로 출제된 학생 ID 목록", example = "[4, 5, 6]")
    val newlyAssignedStudentIds: List<Long>,
    @Schema(description = "이미 출제된 학생 ID 목록", example = "[7, 8]")
    val alreadyAssignedStudentIds: List<Long>,
    @Schema(description = "모든 학생 처리 완료 여부", example = "true")
    val isAllStudentsProcessed: Boolean
) {
    companion object {
        /**
         * Application 레이어 결과 객체를 응답 DTO로 변환
         */
        fun fromResult(result: AssignmentCreateResult): AssignmentCreateResponseDto {
            return AssignmentCreateResponseDto(
                pieceId = result.pieceId,
                totalRequestedStudents = result.totalRequestedStudents,
                newlyAssignedStudents = result.newlyAssignedStudents,
                alreadyAssignedStudents = result.alreadyAssignedStudents,
                newlyAssignedStudentIds = result.newlyAssignedStudentIds,
                alreadyAssignedStudentIds = result.alreadyAssignedStudentIds,
                isAllStudentsProcessed = result.isAllStudentsProcessed
            )
        }
    }
} 