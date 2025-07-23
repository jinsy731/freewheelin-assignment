package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeUseCase
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * 학습지 출제 API Controller
 */
@RestController
@Validated
class AssignmentController(
    private val assignmentCreateUseCase: AssignmentCreateUseCase,
    private val submissionGradeUseCase: SubmissionGradeUseCase,
    private val securityService: SecurityService
) {
    
    /**
     * 학습지 출제 API (선생님 전용)
     * 
     * @param request 학습지 출제 요청 정보
     * @return 출제 결과 정보
     */
    @PostMapping("/piece/{pieceId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun assignPiece(
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: AssignmentCreateRequestDto
    ): ResponseEntity<AssignmentCreateResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val applicationRequest = request.toCommand(currentUserId, pieceId)
        val result = assignmentCreateUseCase.assignPiece(applicationRequest)
        val response = AssignmentCreateResponseDto.Companion.fromResult(result)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 답안 제출 및 자동 채점 API (선생님과 학생 모두 접근 가능)
     *
     * 학생이 제출한 답안을 자동으로 채점하고 정답률을 계산합니다.
     *
     * @param pieceId 학습지 ID
     * @param request 답안 제출 요청 정보
     * @return 채점 결과 정보
     */
    @PutMapping("/{pieceId}/score")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    fun submitAndGrade(
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: SubmissionGradeRequestDto
    ): ResponseEntity<SubmissionGradeResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val applicationRequest = request.toApplicationRequest(pieceId, currentUserId)
        val result = submissionGradeUseCase.submitAndGrade(applicationRequest)
        val response = SubmissionGradeResponseDto.fromResult(result)

        return ResponseEntity.ok(response)
    }
}