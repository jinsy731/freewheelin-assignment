package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AnswerSubmission
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.model.Submission
import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.assignment.domain.port.SubmissionRepository
import com.freewheelin.pulley.assignment.domain.service.SubmissionValidator
import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.InvalidStateException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.common.infrastructure.logging.logger
import com.freewheelin.pulley.isEqualToUpTo
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class SubmissionGradeServiceTest {

    private val submissionRepository = mockk<SubmissionRepository>()
    private val assignmentRepository = mockk<AssignmentRepository>(relaxed = true)
    private val validator = mockk<SubmissionValidator>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>()
    
    private val sut = SubmissionGradeService(
        submissionRepository = submissionRepository,
        assignmentRepository = assignmentRepository,
        validator = validator,
        pieceProblemRepository = pieceProblemRepository,
        problemRepository = problemRepository,
        eventPublisher = eventPublisher
    )

    @Nested
    @DisplayName("submitAndGrade 성공 시나리오 테스트")
    inner class SubmitAndGradeSuccessTest {

        @Test
        @DisplayName("성공: 전체 채점 프로세스가 정상적으로 완료되고 올바른 결과를 반환한다")
        fun `should complete entire grading process successfully and return correct result`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(
                    AnswerSubmission(problemId = 1L, answer = "정답1"),
                    AnswerSubmission(problemId = 2L, answer = "오답2"),
                    AnswerSubmission(problemId = 3L, answer = "정답3")
                )
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now(),
                submittedAt = null
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(1L), ProblemId(2L), Position(2.0)),
                PieceProblem(3L, PieceId(1L), ProblemId(3L), Position(3.0))
            )
            
            val problems = mapOf(
                1L to Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE),
                2L to Problem(2L, "정답2", "UNIT02", 2, ProblemType.SELECTION),
                3L to Problem(3L, "정답3", "UNIT03", 3, ProblemType.SUBJECTIVE)
            )
            
            val updatedAssignment = assignment.copy(
                submittedAt = LocalDateTime.now(),
                correctnessRate = CorrectnessRate(2.0/3.0)
            )
            
            // Mock 설정
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } returns problems
            every { submissionRepository.saveAll(any<List<Submission>>()) } returns listOf()
            every { assignmentRepository.save(any()) } returns updatedAssignment
            every { eventPublisher.publishEvent(any<SubmissionGradedEvent>()) } just Runs

            // act
            val result = sut.submitAndGrade(request)

            // assert - 검증 메소드들이 순서대로 호출되는지 확인
            verifyOrder {
                validator.validateSubmissionDuplication(request)
                validator.validatePieceProblems(1L)
                validator.validateAndGetProblems(request, pieceProblems)
            }

            // 제출 저장 검증
            val submissionSlot = slot<List<Submission>>()
            verify { submissionRepository.saveAll(capture(submissionSlot)) }
            
            val savedSubmissions = submissionSlot.captured
            savedSubmissions.size shouldBe 3
            savedSubmissions[0].apply {
                assignmentId shouldBe 10L
                problemId shouldBe 1L
                answer shouldBe "정답1"
                isCorrect shouldBe true
            }
            savedSubmissions[1].apply {
                assignmentId shouldBe 10L
                problemId shouldBe 2L
                answer shouldBe "오답2"
                isCorrect shouldBe false
            }
            savedSubmissions[2].apply {
                assignmentId shouldBe 10L
                problemId shouldBe 3L
                answer shouldBe "정답3"
                isCorrect shouldBe true
            }
            
            // Assignment 업데이트 검증
            verify { assignmentRepository.save(any()) }
            
            // 이벤트 발행 검증
            val eventSlot = slot<SubmissionGradedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            
            val publishedEvent = eventSlot.captured
            publishedEvent.assignmentId shouldBe 10L
            publishedEvent.pieceId shouldBe 1L
            publishedEvent.studentId shouldBe 100L
            publishedEvent.submissionResults.size shouldBe 3
            publishedEvent.submissionResults[0].apply {
                problemId shouldBe 1L
                isCorrect shouldBe true
            }
            publishedEvent.submissionResults[1].apply {
                problemId shouldBe 2L
                isCorrect shouldBe false
            }
            publishedEvent.submissionResults[2].apply {
                problemId shouldBe 3L
                isCorrect shouldBe true
            }
            
            // 최종 결과 검증
            result.apply {
                pieceId shouldBe 1L
                studentId shouldBe 100L
                assignmentId shouldBe 10L
                totalProblems shouldBe 3
                submittedProblems shouldBe 3
                correctAnswers shouldBe 2
                correctnessRate.isEqualToUpTo((2.0/3.0), 3) shouldBe true
                scorePercentage.isEqualToUpTo((200.0/3.0), 3) shouldBe true
                incorrectAnswers shouldBe 1
                gradingDetails.size shouldBe 3
                gradingDetails[0].apply {
                    problemId shouldBe 1L
                    submittedAnswer shouldBe "정답1"
                    correctAnswer shouldBe "정답1"
                    isCorrect shouldBe true
                }
                gradingDetails[1].apply {
                    problemId shouldBe 2L
                    submittedAnswer shouldBe "오답2"
                    correctAnswer shouldBe "정답2"
                    isCorrect shouldBe false
                }
                gradingDetails[2].apply {
                    problemId shouldBe 3L
                    submittedAnswer shouldBe "정답3"
                    correctAnswer shouldBe "정답3"
                    isCorrect shouldBe true
                }
                submittedAt shouldContain LocalDateTime.now().toLocalDate().toString()
            }
        }

        @Test
        @DisplayName("성공: 모든 답안이 정답일 때 100% 정답률을 반환한다")
        fun `should return 100 percent correctness rate when all answers are correct`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(
                    AnswerSubmission(problemId = 1L, answer = "정답1"),
                    AnswerSubmission(problemId = 2L, answer = "정답2")
                )
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(1L), ProblemId(2L), Position(2.0))
            )
            
            val problems = mapOf(
                1L to Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE),
                2L to Problem(2L, "정답2", "UNIT02", 2, ProblemType.SELECTION)
            )
            
            val updatedAssignment = assignment.copy(
                submittedAt = LocalDateTime.now(),
                correctnessRate = CorrectnessRate(1.0)
            )
            
            // Mock 설정
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } returns problems
            every { submissionRepository.saveAll(any<List<Submission>>()) } returns listOf()
            every { assignmentRepository.save(updatedAssignment) } returns updatedAssignment
            every { eventPublisher.publishEvent(any<SubmissionGradedEvent>()) } just Runs

            // act
            val result = sut.submitAndGrade(request)

            // assert
            result.correctnessRate shouldBe 1.0
            result.scorePercentage shouldBe 100.0
            result.correctAnswers shouldBe 2
            result.incorrectAnswers shouldBe 0
        }

        @Test
        @DisplayName("성공: 모든 답안이 오답일 때 0% 정답률을 반환한다")
        fun `should return 0 percent correctness rate when all answers are incorrect`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(
                    AnswerSubmission(problemId = 1L, answer = "오답1"),
                    AnswerSubmission(problemId = 2L, answer = "오답2")
                )
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0)),
                PieceProblem(2L, PieceId(1L), ProblemId(2L), Position(2.0))
            )
            
            val problems = mapOf(
                1L to Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE),
                2L to Problem(2L, "정답2", "UNIT02", 2, ProblemType.SELECTION)
            )
            
            val updatedAssignment = assignment.copy(
                submittedAt = LocalDateTime.now(),
                correctnessRate = CorrectnessRate(0.0)
            )
            
            // Mock 설정
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } returns problems
            every { submissionRepository.saveAll(any<List<Submission>>()) } returns listOf()
            every { assignmentRepository.save(updatedAssignment) } returns updatedAssignment
            every { eventPublisher.publishEvent(any<SubmissionGradedEvent>()) } just Runs

            logger.info { "mocking is done. start to run test case." }
            // act
            val result = sut.submitAndGrade(request)

            // assert
            result.correctnessRate shouldBe 0.0
            result.scorePercentage shouldBe 0.0
            result.correctAnswers shouldBe 0
            result.incorrectAnswers shouldBe 2
        }
    }

    @Nested
    @DisplayName("submitAndGrade 실패 시나리오 테스트")
    inner class SubmitAndGradeFailureTest {

        @Test
        @DisplayName("실패: 중복 제출 검증 실패 시 예외가 전파되고 이후 로직이 실행되지 않는다")
        fun `should propagate exception when submission duplication validation fails and not execute subsequent logic`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val exception = InvalidStateException(
                ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED,

            )
            
            every { validator.validateSubmissionDuplication(request) } throws exception

            // act & assert
            shouldThrow<InvalidStateException> {
                sut.submitAndGrade(request)
            }
            
            // 이후 로직이 전혀 실행되지 않았는지 확인
            verify { validator.validateSubmissionDuplication(request) }
            verify(exactly = 0) { validator.validatePieceProblems(any()) }
            verify(exactly = 0) { validator.validateAndGetProblems(any(), any()) }
            verify(exactly = 0) { submissionRepository.saveAll(any<List<Submission>>()) }
            verify(exactly = 0) { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("실패: 학습지 문제 검증 실패 시 예외가 전파되고 이후 로직이 실행되지 않는다")
        fun `should propagate exception when piece problems validation fails and not execute subsequent logic`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val exception = NotFoundException(ErrorCode.PIECE_NO_PROBLEMS, "학습지에 문제가 없습니다")
            
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } throws exception

            // act & assert
            shouldThrow<NotFoundException> {
                sut.submitAndGrade(request)
            }
            
            // 검증된 단계까지만 실행되고 이후는 실행되지 않았는지 확인
            verify { validator.validateSubmissionDuplication(request) }
            verify { validator.validatePieceProblems(1L) }
            verify(exactly = 0) { validator.validateAndGetProblems(any(), any()) }
            verify(exactly = 0) { submissionRepository.saveAll(any<List<Submission>>()) }
            verify(exactly = 0) { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("실패: 문제 검증 실패 시 예외가 전파되고 이후 로직이 실행되지 않는다")
        fun `should propagate exception when problems validation fails and not execute subsequent logic`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 99L, answer = "답안99"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0))
            )
            
            val exception = NotFoundException(ErrorCode.SUBMISSION_INVALID_PROBLEMS, "유효하지 않은 문제")
            
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } throws exception

            // act & assert
            shouldThrow<NotFoundException> {
                sut.submitAndGrade(request)
            }
            
            // 검증된 단계까지만 실행되고 이후는 실행되지 않았는지 확인
            verify { validator.validateSubmissionDuplication(request) }
            verify { validator.validatePieceProblems(1L) }
            verify { validator.validateAndGetProblems(request, pieceProblems) }
            verify(exactly = 0) { submissionRepository.saveAll(any<List<Submission>>()) }
            verify(exactly = 0) { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("실패: 채점 중 문제 ID 조회 실패 시 NotFoundException이 발생한다")
        fun `should throw NotFoundException when problem ID lookup fails during grading`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 99L, answer = "답안99"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0))
            )

            val exception = NotFoundException(
                errorCode = ErrorCode.PROBLEM_NOT_FOUND,
                resourceId = 99L
            )

            
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } throws exception

            // act & assert
            shouldThrow<NotFoundException> {
                sut.submitAndGrade(request)
            }
            
            exception.errorCode shouldBe ErrorCode.PROBLEM_NOT_FOUND

            // DB 저장이나 이벤트 발행은 실행되지 않았는지 확인
            verify(exactly = 0) { submissionRepository.saveAll(any<List<Submission>>()) }
            verify(exactly = 0) { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("실패: 제출 저장 실패 시 예외가 전파된다")
        fun `should propagate exception when submission save fails`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0))
            )
            
            val problems = mapOf(
                1L to Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE)
            )
            
            val saveException = RuntimeException("DB 저장 실패")
            
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } returns problems
            every { submissionRepository.saveAll(any<List<Submission>>()) } throws saveException

            // act & assert
            val exception = shouldThrow<RuntimeException> {
                sut.submitAndGrade(request)
            }
            
            exception.message shouldBe "DB 저장 실패"
            
            // 제출 저장까지는 시도되었지만 이후 로직은 실행되지 않았는지 확인
            verify { submissionRepository.saveAll(any<List<Submission>>()) }
            verify(exactly = 0) { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("실패: Assignment 저장 실패 시 예외가 전파된다")
        fun `should propagate exception when assignment save fails`() {
            // arrange
            val request = SubmissionGradeRequest(
                pieceId = 1L,
                studentId = 100L,
                answers = listOf(AnswerSubmission(problemId = 1L, answer = "답안1"))
            )
            
            val assignment = Assignment(
                id = AssignmentId(10L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now()
            )
            
            val pieceProblems = listOf(
                PieceProblem(1L, PieceId(1L), ProblemId(1L), Position(1.0))
            )
            
            val problems = mapOf(
                1L to Problem(1L, "정답1", "UNIT01", 1, ProblemType.SUBJECTIVE)
            )
            
            val updatedAssignment = assignment.copy(
                submittedAt = LocalDateTime.now(),
                correctnessRate = CorrectnessRate(1.0)
            )
            
            val saveException = RuntimeException("Assignment 저장 실패")
            
            every { validator.validateSubmissionDuplication(request) } returns assignment
            every { validator.validatePieceProblems(1L) } returns pieceProblems
            every { validator.validateAndGetProblems(request, pieceProblems) } returns problems
            every { submissionRepository.saveAll(any<List<Submission>>()) } returns listOf()
            every { assignmentRepository.save(any()) } throws saveException
            every { eventPublisher.publishEvent(any<SubmissionGradedEvent>()) } just Runs

            // act & assert
            val exception = shouldThrow<RuntimeException> {
                sut.submitAndGrade(request)
            }
            
            exception.message shouldBe "Assignment 저장 실패"
            
            // Assignment 저장까지는 시도되었지만 이벤트 발행은 실행되지 않았는지 확인
            verify { submissionRepository.saveAll(any<List<Submission>>()) }
            verify { assignmentRepository.save(any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }
    }
}