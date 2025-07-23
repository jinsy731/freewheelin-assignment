package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import com.freewheelin.pulley.piece.application.port.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

/**
 * 학습지 관련 REST API Controller
 */
@RestController
@RequestMapping("/piece")
@Validated
class PieceController(
    private val pieceCreateUseCase: PieceCreateUseCase,
    private val pieceOrderUpdateUseCase: PieceOrderUpdateUseCase,
    private val pieceProblemsQueryUseCase: PieceProblemsQueryUseCase,
    private val pieceAnalysisUseCase: PieceAnalysisUseCase,
    private val securityService: SecurityService
) {
    
    /**
     * 학습지 생성 API (선생님 전용)
     * 
     * @param request 학습지 생성 요청 정보
     * @return 생성된 학습지 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    fun createPiece(@Valid @RequestBody request: PieceCreateRequestDto): ResponseEntity<PieceCreateResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val createRequest = request.toUseCaseRequest(currentUserId)
        val result = pieceCreateUseCase.createPiece(createRequest)
        val response = PieceCreateResponseDto.fromResult(result)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 학습지 문제 순서 수정 API (선생님 전용)
     * 
     * @param pieceId 학습지 ID
     * @param request 문제 순서 수정 요청 정보  
     * @return 수정 완료 응답
     */
    @PatchMapping("/{pieceId}/order")
    @PreAuthorize("hasRole('TEACHER')")
    fun updateProblemOrder(
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: ProblemOrderUpdateRequestDto
    ): ResponseEntity<Void> {
        val currentUserId = securityService.requireCurrentUserId()
        val updateCommand = request.toApplicationCommand(pieceId, currentUserId)
        pieceOrderUpdateUseCase.updateProblemOrder(updateCommand)
        
        return ResponseEntity.ok().build()
    }
    
    /**
     * 학습지 문제 목록 조회 API (선생님과 학생 모두 접근 가능)
     * 
     * @param pieceId 학습지 ID
     * @return 학습지 문제 목록
     */
    @GetMapping("/{pieceId}/problems")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    fun getProblems(
        @PathVariable @Positive pieceId: Long
    ): ResponseEntity<PieceProblemsResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val query = PieceProblemsQuery(pieceId, currentUserId)
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        val response = PieceProblemsResponseDto.from(result)
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 학습지 분석 API (선생님 전용)
     * 
     * @param pieceId 학습지 ID
     * @return 학습지 분석 결과
     */
    @GetMapping("/{pieceId}/analysis")
    @PreAuthorize("hasRole('TEACHER')")
    fun analyzePiece(
        @PathVariable @Positive pieceId: Long
    ): ResponseEntity<PieceAnalysisResponseDto> {
        val currentUserId = securityService.requireCurrentUserId()
        val analysisRequest = PieceAnalysisRequest(pieceId, currentUserId)
        val result = pieceAnalysisUseCase.analyzePiece(analysisRequest)
        val response = PieceAnalysisResponseDto.from(result)
        
        return ResponseEntity.ok(response)
    }
} 