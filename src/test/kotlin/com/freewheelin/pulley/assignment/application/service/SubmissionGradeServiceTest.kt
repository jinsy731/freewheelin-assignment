package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AnswerSubmission
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.assignment.domain.port.SubmissionRepository
import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.freewheelin.pulley.common.exception.BusinessRuleViolationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import org.springframework.context.ApplicationEventPublisher

class SubmissionGradeServiceTest {
    
    private val assignmentRepository = mockk<AssignmentRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    private val submissionRepository = mockk<SubmissionRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val submissionGradeService = SubmissionGradeService(
        submissionRepository,
        assignmentRepository,
        pieceProblemRepository,
        problemRepository,
        eventPublisher
    )
    
    @Test
    fun `submitAndGrade - 정상 채점 테스트`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = 100L,
            studentId = 10L,
            answers = listOf(
                AnswerSubmission(1001L, "10"),
                AnswerSubmission(1002L, "20"),
                AnswerSubmission(1003L, "30")
            )
        )
        
        val assignment = Assignment(
            id = AssignmentId(1L),
            pieceId = PieceId(100L),
            studentId = StudentId(10L),
            assignedAt = LocalDateTime.now(),
        )
        val pieceProblems = listOf(
            PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0)),
            PieceProblem.create(PieceId(100L), ProblemId(1002L), Position(2.0)),
            PieceProblem.create(PieceId(100L), ProblemId(1003L), Position(3.0))
        )
        val problems = listOf(
            Problem(1001L, "10", "A", 1, ProblemType.SELECTION),
            Problem(1002L, "20", "A", 2, ProblemType.SELECTION),
            Problem(1003L, "30", "B", 1, ProblemType.SUBJECTIVE)
        )
        
        every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
        every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
        every { problemRepository.findByIds(listOf(1001L, 1002L, 1003L)) } returns problems
        every { submissionRepository.saveAll(any()) } returns emptyList()
        every { submissionRepository.countByAssignmentId(assignment.id.value) } returns 3L
        every { assignmentRepository.save(any()) } returns assignment
        
        // When
        val result = submissionGradeService.submitAndGrade(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals(10L, result.studentId)
        assertEquals(assignment.id.value, result.assignmentId)
        assertEquals(3, result.totalProblems)
        assertEquals(3, result.submittedProblems)
        assertEquals(3, result.correctAnswers)
        assertEquals(0, result.incorrectAnswers)
        assertEquals(1.0, result.correctnessRate)
        assertEquals(100.0, result.scorePercentage)
        assertEquals(3, result.gradingDetails.size)
        assertTrue(result.gradingDetails.all { it.isCorrect })
        verify { submissionRepository.saveAll(match { it.size == 3 }) }
        verify { assignmentRepository.save(any()) }
        verify { eventPublisher.publishEvent(any<SubmissionGradedEvent>()) }
    }
    
    @Test
    fun `submitAndGrade - 일부 정답 일부 오답 테스트`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = 100L,
            studentId = 10L,
            answers = listOf(
                AnswerSubmission(1001L, "10"),     // 정답
                AnswerSubmission(1002L, "999"),    // 오답
                AnswerSubmission(1003L, "30")      // 정답
            )
        )
        
        val assignment = Assignment(
            id = AssignmentId(1L),
            pieceId = PieceId(100L),
            studentId = StudentId(10L),
            assignedAt = LocalDateTime.now(),
        )
        val pieceProblems = listOf(
            PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0)),
            PieceProblem.create(PieceId(100L), ProblemId(1002L), Position(2.0)),
            PieceProblem.create(PieceId(100L), ProblemId(1003L), Position(3.0))
        )
        val problems = listOf(
            Problem(1001L, "10", "A", 1, ProblemType.SELECTION),
            Problem(1002L, "20", "A", 2, ProblemType.SELECTION),
            Problem(1003L, "30", "B", 1, ProblemType.SUBJECTIVE)
        )
        
        every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
        every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
        every { problemRepository.findByIds(listOf(1001L, 1002L, 1003L)) } returns problems
        every { submissionRepository.saveAll(any()) } returns emptyList()
        every { submissionRepository.countByAssignmentId(assignment.id.value) } returns 3L
        every { assignmentRepository.save(any()) } returns assignment
        
        // When
        val result = submissionGradeService.submitAndGrade(request)
        
        // Then
        assertEquals(2, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(2.0 / 3.0, result.correctnessRate, 0.001)
        assertEquals(66.67, result.scorePercentage, 0.01)
        
        // 채점 상세 결과 확인
        assertEquals(true, result.gradingDetails[0].isCorrect)   // 1001L: "10" 정답
        assertEquals(false, result.gradingDetails[1].isCorrect)  // 1002L: "999" 오답
        assertEquals(true, result.gradingDetails[2].isCorrect)   // 1003L: "30" 정답
    }

    
    @Test
    fun `submitAndGrade - 문제가 없는 학습지 제출시 예외 발생`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = 100L,
            studentId = 10L,
            answers = listOf(AnswerSubmission(1001L, "10"))
        )
        
        val assignment = Assignment.create(
            pieceId = PieceId(100L),
            studentId = StudentId(10L)
        )
        
        every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
        every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns emptyList()
        
        // When & Then
        val exception = assertThrows<BusinessRuleViolationException> {
            submissionGradeService.submitAndGrade(request)
        }
        
        assertEquals(ErrorCode.PIECE_NO_PROBLEMS, exception.errorCode)
        assertTrue(exception.message!!.contains("학습지에 문제가 없습니다"))
    }
    
    @Test
    fun `submitAndGrade - 학습지에 포함되지 않은 문제 제출시 예외 발생`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = 100L,
            studentId = 10L,
            answers = listOf(
                AnswerSubmission(1001L, "10"),   // 학습지에 있는 문제
                AnswerSubmission(9999L, "답")    // 학습지에 없는 문제
            )
        )
        
        val assignment = Assignment.create(
            pieceId = PieceId(100L),
            studentId = StudentId(10L)
        )
        val pieceProblems = listOf(
            PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0))  // 1001L만 있음
        )
        
        every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
        every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
        
        // When & Then
        val exception = assertThrows<BusinessRuleViolationException> {
            submissionGradeService.submitAndGrade(request)
        }
        
        assertEquals(ErrorCode.SUBMISSION_INVALID_PROBLEMS, exception.errorCode)
        assertTrue(exception.message!!.contains("해당 학습지에 포함되지 않은 문제가 있습니다"))
        assertTrue(exception.message!!.contains("9999"))
    }

    @Test
    fun `submitAndGrade - 존재하지 않는 문제 제출시 예외 발생`() {
        // Given
        val request = SubmissionGradeRequest(
            pieceId = 100L,
            studentId = 10L,
            answers = listOf(AnswerSubmission(1001L, "10"))
        )
        
        val assignment = Assignment.create(
            pieceId = PieceId(100L),
            studentId = StudentId(10L)
        )
        val pieceProblems = listOf(
            PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0))
        )
        
        every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
        every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
        every { problemRepository.findByIds(listOf(1001L)) } returns emptyList()  // 문제를 찾을 수 없음
        
        // When & Then
        val exception = assertThrows<NotFoundException> {
            submissionGradeService.submitAndGrade(request)
        }
        
        assertEquals(ErrorCode.PROBLEM_NOT_FOUND, exception.errorCode)
        assertTrue(exception.message!!.contains("1001"))
    }
} 