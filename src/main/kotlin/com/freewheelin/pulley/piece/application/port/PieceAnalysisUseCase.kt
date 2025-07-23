package com.freewheelin.pulley.piece.application.port

/**
 * 학습지 분석 Use Case 포트 인터페이스
 * 
 * 선생님이 자신의 학습지에 대한 학생들의 학습 통계를 조회하는 기능을 정의합니다.
 */
interface PieceAnalysisUseCase {
    
    /**
     * 학습지 분석
     * 
     * @param request 학습지 분석 요청 정보
     * @return 학습지 분석 결과
     */
    fun analyzePiece(request: PieceAnalysisRequest): PieceAnalysisResult
}

/**
 * 학습지 분석 요청 DTO
 */
data class PieceAnalysisRequest(
    val pieceId: Long,
    val teacherId: Long
) {
    init {
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(teacherId > 0) { "선생님 ID는 0보다 커야 합니다." }
    }
}

/**
 * 학습지 분석 결과 DTO
 */
data class PieceAnalysisResult(
    val pieceId: Long,
    val pieceTitle: String,
    val assignedStudents: List<StudentStatistic>,
    val problemStats: List<ProblemStatistic>
)

/**
 * 학생 통계 DTO
 */
data class StudentStatistic(
    val studentId: Long,
    val studentName: String,
    val correctnessRate: Double
)

/**
 * 문제 통계 DTO
 */
data class ProblemStatistic(
    val problemId: Long,
    val correctnessRate: Double
) 