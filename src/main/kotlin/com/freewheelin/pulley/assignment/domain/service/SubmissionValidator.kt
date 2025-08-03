package com.freewheelin.pulley.assignment.domain.service

import com.freewheelin.pulley.assignment.application.port.SubmissionGradeRequest
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.exception.BusinessRuleViolationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.InvalidStateException
import com.freewheelin.pulley.common.infrastructure.logging.logger
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import org.springframework.stereotype.Service

@Service
class SubmissionValidator(
    private val assignmentRepository: AssignmentRepository,
    private val pieceProblemRepository: PieceProblemRepository,
    private val problemRepository: ProblemRepository,
) {

    /**
     * 제출 요청의 기본 검증 (권한, 중복 제출)
     * @throws InvalidStateException
     */
    fun validateSubmissionDuplication(request: SubmissionGradeRequest): Assignment {
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
     * 제출된 문제 검증 및 문제 정보 조회
     * @throws BusinessRuleViolationException
     */
    fun validateAndGetProblems(
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
     * 학습지 문제 검증
     * @throws BusinessRuleViolationException
     */
    fun validatePieceProblems(pieceId: Long): List<PieceProblem> {
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
}