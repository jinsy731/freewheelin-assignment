package com.freewheelin.pulley.common.exception

import org.springframework.http.HttpStatus

/**
 * 시스템 전체에서 사용되는 에러 코드 정의
 *
 * 각 에러 코드는 고유한 식별자, 기본 메시지, HTTP 상태 코드를 포함합니다.
 */
enum class ErrorCode(
    val code: String,
    val defaultMessage: String,
    val httpStatus: HttpStatus
) {
    // ===== 공통 에러 코드 =====

    // 리소스 관련
    RESOURCE_NOT_FOUND("E001", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS("E002", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),

    // 권한 관련
    UNAUTHORIZED_ACCESS("E003", "해당 리소스에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    AUTHENTICATION_FAILED("E007", "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    // 입력 검증 관련
    INVALID_INPUT("E004", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("E005", "입력값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),

    // 상태 관련
    INVALID_STATE("E006", "현재 상태에서는 해당 작업을 수행할 수 없습니다.", HttpStatus.CONFLICT),

    // ===== Piece (학습지) 관련 =====
    PIECE_NOT_FOUND("P001", "학습지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PIECE_UNAUTHORIZED("P002", "해당 학습지에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    PIECE_NO_PROBLEMS("P003", "학습지에 문제가 없습니다.", HttpStatus.BAD_REQUEST),
    PIECE_INVALID_PROBLEMS("P004", "존재하지 않는 문제가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),

    // ===== Assignment (출제) 관련 =====
    ASSIGNMENT_NOT_FOUND("A001", "출제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ASSIGNMENT_ALREADY_SUBMITTED("A002", "이미 제출이 완료된 학습지입니다.", HttpStatus.CONFLICT),
    ASSIGNMENT_NOT_ASSIGNED("A003", "해당 학생에게 출제되지 않은 학습지입니다.", HttpStatus.FORBIDDEN),

    // ===== Problem (문제) 관련 =====
    PROBLEM_NOT_FOUND("PR001", "문제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PROBLEM_INVALID_IDS("PR002", "유효하지 않은 문제 ID가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    PROBLEM_NOT_IN_PIECE("PR003", "해당 문제는 이 학습지에 속해있지 않습니다.", HttpStatus.BAD_REQUEST),

    // ===== Submission (제출) 관련 =====
    SUBMISSION_DUPLICATE("S001", "중복 제출은 허용되지 않습니다.", HttpStatus.CONFLICT),
    SUBMISSION_INVALID_PROBLEMS("S002", "해당 학습지에 포함되지 않은 문제가 있습니다.", HttpStatus.BAD_REQUEST),
    SUBMISSION_EMPTY_ANSWERS("S003", "제출할 답안이 없습니다.", HttpStatus.BAD_REQUEST),

    // ===== 시스템 에러 =====
    INTERNAL_SERVER_ERROR("SYS001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("SYS002", "데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 에러 코드로부터 ErrorCode 찾기
     */
    companion object {
        fun fromCode(code: String): ErrorCode? {
            return values().find { it.code == code }
        }
    }
}