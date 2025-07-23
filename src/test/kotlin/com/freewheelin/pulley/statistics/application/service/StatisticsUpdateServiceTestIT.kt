package com.freewheelin.pulley.statistics.application.service

import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.assignment.domain.model.SubmissionResult
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceProblemStatJpaEntity
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceProblemStatJpaRepository
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceStudentStatJpaEntity
import com.freewheelin.pulley.statistics.infrastructure.persistence.PieceStudentStatJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * StatisticsUpdateService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatisticsUpdateServiceTestIT {
    
    @Autowired
    private lateinit var statisticsUpdateService: StatisticsUpdateService
    
    @Autowired
    private lateinit var pieceStudentStatJpaRepository: PieceStudentStatJpaRepository
    
    @Autowired
    private lateinit var pieceProblemStatJpaRepository: PieceProblemStatJpaRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        pieceStudentStatJpaRepository.deleteAll()
        pieceProblemStatJpaRepository.deleteAll()
    }
    
    @Test
    fun `정상적인 경우 - 새로운 통계 생성`() {
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
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        // 학생 통계 확인
        val studentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)
        assertNotNull(studentStat)
        assertEquals(1001L, studentStat.assignmentId)
        assertEquals(2001L, studentStat.pieceId)
        assertEquals(3001L, studentStat.studentId)
        assertEquals(3, studentStat.totalCount)
        assertEquals(2, studentStat.correctCount)
        assertEquals(2.0 / 3.0, studentStat.correctnessRate, 0.001)
        
        // 문제별 통계 확인
        val problemStats = pieceProblemStatJpaRepository.findByPieceId(2001L)
        assertEquals(3, problemStats.size)
        
        val problem101Stat = problemStats.find { it.problemId == 101L }!!
        assertEquals(1, problem101Stat.totalCount)
        assertEquals(1, problem101Stat.correctCount)
        assertEquals(1.0, problem101Stat.correctnessRate)
        
        val problem102Stat = problemStats.find { it.problemId == 102L }!!
        assertEquals(1, problem102Stat.totalCount)
        assertEquals(0, problem102Stat.correctCount)
        assertEquals(0.0, problem102Stat.correctnessRate)
        
        val problem103Stat = problemStats.find { it.problemId == 103L }!!
        assertEquals(1, problem103Stat.totalCount)
        assertEquals(1, problem103Stat.correctCount)
        assertEquals(1.0, problem103Stat.correctnessRate)
    }
    
    @Test
    fun `기존 학생 통계 업데이트`() {
        // Given: 기존 학생 통계 생성
        val existingStudentStat = PieceStudentStatJpaEntity(
            id = 0,
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            totalCount = 2,
            correctCount = 1,
            correctnessRate = 0.5
        )
        pieceStudentStatJpaRepository.save(existingStudentStat)
        
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = true)
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        val updatedStudentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(2, updatedStudentStat.totalCount) // 이벤트의 totalCount로 업데이트
        assertEquals(2, updatedStudentStat.correctCount) // 이벤트의 correctCount로 업데이트
        assertEquals(1.0, updatedStudentStat.correctnessRate, 0.001)
    }
    
    @Test
    fun `기존 문제 통계 업데이트 - 증분 추가`() {
        // Given: 기존 문제 통계 생성
        val existingProblemStat = PieceProblemStatJpaEntity(
            id = 0,
            pieceId = 2001L,
            problemId = 101L,
            totalCount = 5,
            correctCount = 3,
            correctnessRate = 0.6
        )
        pieceProblemStatJpaRepository.save(existingProblemStat)
        
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true)
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        val updatedProblemStat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 101L)!!
        assertEquals(6, updatedProblemStat.totalCount) // 5 + 1
        assertEquals(4, updatedProblemStat.correctCount) // 3 + 1
        assertEquals(4.0 / 6.0, updatedProblemStat.correctnessRate, 0.001)
    }
    
    @Test
    fun `같은 문제 중복 제출 처리`() {
        // Given
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 101L, isCorrect = false), // 같은 문제 중복
                SubmissionResult(problemId = 102L, isCorrect = true)
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        // 학생 통계: 전체 3개, 정답 2개
        val studentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(3, studentStat.totalCount)
        assertEquals(2, studentStat.correctCount)
        
        // 문제별 통계
        val problem101Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 101L)!!
        assertEquals(2, problem101Stat.totalCount) // 101번 문제가 2번 나옴
        assertEquals(1, problem101Stat.correctCount) // 그 중 1번 정답
        assertEquals(0.5, problem101Stat.correctnessRate)
        
        val problem102Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 102L)!!
        assertEquals(1, problem102Stat.totalCount)
        assertEquals(1, problem102Stat.correctCount)
        assertEquals(1.0, problem102Stat.correctnessRate)
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
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        val studentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(3, studentStat.totalCount)
        assertEquals(3, studentStat.correctCount)
        assertEquals(1.0, studentStat.correctnessRate)
        
        val problemStats = pieceProblemStatJpaRepository.findByPieceId(2001L)
        assertEquals(3, problemStats.size)
        problemStats.forEach { stat ->
            assertEquals(1, stat.totalCount)
            assertEquals(1, stat.correctCount)
            assertEquals(1.0, stat.correctnessRate)
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
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        val studentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(2, studentStat.totalCount)
        assertEquals(0, studentStat.correctCount)
        assertEquals(0.0, studentStat.correctnessRate)
        
        val problemStats = pieceProblemStatJpaRepository.findByPieceId(2001L)
        assertEquals(2, problemStats.size)
        problemStats.forEach { stat ->
            assertEquals(1, stat.totalCount)
            assertEquals(0, stat.correctCount)
            assertEquals(0.0, stat.correctnessRate)
        }
    }
    
    @Test
    fun `여러 학생의 통계 독립적으로 관리`() {
        // Given: 첫 번째 학생
        val event1 = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true),
                SubmissionResult(problemId = 102L, isCorrect = false)
            )
        )
        
        // 두 번째 학생
        val event2 = SubmissionGradedEvent(
            assignmentId = 1002L,
            pieceId = 2001L, // 같은 학습지
            studentId = 3002L, // 다른 학생
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = false),
                SubmissionResult(problemId = 102L, isCorrect = true)
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event1)
        statisticsUpdateService.updateStatistics(event2)
        
        // Then
        // 학생별 통계는 독립적
        val student1Stat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(3001L, student1Stat.studentId)
        assertEquals(2, student1Stat.totalCount)
        assertEquals(1, student1Stat.correctCount)
        assertEquals(0.5, student1Stat.correctnessRate)
        
        val student2Stat = pieceStudentStatJpaRepository.findByAssignmentId(1002L)!!
        assertEquals(3002L, student2Stat.studentId)
        assertEquals(2, student2Stat.totalCount)
        assertEquals(1, student2Stat.correctCount)
        assertEquals(0.5, student2Stat.correctnessRate)
        
        // 문제별 통계는 누적됨
        val problem101Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 101L)!!
        assertEquals(2, problem101Stat.totalCount) // 두 학생 모두 제출
        assertEquals(1, problem101Stat.correctCount) // 첫 번째 학생만 정답
        assertEquals(0.5, problem101Stat.correctnessRate)
        
        val problem102Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 102L)!!
        assertEquals(2, problem102Stat.totalCount) // 두 학생 모두 제출
        assertEquals(1, problem102Stat.correctCount) // 두 번째 학생만 정답
        assertEquals(0.5, problem102Stat.correctnessRate)
    }
    
    @Test
    fun `다른 학습지의 통계는 독립적으로 관리`() {
        // Given: 같은 문제 ID를 가진 다른 학습지들
        val event1 = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = true)
            )
        )
        
        val event2 = SubmissionGradedEvent(
            assignmentId = 1002L,
            pieceId = 2002L, // 다른 학습지
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = false) // 같은 문제 ID
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event1)
        statisticsUpdateService.updateStatistics(event2)
        
        // Then
        // 문제별 통계가 학습지별로 독립적으로 관리됨
        val piece1Problem101Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 101L)!!
        assertEquals(1, piece1Problem101Stat.totalCount)
        assertEquals(1, piece1Problem101Stat.correctCount)
        assertEquals(1.0, piece1Problem101Stat.correctnessRate)
        
        val piece2Problem101Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2002L, 101L)!!
        assertEquals(1, piece2Problem101Stat.totalCount)
        assertEquals(0, piece2Problem101Stat.correctCount)
        assertEquals(0.0, piece2Problem101Stat.correctnessRate)
    }
    
    @Test
    fun `복합 시나리오 - 기존 통계 업데이트와 새로운 통계 생성 혼재`() {
        // Given: 기존에 일부 문제 통계 존재
        val existingProblemStat = PieceProblemStatJpaEntity(
            id = 0,
            pieceId = 2001L,
            problemId = 101L,
            totalCount = 3,
            correctCount = 2,
            correctnessRate = 2.0 / 3.0
        )
        pieceProblemStatJpaRepository.save(existingProblemStat)
        
        val event = SubmissionGradedEvent(
            assignmentId = 1001L,
            pieceId = 2001L,
            studentId = 3001L,
            submissionResults = listOf(
                SubmissionResult(problemId = 101L, isCorrect = false), // 기존 통계가 있는 문제
                SubmissionResult(problemId = 102L, isCorrect = true)   // 새로운 문제
            )
        )
        
        // When
        statisticsUpdateService.updateStatistics(event)
        
        // Then
        // 학생 통계는 새로 생성
        val studentStat = pieceStudentStatJpaRepository.findByAssignmentId(1001L)!!
        assertEquals(2, studentStat.totalCount)
        assertEquals(1, studentStat.correctCount)
        assertEquals(0.5, studentStat.correctnessRate)
        
        // 기존 문제 통계는 업데이트 (누적)
        val updatedProblem101Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 101L)!!
        assertEquals(4, updatedProblem101Stat.totalCount) // 3 + 1
        assertEquals(2, updatedProblem101Stat.correctCount) // 2 + 0
        assertEquals(0.5, updatedProblem101Stat.correctnessRate, 0.001)
        
        // 새로운 문제 통계는 생성
        val newProblem102Stat = pieceProblemStatJpaRepository.findByPieceIdAndProblemId(2001L, 102L)!!
        assertEquals(1, newProblem102Stat.totalCount)
        assertEquals(1, newProblem102Stat.correctCount)
        assertEquals(1.0, newProblem102Stat.correctnessRate)
    }
} 