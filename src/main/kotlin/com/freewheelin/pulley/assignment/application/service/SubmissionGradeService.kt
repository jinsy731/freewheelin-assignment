package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.ProblemGradingResult
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeResult
import com.freewheelin.pulley.assignment.application.port.SubmissionGradeUseCase
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.model.Submission
import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.assignment.domain.model.SubmissionResult
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.assignment.domain.port.SubmissionRepository
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.ProblemCount
import com.freewheelin.pulley.common.exception.BusinessRuleViolationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.InvalidStateException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.collections.associateBy
import kotlin.collections.count
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.minus
import kotlin.collections.toList
import kotlin.collections.toSet

private val logger = KotlinLogging.logger {}

/**
 * 답안 제출 및 채점 Application Service
 * 
 * 학생의 답안 제출을 받아 자동으로 채점하고,
 * 정답률을 계산하여 Assignment를 업데이트하는 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional
class SubmissionGradeService(
    private val submissionRepository: SubmissionRepository,
    private val assignmentRepository: AssignmentRepository,
    private val pieceProblemRepository: PieceProblemRepository,
    private val problemRepository: ProblemRepository,
    private val eventPublisher: ApplicationEventPublisher
) : SubmissionGradeUseCase {
    
    override fun submitAndGrade(request: SubmissionGradeRequest): SubmissionGradeResult {
        logger.info { 
            "채점 시작 - pieceId: ${request.pieceId}, studentId: ${request.studentId}, " +
            "submitCount: ${request.answers.size}" 
        }
        
        try {
            // 1. 기본 검증
            val assignment = validateSubmissionRequest(request)
            val pieceProblems = validatePieceProblems(request.pieceId)
            
            // 2. 제출 답안 검증 및 채점
            val problems = validateAndGetProblems(request, pieceProblems)
            val submissions = gradeSubmissions(request, assignment, problems)
            
            val correctCount = submissions.count { it.isCorrect }
            val correctnessRate = CorrectnessRate.calculate(submissions).value
            
            logger.info { 
                "채점 완료 - pieceId: ${request.pieceId}, studentId: ${request.studentId}, " +
                "총문제: ${submissions.size}, 정답: $correctCount, 정답률: ${correctnessRate}%" 
            }
            
            // 3. 제출 처리 및 저장
            submissionRepository.saveAll(submissions)
            updateAssignmentWithSubmission(assignment, submissions, pieceProblems.size)
            
            // 4. 이벤트 발행 및 결과 반환
            publishSubmissionEvent(assignment, request, submissions)
            return calculateSubmissionResult(request.pieceId, request.studentId, assignment.id.value, problems, submissions, pieceProblems.size)
            
        } catch (e: Exception) {
            logger.error(e) { 
                "채점 실패 - pieceId: ${request.pieceId}, studentId: ${request.studentId}, error: ${e.message}" 
            }
            throw e
        }
    }
    
    /**
     * 제출 요청의 기본 검증 (권한, 중복 제출)
     */
    private fun validateSubmissionRequest(request: SubmissionGradeRequest): Assignment {
        logger.debug { "제출 요청 검증 시작 - pieceId: ${request.pieceId}, studentId: ${request.studentId}" }
        
        val assignment = assignmentRepository.getByPieceIdAndStudentId(
            request.pieceId,
            request.studentId
        )
        
        if (assignment.isSubmitted()) {
            logger.warn { 
                "중복 제출 시도 - pieceId: ${request.pieceId}, studentId: ${request.studentId}, " +
                "assignmentId: ${assignment.id.value}" 
            }
            throw InvalidStateException(
                ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED,
                currentState = "이미 제출됨",
                requestedAction = "답안 제출",
                expectedState = "미제출 상태"
            )
        }
        
        logger.debug { "제출 요청 검증 완료 - assignmentId: ${assignment.id.value}" }
        return assignment
    }
    
    /**
     * 학습지 문제 검증
     */
    private fun validatePieceProblems(pieceId: Long): List<PieceProblem> {
        val pieceProblems = pieceProblemRepository.findByPieceIdOrderByPosition(pieceId)
        if (pieceProblems.isEmpty()) {
            logger.warn { "문제 없는 학습지 - pieceId: $pieceId" }
            throw BusinessRuleViolationException(
                ErrorCode.PIECE_NO_PROBLEMS,
                "학습지에 문제가 없습니다.",
                "학습지 ID: $pieceId"
            )
        }
        
        logger.debug { "학습지 문제 검증 완료 - pieceId: $pieceId, problemCount: ${pieceProblems.size}" }
        return pieceProblems
    }
    
    /**
     * 제출된 문제 검증 및 문제 정보 조회
     */
    private fun validateAndGetProblems(
        request: SubmissionGradeRequest,
        pieceProblems: List<PieceProblem>
    ): Map<Long, Problem> {
        val validProblemIds = pieceProblems.map { it.problemId.value }.toSet()
        val submittedProblemIds = request.answers.map { it.problemId }.toSet()
        
        val invalidProblemIds = submittedProblemIds - validProblemIds
        if (invalidProblemIds.isNotEmpty()) {
            logger.warn { 
                "잘못된 문제 제출 - pieceId: ${request.pieceId}, invalidProblemIds: $invalidProblemIds" 
            }
            throw BusinessRuleViolationException(
                ErrorCode.SUBMISSION_INVALID_PROBLEMS,
                message = "해당 학습지에 포함되지 않은 문제가 있습니다: $invalidProblemIds"
            )
        }
        
        val problems = problemRepository.findByIds(submittedProblemIds.toList())
        logger.debug { "문제 정보 조회 완료 - problemCount: ${problems.size}" }
        return problems.associateBy { it.id }
    }
    
    /**
     * 답안 채점 처리
     */
    private fun gradeSubmissions(
        request: SubmissionGradeRequest,
        assignment: Assignment,
        problems: Map<Long, Problem>
    ): List<Submission> {
        logger.debug { "답안 채점 시작 - answerCount: ${request.answers.size}" }
        
        return request.answers.map { answerSubmission ->
            val problem = problems[answerSubmission.problemId]
                ?: throw NotFoundException(
                    ErrorCode.PROBLEM_NOT_FOUND,
                    answerSubmission.problemId
                )
            
            val isCorrect = problem.isCorrectAnswer(answerSubmission.answer)
            
            logger.debug { 
                "개별 문제 채점 - problemId: ${answerSubmission.problemId}, " +
                "answer: '${answerSubmission.answer}', correct: $isCorrect" 
            }

            Submission(
                id = 0L,
                assignmentId = assignment.id.value,
                problemId = answerSubmission.problemId,
                answer = answerSubmission.answer,
                isCorrect = isCorrect
            )
        }
    }
    
    /**
     * Assignment 제출 완료 처리
     */
    private fun updateAssignmentWithSubmission(
        assignment: Assignment,
        submissions: List<Submission>,
        totalProblemsCount: Int
    ) {
        logger.debug { 
            "과제 제출 상태 업데이트 - assignmentId: ${assignment.id.value}, " +
            "submissionCount: ${submissions.size}, totalProblems: $totalProblemsCount" 
        }
        
        val totalProblems = ProblemCount(totalProblemsCount)
        val updatedAssignment = assignment.submit(submissions, totalProblems)
        assignmentRepository.save(updatedAssignment)
        
        logger.debug { "과제 제출 완료 처리됨 - assignmentId: ${assignment.id.value}" }
    }
    
    /**
     * 통계 업데이트 이벤트 발행
     */
    private fun publishSubmissionEvent(
        assignment: Assignment,
        request: SubmissionGradeRequest,
        submissions: List<Submission>
    ) {
        val submissionResults = submissions.map { submission ->
            SubmissionResult(
                problemId = submission.problemId,
                isCorrect = submission.isCorrect
            )
        }
        
        val event = SubmissionGradedEvent(
            assignmentId = assignment.id.value,
            pieceId = request.pieceId,
            studentId = request.studentId,
            submissionResults = submissionResults
        )
        
        logger.info { 
            "통계 업데이트 이벤트 발행 - assignmentId: ${assignment.id.value}, " +
            "pieceId: ${request.pieceId}, studentId: ${request.studentId}" 
        }
        
        eventPublisher.publishEvent(event)
    }
    
    /**
     * 제출 결과 계산
     */
    private fun calculateSubmissionResult(
        pieceId: Long,
        studentId: Long,
        assignmentId: Long,
        problems: Map<Long, Problem>,
        submissions: List<Submission>,
        totalProblemsInPiece: Int
    ): SubmissionGradeResult {
        // 정답률 계산 (제출한 문제들 기준)
        val correctnessRate = CorrectnessRate.calculate(submissions).value
        val totalCorrect = submissions.count { it.isCorrect }

        // 채점 상세 결과 생성
        val existingProblemIds = submissions.map { it.problemId }.toSet()
        val gradingDetails = submissions.map { submission ->
            val problem = problems[submission.problemId]!!
            val wasUpdated = existingProblemIds.contains(submission.problemId)
            ProblemGradingResult(
                problemId = submission.problemId,
                submittedAnswer = submission.answer,
                correctAnswer = problem.answer,
                isCorrect = submission.isCorrect,
            )
        }
        
        return SubmissionGradeResult(
            pieceId = pieceId,
            studentId = studentId,
            assignmentId = assignmentId,
            totalProblems = totalProblemsInPiece,
            submittedProblems = submissions.size,
            correctAnswers = totalCorrect,
            correctnessRate = correctnessRate,
            gradingDetails = gradingDetails,
            submittedAt = LocalDateTime.now().toString()
        )
    }
} 