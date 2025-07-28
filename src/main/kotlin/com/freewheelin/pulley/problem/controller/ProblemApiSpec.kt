package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import com.freewheelin.pulley.common.presentation.ErrorResponseExamples
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "문제 관리", description = "문제 검색 및 조회 API")
@SecurityRequirement(name = "userIdAuth")
interface ProblemApiSpec {

    @Operation(
        summary = "문제 검색",
        description = "조건에 맞는 문제들을 검색합니다. 선생님은 정답을 포함한 모든 정보를, 학생은 정답을 제외한 정보를 조회할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", 
                description = "문제 검색 성공",
                content = [Content(schema = Schema(implementation = ProblemSearchResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 파라미터 - 필수 파라미터 누락 또는 잘못된 형식",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.ValidationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "401", 
                description = "인증 실패",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthenticationErrorExample::class))]
            ),
            ApiResponse(
                responseCode = "403", 
                description = "권한 없음 - 선생님만 접근 가능",
                content = [Content(schema = Schema(implementation = ErrorResponseExamples.AuthorizationErrorExample::class))]
            )
        ]
    )
    fun getProblems(
        @Parameter(description = "조회할 총 문제 수", required = true, example = "10")
        @RequestParam(name = "totalCount", required = true) @NotNull @Min(1) totalCount: Int,
        @Parameter(description = "유형코드 리스트 (쉼표로 구분)", required = true, example = "uc1580,uc1581,uc1576")
        @RequestParam(name = "unitCodeList", required = true) @NotBlank unitCodeList: String,
        @Parameter(description = "난이도", required = true, example = "HIGH")
        @RequestParam(name = "level", required = true) @NotNull level: Level,
        @Parameter(description = "문제 유형", required = true, example = "ALL")
        @RequestParam(name = "problemType", required = true) @NotNull problemType: ProblemTypeFilter
    ): ResponseEntity<ProblemSearchResponseDto>
}