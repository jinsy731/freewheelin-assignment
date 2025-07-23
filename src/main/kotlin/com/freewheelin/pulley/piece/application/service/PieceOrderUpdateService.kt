package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceOrderUpdateUseCase
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateResult
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.associateBy
import kotlin.let

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
        // 1. 권한 검증
        val piece = pieceRepository.getById(command.pieceId)
        piece.validateOwnership(command.teacherId)
        
        // 2. 필요한 모든 문제들을 한 번에 조회 (성능 최적화)
        val requiredProblemIds = listOfNotNull(
            command.problemId,
            command.prevProblemId,
            command.nextProblemId
        )
        
        val foundProblems = pieceProblemRepository.findByPieceIdAndProblemIdIn(
            command.pieceId, 
            requiredProblemIds
        )
        
        // 3. 조회된 문제들을 Map으로 변환 (빠른 접근)
        val problemMap = foundProblems.associateBy { it.problemId }
        
        // 4. 이동할 문제 확인
        val problemToMove = problemMap[ProblemId(command.problemId)]
            ?: throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "해당 문제는 이 학습지에 속해있지 않습니다. (문제 ID: ${command.problemId})"
            )
        
        // 5. 이전/다음 문제 확인 (존재하는 경우만)
        val prevProblem = command.prevProblemId?.let { prevId ->
            problemMap[ProblemId(prevId)] ?: throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "이전 문제를 찾을 수 없습니다: $prevId"
            )
        }
        
        val nextProblem = command.nextProblemId?.let { nextId ->
            problemMap[ProblemId(nextId)] ?: throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "다음 문제를 찾을 수 없습니다: $nextId"
            )
        }
        
        // 6. 문제 위치 이동 (위치 계산 + 업데이트를 한 번에 처리)
        val updatedProblem = problemToMove.moveTo(prevProblem, nextProblem)
        pieceProblemRepository.save(updatedProblem)
        
        // 7. 결과 반환
        return ProblemOrderUpdateResult(
            pieceId = command.pieceId,
            problemId = command.problemId,
            previousPosition = problemToMove.position.value,
            newPosition = updatedProblem.position.value,
            success = true
        )
    }
} 