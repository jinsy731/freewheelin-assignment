package com.freewheelin.pulley.assignment.controller

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

@Tag(name = "학습지 출제 및 채점", description = "학습지 출제, 답안 제출 및 자동 채점 API")
@SecurityRequirement(name = "userIdAuth")
interface AssignmentApiSpec {

    @Operation(
        summary = "학습지 출제",
        description = "선생님이 학생들에게 학습지를 출제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", 
                description = "학습지 출제 성공",
                content = [Content(schema = Schema(implementation = AssignmentCreateResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 - 학생 ID 리스트 오류",
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
    fun assignPiece(
        @Parameter(description = "학습지 ID", required = true, example = "1")
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: AssignmentCreateRequestDto
    ): ResponseEntity<AssignmentCreateResponseDto>

    @Operation(
        summary = "답안 제출 및 자동 채점",
        description = "학생이 답안을 제출하면 자동으로 채점하여 정답률을 계산합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", 
                description = "답안 제출 및 채점 성공",
                content = [Content(schema = Schema(implementation = SubmissionGradeResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 - 답안 형식 오류",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.ValidationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 출제되지 않은 학습지",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AssignmentNotAssignedErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "404", 
                description = "출제 정보를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AssignmentNotFoundErrorExample::class))]
            )
        ]
    )
    fun submitAndGrade(
        @Parameter(description = "학습지 ID", required = true, example = "1")
        @PathVariable @Positive pieceId: Long,
        @Valid @RequestBody request: SubmissionGradeRequestDto
    ): ResponseEntity<SubmissionGradeResponseDto>
}