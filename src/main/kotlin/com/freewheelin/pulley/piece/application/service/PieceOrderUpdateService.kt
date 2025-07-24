package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceOrderUpdateUseCase
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateResult
import com.freewheelin.pulley.piece.domain.model.PieceProblems
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 학습지 문제 순서 변경 Application Service
 * 
 * "사이" 위치 지정 방식으로 문제 순서를 변경합니다.
 * 성능 최적화: 필요한 문제들을 한 번에 조회하고 오직 1개 문제만 업데이트합니다.
 */
@Service
@Transactional
class PieceOrderUpdateService(
    private val pieceRepository: PieceRepository,
    private val pieceProblemRepository: PieceProblemRepository
) : PieceOrderUpdateUseCase {

    override fun updateProblemOrder(command: ProblemOrderUpdateCommand): ProblemOrderUpdateResult {
        val piece = pieceRepository.getById(command.pieceId)
        piece.validateOwnership(command.teacherId)

        val requiredProblems = pieceProblemRepository.findPieceProblemsForOrderUpdate(
            pieceId = command.pieceId,
            pieceProblemId = command.pieceProblemId,
            prevPieceProblemId = command.prevPieceProblemId,
            nextPieceProblemId = command.nextPieceProblemId
        )

        if (requiredProblems.isEmpty()) {
            throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "학습지에 문제가 없거나 지정된 PieceProblem을 찾을 수 없습니다."
            )
        }

        val pieceProblems = PieceProblems.of(requiredProblems)
        val problemToMove = pieceProblems.getByPieceProblemId(command.pieceProblemId) // 존재 확인
        val originalPosition = problemToMove.position.value

        val updatedProblem = pieceProblems.moveOrderTo(
            pieceProblemId = command.pieceProblemId,
            prevPieceProblemId = command.prevPieceProblemId,
            nextPieceProblemId = command.nextPieceProblemId
        )

        // 6. 변경사항이 있는 경우에만 저장
        val newPosition = if (updatedProblem != null) {
            pieceProblemRepository.save(updatedProblem)
            updatedProblem.position.value
        } else {
            originalPosition // 변경 불필요
        }

        // 7. 결과 반환
        return ProblemOrderUpdateResult(
            pieceId = command.pieceId,
            pieceProblemId = command.pieceProblemId,
            previousPosition = originalPosition,
            newPosition = newPosition,
            success = true
        )
    }
}