package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateResult

/**
 * 학습지 출제 API 응답 DTO
 */
data class AssignmentCreateResponseDto(
    val pieceId: Long,
    val totalRequestedStudents: Int,
    val newlyAssignedStudents: Int,
    val alreadyAssignedStudents: Int,
    val newlyAssignedStudentIds: List<Long>,
    val alreadyAssignedStudentIds: List<Long>,
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