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
) : AssignmentApiSpec {
    
    @PostMapping("/piece/{pieceId}")
    @PreAuthorize("hasRole('TEACHER')")
    override fun assignPiece(
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: AssignmentCreateRequestDto
    ): ResponseEntity<AssignmentCreateResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val applicationRequest = request.toCommand(currentUserId, pieceId)
        val result = assignmentCreateUseCase.assignPiece(applicationRequest)
        val response = AssignmentCreateResponseDto.Companion.fromResult(result)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/piece/{pieceId}/score")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    override fun submitAndGrade(
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