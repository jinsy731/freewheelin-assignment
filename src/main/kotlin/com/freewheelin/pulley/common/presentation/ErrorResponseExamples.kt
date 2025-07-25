package com.freewheelin.pulley.common.presentation

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Swagger 문서화를 위한 에러 응답 예시들
 */
object ErrorResponseExamples {

    @Schema(description = "입력값 검증 실패 에러")
    data class ValidationErrorExample(
        @Schema(description = "에러 코드", example = "E005")
        val errorCode: String = "E005",
        @Schema(description = "에러 메시지", example = "입력값 검증에 실패했습니다")
        val message: String = "입력값 검증에 실패했습니다",
        @Schema(description = "상세 에러 정보", example = "[\"title: 학습지 이름은 필수입니다\", \"problemIds: 문제 ID 리스트는 비어있을 수 없습니다\"]")
        val details: List<String> = listOf("title: 학습지 이름은 필수입니다", "problemIds: 문제 ID 리스트는 비어있을 수 없습니다"),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece")
        val path: String = "/piece"
    )

    @Schema(description = "인증 실패 에러")
    data class AuthenticationErrorExample(
        @Schema(description = "에러 코드", example = "E007")
        val errorCode: String = "E007",
        @Schema(description = "에러 메시지", example = "인증에 실패했습니다")
        val message: String = "인증에 실패했습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece")
        val path: String = "/piece"
    )

    @Schema(description = "권한 없음 에러")
    data class AuthorizationErrorExample(
        @Schema(description = "에러 코드", example = "E003")
        val errorCode: String = "E003",
        @Schema(description = "에러 메시지", example = "해당 리소스에 접근할 권한이 없습니다")
        val message: String = "해당 리소스에 접근할 권한이 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece")
        val path: String = "/piece"
    )

    @Schema(description = "리소스 찾을 수 없음 에러")
    data class NotFoundErrorExample(
        @Schema(description = "에러 코드", example = "E001")
        val errorCode: String = "E001",
        @Schema(description = "에러 메시지", example = "요청한 리소스를 찾을 수 없습니다")
        val message: String = "요청한 리소스를 찾을 수 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece/999")
        val path: String = "/piece/999"
    )

    @Schema(description = "서버 내부 오류")
    data class InternalServerErrorExample(
        @Schema(description = "에러 코드", example = "SYS001")
        val errorCode: String = "SYS001",
        @Schema(description = "에러 메시지", example = "서버 내부 오류가 발생했습니다")
        val message: String = "서버 내부 오류가 발생했습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece")
        val path: String = "/piece"
    )

    // === 학습지 관련 에러 예시 ===
    @Schema(description = "학습지 찾을 수 없음 에러")
    data class PieceNotFoundErrorExample(
        @Schema(description = "에러 코드", example = "P001")
        val errorCode: String = "P001",
        @Schema(description = "에러 메시지", example = "학습지를 찾을 수 없습니다")
        val message: String = "학습지를 찾을 수 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece/999")
        val path: String = "/piece/999"
    )

    @Schema(description = "학습지 권한 없음 에러")
    data class PieceUnauthorizedErrorExample(
        @Schema(description = "에러 코드", example = "P002")
        val errorCode: String = "P002",
        @Schema(description = "에러 메시지", example = "해당 학습지에 대한 권한이 없습니다")
        val message: String = "해당 학습지에 대한 권한이 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece/1")
        val path: String = "/piece/1"
    )

    // === 출제 관련 에러 예시 ===
    @Schema(description = "출제 정보 찾을 수 없음 에러")
    data class AssignmentNotFoundErrorExample(
        @Schema(description = "에러 코드", example = "A001")
        val errorCode: String = "A001",
        @Schema(description = "에러 메시지", example = "출제 정보를 찾을 수 없습니다")
        val message: String = "출제 정보를 찾을 수 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece/1/score")
        val path: String = "/piece/1/score"
    )

    @Schema(description = "출제되지 않은 학습지 에러")
    data class AssignmentNotAssignedErrorExample(
        @Schema(description = "에러 코드", example = "A003")
        val errorCode: String = "A003",
        @Schema(description = "에러 메시지", example = "해당 학생에게 출제되지 않은 학습지입니다")
        val message: String = "해당 학생에게 출제되지 않은 학습지입니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/piece/1/score")
        val path: String = "/piece/1/score"
    )

    // === 문제 관련 에러 예시 ===
    @Schema(description = "문제 찾을 수 없음 에러")
    data class ProblemNotFoundErrorExample(
        @Schema(description = "에러 코드", example = "PR001")
        val errorCode: String = "PR001",
        @Schema(description = "에러 메시지", example = "문제를 찾을 수 없습니다")
        val message: String = "문제를 찾을 수 없습니다",
        @Schema(description = "상세 에러 정보", example = "[]")
        val details: List<String> = emptyList(),
        @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        @Schema(description = "요청 경로", example = "/problems")
        val path: String = "/problems"
    )
}