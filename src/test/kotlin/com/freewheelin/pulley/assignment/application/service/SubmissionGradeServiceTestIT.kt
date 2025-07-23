package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AnswerSubmission
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeUseCase
import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaEntity
import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaRepository
import com.freewheelin.pulley.assignment.infrastructure.persistence.SubmissionJpaRepository
import com.freewheelin.pulley.common.exception.BusinessRuleViolationException
import com.freewheelin.pulley.common.exception.InvalidStateException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaRepository
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaEntity
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * SubmissionGradeService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubmissionGradeServiceTestIT {
    
    @Autowired
    private lateinit var submissionGradeUseCase: SubmissionGradeUseCase
    
    @Autowired
    private lateinit var assignmentJpaRepository: AssignmentJpaRepository
    
    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository
    
    @Autowired
    private lateinit var pieceProblemJpaRepository: PieceProblemJpaRepository
    
    @Autowired
    private lateinit var problemJpaRepository: ProblemJpaRepository
    
    @Autowired
    private lateinit var submissionJpaRepository: SubmissionJpaRepository
    
    private var testPieceId = 0L
    private var testAssignmentId = 0L
    private val testProblemIds = mutableListOf<Long>()
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        submissionJpaRepository.deleteAll()
        assignmentJpaRepository.deleteAll()
        pieceProblemJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        problemJpaRepository.deleteAll()
        
        // 1. 테스트 문제들 생성
        val testProblems = listOf(
            ProblemJpaEntity(
                id = 0,
                answer = "답1",
                unitCode = "UC001",
                level = 1,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답2", unitCode = "UC001", level = 2, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(
                id = 0,
                answer = "답3",
                unitCode = "UC002",
                level = 3,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답4", unitCode = "UC002", level = 4, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(id = 0, answer = "답5", unitCode = "UC003", level = 5, problemType = ProblemType.SUBJECTIVE)
        )
        val savedProblems = problemJpaRepository.saveAll(testProblems)
        testProblemIds.addAll(savedProblems.map { it.id })
        
        // 2. 테스트 학습지 생성
        val testPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "채점 테스트 학습지")
        val savedPiece = pieceJpaRepository.save(testPiece)
        testPieceId = savedPiece.id
        
        // 3. 학습지-문제 매핑 생성
        val testPieceProblems = testProblemIds.mapIndexed { index, problemId ->
            PieceProblemJpaEntity(
                id = 0,
                pieceId = testPieceId,
                problemId = problemId,
                position = (index + 1).toDouble()
            )
        }
        pieceProblemJpaRepository.saveAll(testPieceProblems)
        
        // 4. 학생에게 학습지 출제
        val testAssignment = AssignmentJpaEntity(
            id = 0,
            pieceId = testPieceId,
            studentId = 101L,
            assignedAt = LocalDateTime.now()
        )
        val savedAssignment = assignmentJpaRepository.save(testAssignment)
        testAssignmentId = savedAssignment.id
    }
    
    @Test
    fun `submitAndGrade - 정상적인 경우_답안 제출 및 채점 성공`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "답1"), // 정답
                AnswerSubmission(problemId = testProblemIds[1], answer = "틀린답"), // 오답
                AnswerSubmission(problemId = testProblemIds[2], answer = "답3"), // 정답
                AnswerSubmission(problemId = testProblemIds[3], answer = "답4"), // 정답
                AnswerSubmission(problemId = testProblemIds[4], answer = "틀린답") // 오답
            )
        )
        
        // When
        val result = submissionGradeUseCase.submitAndGrade(request)
        
        // Then
        assertEquals(testPieceId, result.pieceId)
        assertEquals(101L, result.studentId)
        assertEquals(testAssignmentId, result.assignmentId)
        assertEquals(5, result.totalProblems)
        assertEquals(5, result.submittedProblems)
        assertEquals(3, result.correctAnswers)
        assertEquals(0.6, result.correctnessRate, 0.001)
        assertEquals(5, result.gradingDetails.size)
        assertNotNull(result.submittedAt)
        
        // 채점 상세 결과 확인
        val gradingDetails = result.gradingDetails
        assertEquals("답1", gradingDetails[0].correctAnswer)
        assertEquals("답1", gradingDetails[0].submittedAnswer)
        assertTrue(gradingDetails[0].isCorrect)
        
        assertEquals("답2", gradingDetails[1].correctAnswer)
        assertEquals("틀린답", gradingDetails[1].submittedAnswer)
        assertTrue(!gradingDetails[1].isCorrect)
        
        // DB에 제출 데이터가 저장되었는지 확인
        val savedSubmissions = submissionJpaRepository.findByAssignmentId(testAssignmentId)
        assertEquals(5, savedSubmissions.size)
        
        // Assignment가 제출 완료 상태로 업데이트되었는지 확인
        val updatedAssignment = assignmentJpaRepository.findById(testAssignmentId).orElse(null)
        assertNotNull(updatedAssignment.submittedAt)
        assertEquals(0.6, updatedAssignment.correctnessRate!!, 0.001)
    }
    
    @Test
    fun `submitAndGrade - 정상 채점 테스트`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "답1"),
                AnswerSubmission(problemId = testProblemIds[1], answer = "답2"),
                AnswerSubmission(problemId = testProblemIds[2], answer = "답3")
            )
        )
        
        // When
        val result = submissionGradeUseCase.submitAndGrade(request)
        
        // Then
        assertEquals(3, result.correctAnswers)
        assertEquals(1.0, result.correctnessRate)
        assertTrue(result.gradingDetails.all { it.isCorrect })
        
        val updatedAssignment = assignmentJpaRepository.findById(testAssignmentId).orElse(null)
        assertEquals(1.0, updatedAssignment.correctnessRate!!)
    }
    
    @Test
    fun `submitAndGrade - 모든 문제 오답인 경우`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "틀린답1"),
                AnswerSubmission(problemId = testProblemIds[1], answer = "틀린답2"),
                AnswerSubmission(problemId = testProblemIds[2], answer = "틀린답3")
            )
        )
        
        // When
        val result = submissionGradeUseCase.submitAndGrade(request)
        
        // Then
        assertEquals(0, result.correctAnswers)
        assertEquals(0.0, result.correctnessRate)
        assertTrue(result.gradingDetails.none { it.isCorrect })
        
        val updatedAssignment = assignmentJpaRepository.findById(testAssignmentId).orElse(null)
        assertEquals(0.0, updatedAssignment.correctnessRate!!)
    }
    
    @Test
    fun `submitAndGrade - 이미 제출된 학습지에 재제출 시도하면 InvalidStateException 발생`() {
        // Given: 먼저 한 번 제출
        val firstRequest = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "답1")
            )
        )
        submissionGradeUseCase.submitAndGrade(firstRequest)
        
        // 두 번째 제출 시도
        val secondRequest = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[1], answer = "답2")
            )
        )
        
        // When & Then
        assertThrows<InvalidStateException> {
            submissionGradeUseCase.submitAndGrade(secondRequest)
        }
    }
    
    @Test
    fun `submitAndGrade - 출제받지 않은 학습지에 제출 시도하면 NotFoundException 발생`() {
        // Given: 다른 학생이 제출 시도
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 999L, // 출제받지 않은 학생
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "답1")
            )
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            submissionGradeUseCase.submitAndGrade(request)
        }
    }
    
    @Test
    fun `submitAndGrade - 존재하지 않는 문제에 답안 제출하면 BusinessRuleViolationException 발생`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = 999L, answer = "답") // 존재하지 않는 문제
            )
        )
        
        // When & Then
        assertThrows<BusinessRuleViolationException> {
            submissionGradeUseCase.submitAndGrade(request)
        }
    }
    
    @Test
    fun `submitAndGrade - 학습지에 속하지 않은 문제에 답안 제출하면 BusinessRuleViolationException 발생`() {
        // Given: 다른 문제 생성
        val otherProblem = ProblemJpaEntity(
            id = 0,
            answer = "다른답",
            unitCode = "UC999",
            level = 1,
            problemType = ProblemType.SUBJECTIVE
        )
        val savedOtherProblem = problemJpaRepository.save(otherProblem)
        
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = savedOtherProblem.id, answer = "다른답") // 학습지에 속하지 않은 문제
            )
        )
        
        // When & Then
        assertThrows<BusinessRuleViolationException> {
            submissionGradeUseCase.submitAndGrade(request)
        }
    }

    
    @Test
    fun `submitAndGrade - 제출부터 통계 업데이트까지 전체 플로우 테스트`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = testPieceId,
            studentId = 101L,
            answers = listOf(
                AnswerSubmission(problemId = testProblemIds[0], answer = "답1"),
                AnswerSubmission(problemId = testProblemIds[1], answer = "틀린답"),
                AnswerSubmission(problemId = testProblemIds[2], answer = "답3")
            )
        )
        
        // When
        val result = submissionGradeUseCase.submitAndGrade(request)
        
        // Then
        // 1. 제출 결과 확인
        assertEquals(3, result.submittedProblems)
        assertEquals(2, result.correctAnswers)
        assertEquals(0.6667, result.correctnessRate, 0.001) // 2/3
        
        // 2. Submission 저장 확인
        val submissions = submissionJpaRepository.findByAssignmentId(testAssignmentId)
        assertEquals(3, submissions.size)
        
        // 3. Assignment 업데이트 확인
        val updatedAssignment = assignmentJpaRepository.findById(testAssignmentId).orElse(null)
        assertNotNull(updatedAssignment.submittedAt)
        assertEquals(0.6667, updatedAssignment.correctnessRate!!, 0.001)
        
        // 4. 이벤트 발행 검증은 통합테스트에서는 직접 확인하기 어려우므로 생략
        // (EventListener가 별도의 트랜잭션에서 실행되거나 비동기일 수 있음)
    }
} 