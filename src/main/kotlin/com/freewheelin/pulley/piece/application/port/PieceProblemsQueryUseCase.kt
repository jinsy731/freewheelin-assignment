package com.freewheelin.pulley.piece.application.port

import com.freewheelin.pulley.problem.domain.model.ProblemType

/**
 * 학습지 문제 조회 Use Case 포트 인터페이스
 * 
 * 학생이 자신에게 출제된 학습지의 문제들을 조회하는 기능을 정의합니다.
 */
interface PieceProblemsQueryUseCase {
    
    /**
     * 학습지의 문제들을 순서대로 조회
     * 
     * @param query 학습지 문제 조회 쿼리 정보
     * @return 순서대로 정렬된 문제 리스트가 포함된 응답
     */
    fun getProblemsInPiece(query: PieceProblemsQuery): PieceProblemsResult
}

/**
 * 학습지 문제 조회 쿼리 DTO
 */
data class PieceProblemsQuery(
    val pieceId: Long,
    val studentId: Long
) {
    init {
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(studentId > 0) { "학생 ID는 0보다 커야 합니다." }
    }
}

/**
 * 학습지 문제 조회 결과 DTO
 */
data class PieceProblemsResult(
    val problems: List<ProblemDetail>
)

/**
 * 문제 상세 정보 DTO
 */
data class ProblemDetail(
    val id: Long,
    val unitCode: String,
    val level: Int,
    val type: ProblemType
) 