package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId


/**
 * 학습지-문제 매핑 도메인 모델
 * 
 * 학습지 내 문제의 순서 관리를 담당합니다.
 */
data class PieceProblem(
    val id: Long, // 매핑 ID
    val pieceId: PieceId,
    val problemId: ProblemId,
    val position: Position
) {
    
    /**
     * 위치 업데이트
     */
    fun updatePosition(newPosition: Position): PieceProblem {
        return copy(position = newPosition)
    }
    
    /**
     * 두 문제 사이로 이동 (위치 계산 + 업데이트를 한 번에 처리)
     * 
     * @param prevProblem 이전 문제 (null이면 맨 앞으로 이동)
     * @param nextProblem 다음 문제 (null이면 맨 뒤로 이동)
     * @return 새로운 위치로 업데이트된 PieceProblem
     */
    fun moveTo(prevProblem: PieceProblem?, nextProblem: PieceProblem?): PieceProblem {
        val newPosition = Position.between(prevProblem?.position, nextProblem?.position)
        return updatePosition(newPosition)
    }
    
    companion object {
        /**
         * 새로운 PieceProblem 생성
         */
        fun create(
            pieceId: PieceId,
            problemId: ProblemId,
            position: Position
        ): PieceProblem {
            return PieceProblem(
                id = 0L, // JPA가 자동 생성할 임시 ID
                pieceId = pieceId,
                problemId = problemId,
                position = position
            )
        }
    }
} 