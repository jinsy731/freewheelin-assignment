package com.freewheelin.pulley.assignment.domain.service

import com.freewheelin.pulley.assignment.application.port.AnswerSubmission
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.common.exception.BusinessRuleViolationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.InvalidStateException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SubmissionValidatorTest {

    private val assignmentRepository = mockk<AssignmentRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    
    private val sut = SubmissionValidator(
        assignmentRepository = assignmentRepository,
        pieceProblemRepository = pieceProblemRepository,
        problemRepository = problemRepository
    )

    @Nested
    @DisplayName("validateSubmissionDuplication 테스트")
    inner class ValidateSubmissionDuplicationTest {

        @Test
        @DisplayName("성공: 미제출 상태의 Assignment를 정상적으로 반환한다")
        fun `should return assignment when not submitted yet`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(1L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now(),
                submittedAt = null // 미제출 상태
            )
            
            every { assignmentRepository.getByPieceIdAndStudentId(1L, 100L) } returns assignment

            // act
            val result = sut.validateSubmissionDuplication(request)

            // assert
            result shouldBe assignment
            verify { assignmentRepository.getByPieceIdAndStudentId(1L, 100L) }
        }

        @Test
        @DisplayName("실패: 이미 제출된 상태일 때 InvalidStateException을 발생시킨다")
        fun `should throw InvalidStateException when already submitted`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val submittedAssignment = Assignment(
                id = AssignmentId(1L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now(),
                submittedAt = LocalDateTime.now() // 이미 제출됨
            )
            
            every { assignmentRepository.getByPieceIdAndStudentId(1L, 100L) } returns submittedAssignment

            // act & assert
            val exception = shouldThrow<InvalidStateException> {
                sut.validateSubmissionDuplication(request)
            }
            
            exception.errorCode shouldBe ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED
            exception.context["currentState"] shouldBe "이미 제출됨"
            exception.context["requestedAction"] shouldBe "답안 제출"
            exception.context["expectedState"] shouldBe "미제출 상태"
        }

        @Test
        @DisplayName("실패: Assignment가 존재하지 않을 때 예외가 전파된다")
        fun `should propagate exception when assignment not found`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            every { assignmentRepository.getByPieceIdAndStudentId(1L, 100L) } throws 
                NotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND, "Assignment not found")

            // act & assert
            shouldThrow<NotFoundException> {
                sut.validateSubmissionDuplication(request)
            }
        }
    }

    @Nested
    @DisplayName("validateAndGetProblems 테스트")
    inner class ValidateAndGetProblemsTest {

        @Test
        @DisplayName("성공: 모든 제출된 문제가 유효할 때 Problem 맵을 반환한다")
        fun `should return problem map when all submitted problems are valid`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(
                    AnswerSubmission(problemId = 1L, answer = "답안1"),
                    AnswerSubmission(problemId = 2L, answer = "답안2")
                )
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(1L), ProblemId(2L), Position(2.0)),
                PieceProblem(3L, PieceId(1L), ProblemId(3L), Position(3.0))
            )
            
            val problems = listOf(
                Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE),
                Problem(2L, "정답2", "UNIT02", 2, ProblemType.SELECTION)
            )
            
            every { problemRepository.findByIds(listOf(1L, 2L)) } returns problems

            // act
            val result = sut.validateAndGetProblems(request, pieceProblems)

            // assert
            result.size shouldBe 2
            result[1L] shouldBe problems[0]
            result[2L] shouldBe problems[1]
            verify { problemRepository.findByIds(listOf(1L, 2L)) }
        }

        @Test
        @DisplayName("실패: 학습지에 포함되지 않은 문제가 있을 때 BusinessRuleViolationException을 발생시킨다")
        fun `should throw BusinessRuleViolationException when invalid problems included`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(
                    AnswerSubmission(problemId = 1L, answer = "답안1"),
                    AnswerSubmission(problemId = 99L, answer = "답안99") // 유효하지 않은 문제
                )
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(1L), ProblemId(2L), Position(2.0))
            )

            // act & assert
            val exception = shouldThrow<BusinessRuleViolationException> {
                sut.validateAndGetProblems(request, pieceProblems)
            }
            
            exception.errorCode shouldBe ErrorCode.SUBMISSION_INVALID_PROBLEMS
            exception.message shouldBe "해당 학습지에 포함되지 않은 문제가 있습니다: [99]"
        }

        @Test
        @DisplayName("엣지 케이스: 제출 답안이 없을 때 빈 맵을 반환한다")
        fun `should return empty map when no answers submitted`() {
            // arrange
            // SubmissionGradeRequest의 init 블록에서 answers가 비어있으면 예외를 발생시키므로
            // 직접 answers 리스트를 빈 리스트로 만들어서 테스트
            val request = mockk<SubmissionGradeRequest>()
            every { request.answers } returns emptyList()
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0))
            )
            
            every { problemRepository.findByIds(emptyList()) } returns emptyList()

            // act
            val result = sut.validateAndGetProblems(request, pieceProblems)

            // assert
            result.shouldBeEmpty()
            verify { problemRepository.findByIds(emptyList()) }
        }
    }

    @Nested
    @DisplayName("validatePieceProblems 테스트")
    inner class ValidatePieceProblemsTest {

        @Test
        @DisplayName("성공: 학습지에 문제가 있을 때 PieceProblem 리스트를 반환한다")
        fun `should return piece problems when piece has problems`() {
            // arrange
            val pieceId = 1L
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(pieceId), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(pieceId), ProblemId(2L), Position(2.0)),
                PieceProblem(3L, PieceId(pieceId), ProblemId(3L), Position(3.0))
            )
            
            every { pieceProblemRepository.findByPieceIdOrderByPosition(pieceId) } returns pieceProblems

            // act
            val result = sut.validatePieceProblems(pieceId)

            // assert
            result shouldBe pieceProblems
            result.size shouldBe 3
            verify { pieceProblemRepository.findByPieceIdOrderByPosition(pieceId) }
        }

        @Test
        @DisplayName("실패: 학습지에 문제가 없을 때 BusinessRuleViolationException을 발생시킨다")
        fun `should throw BusinessRuleViolationException when piece has no problems`() {
            // arrange
            val pieceId = 1L
            
            every { pieceProblemRepository.findByPieceIdOrderByPosition(pieceId) } returns emptyList()

            // act & assert
            val exception = shouldThrow<BusinessRuleViolationException> {
                sut.validatePieceProblems(pieceId)
            }
            
            exception.errorCode shouldBe ErrorCode.PIECE_NO_PROBLEMS
            exception.message shouldBe "학습지에 문제가 없습니다. 규칙: 학습지에 문제가 없습니다. - 학습지 ID: $pieceId"
        }
    }
}