package com.freewheelin.pulley.statistics.application.service

import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.assignment.domain.model.SubmissionResult
import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import com.freewheelin.pulley.statistics.domain.port.PieceProblemStatRepository
import com.freewheelin.pulley.statistics.domain.port.PieceStudentStatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * StatisticsUpdateService 단위 테스트
 * 
 * Mock을 사용하여 비즈니스 로직을 검증합니다.
 */
class StatisticsUpdateServiceTest {
    
    private lateinit var pieceStudentStatRepository: PieceStudentStatRepository
    private lateinit var pieceProblemStatRepository: PieceProblemStatRepository
    private lateinit var statisticsUpdateService: StatisticsUpdateService
    
    @BeforeEach
    fun setUp() {
        pieceStudentStatRepository = mockk()
        pieceProblemStatRepository = mockk()
        statisticsUpdateService = StatisticsUpdateService(
            pieceStudentStatRepository,
            pieceProblemStatRepository
        )
    }
    
    @Test
    fun `새로운 학생 통계 생성 - 기존 통계가 없는 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = false),
                SubmissionResult(problemId = 103L, isCorrect = true)
            )
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(1001L) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 101L) } returns null
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 102L) } returns null
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 103L) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceStudentStatRepository.save(match { stat ->
                stat.assignmentId.value == 1001L &&
                stat.pieceId.value == 2001L &&
                stat.studentId.value == 3001L &&
                stat.totalCount == 3 &&
                stat.correctCount == 2 &&
                stat.correctnessRate.value == 2.0 / 3.0
            })
        }
        
        verify(exactly = 3) { pieceProblemStatRepository.save(any()) }
    }
    
    @Test
    fun `기존 학생 통계 업데이트 - 기존 통계가 있는 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = true),
                SubmissionResult(problemId = 103L, isCorrect = false)
            )
        )
        
        val existingStudentStat = PieceStudentStat(
            id = 1L,
            assignmentId = AssignmentId(1001L),
            pieceId = PieceId(2001L),
            studentId = StudentId(3001L),
            totalCount = 2,
            correctCount = 1,
            correctnessRate = CorrectnessRate(0.5)
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(1001L) } returns existingStudentStat
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(any(), any()) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceStudentStatRepository.save(match { stat ->
                stat.assignmentId.value == 1001L &&
                stat.totalCount == 3 &&
                stat.correctCount == 2 &&
                stat.correctnessRate.value == 2.0 / 3.0
            })
        }
    }
    
    @Test
    fun `새로운 문제 통계 생성 - 기존 통계가 없는 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = false)
            )
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(any()) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 101L) } returns null
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 102L) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.pieceId.value == 2001L &&
                stat.problemId.value == 101L &&
                stat.totalCount == 1 &&
                stat.correctCount == 1 &&
                stat.correctnessRate.value == 1.0
            })
        }
        
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.pieceId.value == 2001L &&
                stat.problemId.value == 102L &&
                stat.totalCount == 1 &&
                stat.correctCount == 0 &&
                stat.correctnessRate.value == 0.0
            })
        }
    }
    
    @Test
    fun `기존 문제 통계 업데이트 - 기존 통계가 있는 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true)
            )
        )
        
        val existingProblemStat = PieceProblemStat(
            id = 1L,
            pieceId = PieceId(2001L),
            problemId = ProblemId(101L),
            totalCount = 5,
            correctCount = 3,
            correctnessRate = CorrectnessRate(0.6)
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(any()) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(2001L, 101L) } returns existingProblemStat
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.pieceId.value == 2001L &&
                stat.problemId.value == 101L &&
                stat.totalCount == 6 && // 5 + 1
                stat.correctCount == 4 && // 3 + 1
                stat.correctnessRate.value == 4.0 / 6.0
            })
        }
    }
    
    @Test
    fun `복합 시나리오 - 다양한 문제의 통계 업데이트`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = false),
                SubmissionResult(problemId = 103L, isCorrect = true),
                SubmissionResult(problemId = 101L, isCorrect = false) // 같은 문제 중복
            )
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(any()) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(any(), any()) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        // 학생 통계: 전체 4개, 정답 2개
        verify(exactly = 1) {
            pieceStudentStatRepository.save(match { stat ->
                stat.totalCount == 4 &&
                stat.correctCount == 2
            })
        }
        
        // 문제별 통계 확인
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.problemId.value == 101L &&
                stat.totalCount == 2 && // 101번 문제가 2번 나옴
                stat.correctCount == 1 // 그 중 1번 정답
            })
        }
        
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.problemId.value == 102L &&
                stat.totalCount == 1 &&
                stat.correctCount == 0
            })
        }
        
        verify(exactly = 1) {
            pieceProblemStatRepository.save(match { stat ->
                stat.problemId.value == 103L &&
                stat.totalCount == 1 &&
                stat.correctCount == 1
            })
        }
    }
    
    @Test
    fun `모든 문제가 정답인 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = true),
                SubmissionResult(problemId = 103L, isCorrect = true)
            )
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(any()) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(any(), any()) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceStudentStatRepository.save(match { stat ->
                stat.totalCount == 3 &&
                stat.correctCount == 3 &&
                stat.correctnessRate.value == 1.0
            })
        }
        
        verify(exactly = 3) {
            pieceProblemStatRepository.save(match { stat ->
                stat.totalCount == 1 &&
                stat.correctCount == 1 &&
                stat.correctnessRate.value == 1.0
            })
        }
    }
    
    @Test
    fun `모든 문제가 오답인 경우`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = false),
                SubmissionResult(problemId = 102L, isCorrect = false)
            )
        )
        
        every { pieceStudentStatRepository.findByAssignmentId(any()) } returns null
        every { pieceStudentStatRepository.save(any()) } returnsArgument 0
        
        every { pieceProblemStatRepository.findByPieceIdAndProblemId(any(), any()) } returns null
        every { pieceProblemStatRepository.save(any()) } returnsArgument 0
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        verify(exactly = 1) {
            pieceStudentStatRepository.save(match { stat ->
                stat.totalCount == 2 &&
                stat.correctCount == 0 &&
                stat.correctnessRate.value == 0.0
            })
        }
        
        verify(exactly = 2) {
            pieceProblemStatRepository.save(match { stat ->
                stat.totalCount == 1 &&
                stat.correctCount == 0 &&
                stat.correctnessRate.value == 0.0
            })
        }
    }
} 