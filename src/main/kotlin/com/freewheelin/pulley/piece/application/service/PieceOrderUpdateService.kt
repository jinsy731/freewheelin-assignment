package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceOrderUpdateUseCase
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateResult
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.model.PieceProblemOrder
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 학습지 문제 순서 변경 Application Service
 */
@Service
@Transactional
class PieceOrderUpdateService(
    private val pieceRepository: PieceRepository,
    private val pieceProblemRepository: PieceProblemRepository
) : PieceOrderUpdateUseCase {

    /**
     * 문제 순서 업데이트
     * 1.Piece 소유권 검증
     * 2. 변경할 문제, 앞/뒤 문제 조회 (+ 앞/뒤 문제 사이에 있는 문제 조회 for 연속성 검증)
     * 3.
     */
    override fun updateProblemOrder(command: ProblemOrderUpdateCommand): ProblemOrderUpdateResult {
        validatePieceOwnership(command)
        
        val requiredProblems = fetchRequiredProblems(command)
        val pieceProblemOrder = PieceProblemOrder.of(requiredProblems)
        
        val originalPosition = getOriginalPosition(pieceProblemOrder, command.pieceProblemId)
        val newPosition = updateProblemPosition(pieceProblemOrder, command, originalPosition)
        
        return ProblemOrderUpdateResult.success(
            pieceId = command.pieceId,
            pieceProblemId = command.pieceProblemId,
            previousPosition = originalPosition.value,
            newPosition = newPosition.value
        )
    }

    private fun validatePieceOwnership(command: ProblemOrderUpdateCommand) {
        val piece = pieceRepository.getById(command.pieceId)
        piece.validateOwnership(command.teacherId)
    }

    private fun fetchRequiredProblems(command: ProblemOrderUpdateCommand): List<PieceProblem> {
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

        return requiredProblems
    }

    private fun getOriginalPosition(pieceProblemOrder: PieceProblemOrder, pieceProblemId: Long): Position {
        return pieceProblemOrder
            .getByPieceProblemId(pieceProblemId)
            .position
    }

    private fun updateProblemPosition(
        pieceProblemOrder: PieceProblemOrder,
        command: ProblemOrderUpdateCommand,
        originalPosition: Position
    ): Position {
        val updatedProblem = pieceProblemOrder.moveOrderTo(
            pieceProblemId = command.pieceProblemId,
            prevPieceProblemId = command.prevPieceProblemId,
            nextPieceProblemId = command.nextPieceProblemId
        )

        return updatedProblem?.let{
            pieceProblemRepository.save(updatedProblem)
            updatedProblem.position
        } ?: originalPosition
    }


}