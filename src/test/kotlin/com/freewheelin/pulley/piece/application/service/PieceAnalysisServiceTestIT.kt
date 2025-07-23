package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceAnalysisRequest
import com.freewheelin.pulley.piece.application.port.PieceAnalysisUseCase
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceProblemStatJpaEntity
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceStudentStatJpaRepository
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceProblemStatJpaRepository
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceStudentStatJpaEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PieceAnalysisService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PieceAnalysisServiceTestIT {
    
    @Autowired
    private lateinit var pieceAnalysisUseCase: PieceAnalysisUseCase
    
    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository
    
    @Autowired
    private lateinit var pieceStudentStatJpaRepository: PieceStudentStatJpaRepository
    
    @Autowired
    private lateinit var pieceProblemStatJpaRepository: PieceProblemStatJpaRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        pieceStudentStatJpaRepository.deleteAll()
        pieceProblemStatJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        
        // 테스트 학습지 데이터 삽입
        val testPieces = listOf(
            PieceJpaEntity(id = 0, teacherId = 1L, name = "수학 기초"),
            PieceJpaEntity(id = 0, teacherId = 1L, name = "영어 문법"),
            PieceJpaEntity(id = 0, teacherId = 2L, name = "과학 실험"),
            PieceJpaEntity(id = 0, teacherId = 3L, name = "국어 독해")
        )
        
        pieceJpaRepository.saveAll(testPieces)
    }
    
    @Test
    fun `정상적인 경우 - 학습지 분석 성공`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 학생별 통계 데이터
        val studentStats = listOf(
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1001L,
                pieceId = piece.id,
                studentId = 101L,
                totalCount = 10,
                correctCount = 8,
                correctnessRate = 0.8
            ),
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1002L,
                pieceId = piece.id,
                studentId = 102L,
                totalCount = 10,
                correctCount = 6,
                correctnessRate = 0.6
            ),
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1003L,
                pieceId = piece.id,
                studentId = 103L,
                totalCount = 10,
                correctCount = 9,
                correctnessRate = 0.9
            )
        )
        pieceStudentStatJpaRepository.saveAll(studentStats)
        
        // 문제별 통계 데이터
        val problemStats = listOf(
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 201L,
                totalCount = 3,
                correctCount = 2,
                correctnessRate = 0.667
            ),
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 202L,
                totalCount = 3,
                correctCount = 3,
                correctnessRate = 1.0
            ),
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 203L,
                totalCount = 3,
                correctCount = 1,
                correctnessRate = 0.333
            )
        )
        pieceProblemStatJpaRepository.saveAll(problemStats)
        
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 1L
        )
        
        // When
        val result = pieceAnalysisUseCase.analyzePiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals("수학 기초", result.pieceTitle)
        
        // 학생별 통계 검증
        assertEquals(3, result.assignedStudents.size)
        val studentStatsMap = result.assignedStudents.associateBy { it.studentId }
        
        assertEquals(0.8, studentStatsMap[101L]!!.correctnessRate, 0.001)
        assertEquals("학생101", studentStatsMap[101L]!!.studentName)
        
        assertEquals(0.6, studentStatsMap[102L]!!.correctnessRate, 0.001)
        assertEquals("학생102", studentStatsMap[102L]!!.studentName)
        
        assertEquals(0.9, studentStatsMap[103L]!!.correctnessRate, 0.001)
        assertEquals("학생103", studentStatsMap[103L]!!.studentName)
        
        // 문제별 통계 검증
        assertEquals(3, result.problemStats.size)
        val problemStatsMap = result.problemStats.associateBy { it.problemId }
        
        assertEquals(0.667, problemStatsMap[201L]!!.correctnessRate, 0.001)
        assertEquals(1.0, problemStatsMap[202L]!!.correctnessRate, 0.001)
        assertEquals(0.333, problemStatsMap[203L]!!.correctnessRate, 0.001)
    }
    
    @Test
    fun `통계 데이터가 없는 경우 - 빈 결과 반환`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 1L
        )
        
        // When
        val result = pieceAnalysisUseCase.analyzePiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals("수학 기초", result.pieceTitle)
        assertTrue(result.assignedStudents.isEmpty())
        assertTrue(result.problemStats.isEmpty())
    }
    
    @Test
    fun `학생별 통계만 있는 경우 - 학생 통계만 반환`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 학생별 통계만 생성
        val studentStats = listOf(
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1001L,
                pieceId = piece.id,
                studentId = 101L,
                totalCount = 5,
                correctCount = 4,
                correctnessRate = 0.8
            )
        )
        pieceStudentStatJpaRepository.saveAll(studentStats)
        
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 1L
        )
        
        // When
        val result = pieceAnalysisUseCase.analyzePiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals(1, result.assignedStudents.size)
        assertEquals(101L, result.assignedStudents[0].studentId)
        assertEquals(0.8, result.assignedStudents[0].correctnessRate)
        assertTrue(result.problemStats.isEmpty())
    }
    
    @Test
    fun `문제별 통계만 있는 경우 - 문제 통계만 반환`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 문제별 통계만 생성
        val problemStats = listOf(
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 201L,
                totalCount = 2,
                correctCount = 1,
                correctnessRate = 0.5
            )
        )
        pieceProblemStatJpaRepository.saveAll(problemStats)
        
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 1L
        )
        
        // When
        val result = pieceAnalysisUseCase.analyzePiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertTrue(result.assignedStudents.isEmpty())
        assertEquals(1, result.problemStats.size)
        assertEquals(201L, result.problemStats[0].problemId)
        assertEquals(0.5, result.problemStats[0].correctnessRate)
    }
    
    @Test
    fun `권한이 없는 선생님이 분석하려는 경우 - AuthorizationException 발생`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0] // teacherId = 1L인 학습지
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 2L // 다른 선생님 ID
        )
        
        // When & Then
        assertThrows<AuthorizationException> {
            pieceAnalysisUseCase.analyzePiece(request)
        }
    }
    
    @Test
    fun `존재하지 않는 학습지 분석 - NotFoundException 발생`() {
        // Given
        val nonExistentPieceId = 999L
        val request = PieceAnalysisRequest(
            pieceId = nonExistentPieceId,
            teacherId = 1L
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            pieceAnalysisUseCase.analyzePiece(request)
        }
    }
    
    @Test
    fun `여러 학생과 문제의 통계 - 정확한 매핑 확인`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 다양한 정답률의 학생 통계
        val studentStats = listOf(
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1001L,
                pieceId = piece.id,
                studentId = 101L,
                totalCount = 10,
                correctCount = 10,
                correctnessRate = 1.0
            ),
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1002L,
                pieceId = piece.id,
                studentId = 102L,
                totalCount = 10,
                correctCount = 0,
                correctnessRate = 0.0
            ),
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1003L,
                pieceId = piece.id,
                studentId = 103L,
                totalCount = 10,
                correctCount = 5,
                correctnessRate = 0.5
            )
        )
        pieceStudentStatJpaRepository.saveAll(studentStats)
        
        // 다양한 정답률의 문제 통계
        val problemStats = listOf(
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 201L,
                totalCount = 3,
                correctCount = 3,
                correctnessRate = 1.0
            ),
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 202L,
                totalCount = 3,
                correctCount = 0,
                correctnessRate = 0.0
            ),
            PieceProblemStatJpaEntity(
                id = 0,
                pieceId = piece.id,
                problemId = 203L,
                totalCount = 3,
                correctCount = 1,
                correctnessRate = 0.333
            )
        )
        pieceProblemStatJpaRepository.saveAll(problemStats)
        
        val request = PieceAnalysisRequest(
            pieceId = piece.id,
            teacherId = 1L
        )
        
        // When
        val result = pieceAnalysisUseCase.analyzePiece(request)
        
        // Then
        assertEquals(3, result.assignedStudents.size)
        assertEquals(3, result.problemStats.size)
        
        // 정답률 순서와 상관없이 올바른 매핑 확인
        val studentRatesMap = result.assignedStudents.associate { it.studentId to it.correctnessRate }
        val problemRatesMap = result.problemStats.associate { it.problemId to it.correctnessRate }
        
        assertEquals(1.0, studentRatesMap[101L])
        assertEquals(0.0, studentRatesMap[102L])
        assertEquals(0.5, studentRatesMap[103L])
        
        assertEquals(1.0, problemRatesMap[201L])
        assertEquals(0.0, problemRatesMap[202L])
        assertEquals(0.333, problemRatesMap[203L]!!, 0.001)
    }
    
    @Test
    fun `같은 선생님의 다른 학습지들 - 각각 독립적인 분석`() {
        // Given
        val pieces = pieceJpaRepository.findByTeacherId(1L)
        val piece1 = pieces[0]
        val piece2 = pieces[1]
        
        // 첫 번째 학습지 통계
        val piece1StudentStats = listOf(
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1001L,
                pieceId = piece1.id,
                studentId = 101L,
                totalCount = 5,
                correctCount = 5,
                correctnessRate = 1.0
            )
        )
        
        // 두 번째 학습지 통계
        val piece2StudentStats = listOf(
            PieceStudentStatJpaEntity(
                id = 0,
                assignmentId = 1002L,
                pieceId = piece2.id,
                studentId = 102L,
                totalCount = 5,
                correctCount = 2,
                correctnessRate = 0.4
            )
        )
        
        pieceStudentStatJpaRepository.saveAll(piece1StudentStats + piece2StudentStats)
        
        val request1 = PieceAnalysisRequest(pieceId = piece1.id, teacherId = 1L)
        val request2 = PieceAnalysisRequest(pieceId = piece2.id, teacherId = 1L)
        
        // When
        val result1 = pieceAnalysisUseCase.analyzePiece(request1)
        val result2 = pieceAnalysisUseCase.analyzePiece(request2)
        
        // Then
        assertEquals(piece1.id, result1.pieceId)
        assertEquals("수학 기초", result1.pieceTitle)
        assertEquals(1, result1.assignedStudents.size)
        assertEquals(101L, result1.assignedStudents[0].studentId)
        assertEquals(1.0, result1.assignedStudents[0].correctnessRate)
        
        assertEquals(piece2.id, result2.pieceId)
        assertEquals("영어 문법", result2.pieceTitle)
        assertEquals(1, result2.assignedStudents.size)
        assertEquals(102L, result2.assignedStudents[0].studentId)
        assertEquals(0.4, result2.assignedStudents[0].correctnessRate)
    }
} 