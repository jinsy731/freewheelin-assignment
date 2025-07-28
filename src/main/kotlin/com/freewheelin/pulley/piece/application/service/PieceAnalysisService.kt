package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.validateOwnership
import com.freewheelin.pulley.piece.application.port.*
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.statistics.domain.port.PieceProblemStatRepository
import com.freewheelin.pulley.statistics.domain.port.PieceStudentStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.map

/**
 * 학습지 분석 Application Service
 * 
 * 학습지에 대한 통계 분석 정보를 제공하는 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional(readOnly = true)
class PieceAnalysisService(
    private val pieceRepository: PieceRepository,
    private val pieceStudentStatRepository: PieceStudentStatRepository,
    private val pieceProblemStatRepository: PieceProblemStatRepository
) : PieceAnalysisUseCase {
    
    override fun analyzePiece(request: PieceAnalysisRequest): PieceAnalysisResult {
        // 1. 권한 검증
        val piece = pieceRepository.getById(request.pieceId)
        piece.validateOwnership(request.teacherId)
        
        // 2. 학생별 통계 조회
        val studentStats = pieceStudentStatRepository.findByPieceId(request.pieceId)
        val assignedStudents = studentStats.map { stat ->
            StudentStatistic(
                studentId = stat.studentId.value,
                studentName = "학생${stat.studentId.value}",
                correctnessRate = stat.correctnessRate.value
            )
        }
        
        // 3. 문제별 통계 조회
        val problemStats = pieceProblemStatRepository.findByPieceId(request.pieceId)
        val problemStatistics = problemStats.map { stat ->
            ProblemStatistic(
                problemId = stat.problemId.value,
                correctnessRate = stat.correctnessRate.value
            )
        }
        
        // 4. 결과 반환
        return PieceAnalysisResult(
            pieceId = piece.id.value,
            pieceTitle = piece.name.value,
            assignedStudents = assignedStudents,
            problemStats = problemStatistics
        )
    }
} 