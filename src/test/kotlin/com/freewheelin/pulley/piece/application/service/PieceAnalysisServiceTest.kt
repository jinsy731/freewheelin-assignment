package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.piece.application.port.PieceAnalysisRequest
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import com.freewheelin.pulley.statistics.domain.port.PieceProblemStatRepository
import com.freewheelin.pulley.statistics.domain.port.PieceStudentStatRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 간소화된 통계 테이블 기반 분석 서비스 테스트
 */
class PieceAnalysisServiceTest {
    
    private val pieceRepository = mockk<PieceRepository>()
    private val pieceStudentStatRepository = mockk<PieceStudentStatRepository>()
    private val pieceProblemStatRepository = mockk<PieceProblemStatRepository>()
    
    private val pieceAnalysisService = PieceAnalysisService(
        pieceRepository,
        pieceStudentStatRepository,
        pieceProblemStatRepository
    )
    
    @Test
    fun `analyzePiece - 정상 분석 테스트`() {
        // Given
        val request = PieceAnalysisRequest(pieceId = 100L, teacherId = 1L)
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        val studentStats = listOf(
            PieceStudentStat.create(1L, 100L, 10L, 3, 2),
            PieceStudentStat.create(2L, 100L, 20L, 2, 1)
        )
        val problemStats = listOf(
            PieceProblemStat.create(100L, 1001L, 2, 2),
            PieceProblemStat.create(100L, 1002L, 2, 1),
            PieceProblemStat.create(100L, 1003L, 1, 0)
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { pieceStudentStatRepository.findByPieceId(100L) } returns studentStats
        every { pieceProblemStatRepository.findByPieceId(100L) } returns problemStats
        
        // When
        val result = pieceAnalysisService.analyzePiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals("수학 학습지", result.pieceTitle)
        assertEquals(2, result.assignedStudents.size)
        assertEquals(3, result.problemStats.size)
        
        // 학생 통계 검증
        val firstStudent = result.assignedStudents[0]
        assertEquals(10L, firstStudent.studentId)
        assertEquals("학생10", firstStudent.studentName)
        assertEquals(2.0/3.0, firstStudent.correctnessRate, 0.001)
        
        // 문제 통계 검증
        val firstProblem = result.problemStats[0]
        assertEquals(1001L, firstProblem.problemId)
        assertEquals(1.0, firstProblem.correctnessRate, 0.001)
    }
    
    @Test
    fun `analyzePiece - 자신이 만든 학습지가 아니면 AuthorizationException 이 발생해야 한다`() {
        // Given
        val request = PieceAnalysisRequest(pieceId = 100L, teacherId = 999L)
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L), // 선생님 ID가 1L
            name = PieceName("수학 학습지")
        )
        
        every { pieceRepository.getById(100L) } returns piece
        
        // When & Then
        val exception = assertThrows<AuthorizationException> {
            pieceAnalysisService.analyzePiece(request)
        }
        
        assertTrue(exception.message!!.contains("권한") || exception.message!!.contains("Authorization"))
    }
    
    @Test
    fun `analyzePiece - 아무도 제출하지 않은 경우`() {
        // Given
        val request = PieceAnalysisRequest(pieceId = 100L, teacherId = 1L)
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { pieceStudentStatRepository.findByPieceId(100L) } returns emptyList()
        every { pieceProblemStatRepository.findByPieceId(100L) } returns emptyList()
        
        // When
        val result = pieceAnalysisService.analyzePiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals("수학 학습지", result.pieceTitle)
        assertEquals(0, result.assignedStudents.size)
        assertEquals(0, result.problemStats.size)
    }
} 