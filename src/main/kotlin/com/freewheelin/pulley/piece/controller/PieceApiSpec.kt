package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.*
import com.freewheelin.pulley.common.presentation.ErrorResponseExamples
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "학습지 관리", description = "학습지 생성, 수정, 조회 및 분석 API")
@SecurityRequirement(name = "userIdAuth")
interface PieceApiSpec {

    @Operation(
        summary = "학습지 생성",
        description = "선생님이 새로운 학습지를 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", 
                description = "학습지 생성 성공",
                content = [Content(schema = Schema(implementation = PieceCreateResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 - 입력값 검증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.ValidationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패 - 헤더 누락 또는 잘못된 사용자",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 선생님만 접근 가능",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthorizationErrorExample::class))]
            )
        ]
    )
    fun createPiece(@Valid @RequestBody request: PieceCreateRequestDto): ResponseEntity<PieceCreateResponseDto>

    @Operation(
        summary = "학습지 문제 순서 수정",
        description = "선생님이 학습지 내 문제들의 순서를 변경합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "문제 순서 수정 성공"),
            ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 - 잘못된 순서 정보",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.ValidationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 학습지 소유자가 아님",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceUnauthorizedErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "404", 
                description = "학습지를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceNotFoundErrorExample::class))]
            )
        ]
    )
    fun updateProblemOrder(
        @Parameter(description = "학습지 ID", required = true, example = "1")
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: ProblemOrderUpdateRequestDto
    ): ResponseEntity<Void>

    @Operation(
        summary = "학습지 문제 목록 조회",
        description = "학습지에 포함된 문제들의 목록을 조회합니다. 선생님은 정답을 포함한 모든 정보를, 학생은 정답을 제외한 정보를 조회할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", 
                description = "문제 목록 조회 성공",
                content = [Content(schema = Schema(implementation = PieceProblemsResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 학습지 접근 권한 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceUnauthorizedErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "404", 
                description = "학습지를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceNotFoundErrorExample::class))]
            )
        ]
    )
    fun getProblems(
        @Parameter(description = "학습지 ID", required = true, example = "1")
        @PathVariable @Positive pieceId: Long
    ): ResponseEntity<PieceProblemsResponseDto>

    @Operation(
        summary = "학습지 분석",
        description = "선생님이 학습지의 통계 및 분석 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", 
                description = "학습지 분석 성공",
                content = [Content(schema = Schema(implementation = PieceAnalysisResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 학습지 소유자가 아님",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceUnauthorizedErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "404", 
                description = "학습지를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.PieceNotFoundErrorExample::class))]
            )
        ]
    )
    fun analyzePiece(
        @Parameter(description = "학습지 ID", required = true, example = "1")
        @PathVariable @Positive pieceId: Long
    ): ResponseEntity<PieceAnalysisResponseDto>
}