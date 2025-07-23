package com.freewheelin.pulley.assignment.controller

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 학습지 출제 API Controller
 */
@RestController
class AssignmentController(
    private val assignmentCreateUseCase: AssignmentCreateUseCase,
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
        @Valid @RequestBody request: AssignmentCreateRequestDto
    ): ResponseEntity<AssignmentCreateResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val applicationRequest = request.toCommand(currentUserId)
        val result = assignmentCreateUseCase.assignPiece(applicationRequest)
        val response = AssignmentCreateResponseDto.Companion.fromResult(result)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}