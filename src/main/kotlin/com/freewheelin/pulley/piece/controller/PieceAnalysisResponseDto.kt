package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.PieceAnalysisResult

/**
 * 학습지 분석 응답 DTO
 */
data class PieceAnalysisResponseDto(
    val pieceId: Long,
    val pieceTitle: String,
    val assignedStudents: List<AssignedStudentDto>,
    val problemStats: List<ProblemStatDto>
) {
    companion object {
        fun from(result: PieceAnalysisResult): PieceAnalysisResponseDto {
            return PieceAnalysisResponseDto(
                pieceId = result.pieceId,
                pieceTitle = result.pieceTitle,
                assignedStudents = result.assignedStudents.map { 
                    AssignedStudentDto.from(it) 
                },
                problemStats = result.problemStats.map { 
                    ProblemStatDto.from(it) 
                }
            )
        }
    }
}

/**
 * 출제된 학생 DTO
 */
data class AssignedStudentDto(
    val studentId: Long,
    val studentName: String,
    val correctnessRate: Double
) {
    companion object {
        fun from(statistic: com.freewheelin.pulley.piece.application.port.StudentStatistic): AssignedStudentDto {
            return AssignedStudentDto(
                studentId = statistic.studentId,
                studentName = statistic.studentName,
                correctnessRate = statistic.correctnessRate
            )
        }
    }
}

/**
 * 문제 통계 DTO
 */
data class ProblemStatDto(
    val problemId: Long,
    val correctnessRate: Double
) {
    companion object {
        fun from(statistic: com.freewheelin.pulley.piece.application.port.ProblemStatistic): ProblemStatDto {
            return ProblemStatDto(
                problemId = statistic.problemId,
                correctnessRate = statistic.correctnessRate
            )
        }
    }
} 