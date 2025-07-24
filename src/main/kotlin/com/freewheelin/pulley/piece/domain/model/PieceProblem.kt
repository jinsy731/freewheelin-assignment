package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException


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
     * @throws ValidationException 자기 자신을 prev/next로 지정한 경우
     */
    fun moveTo(prevProblem: PieceProblem?, nextProblem: PieceProblem?): PieceProblem {
        // 자기 자신을 이전/다음 문제로 지정하는 것 방지
        validateNotSelf(prevProblem, "이전")
        validateNotSelf(nextProblem, "다음")

        // 새로운 위치 계산
        val newPosition = Position.between(prevProblem?.position, nextProblem?.position)

        return updatePosition(newPosition)
    }

    /**
     * 자기 자신과의 비교 방지 검증
     */
    private fun validateNotSelf(otherProblem: PieceProblem?, positionName: String) {
        if (otherProblem != null && otherProblem.problemId == problemId) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "self_reference",
                problemId.value,
                "자기 자신을 ${positionName} 문제로 지정할 수 없습니다. (문제 ID: ${problemId.value})"
            )
        }
    }

    /**
     * 같은 학습지에 속한 문제인지 확인
     */
    fun belongsToSamePiece(other: PieceProblem): Boolean {
        return pieceId == other.pieceId
    }

    /**
     * 현재 문제가 다른 문제보다 앞에 있는지 확인
     */
    fun isBefore(other: PieceProblem): Boolean {
        validateSamePiece(other)
        return position.isBefore(other.position)
    }

    /**
     * 현재 문제가 다른 문제보다 뒤에 있는지 확인
     */
    fun isAfter(other: PieceProblem): Boolean {
        validateSamePiece(other)
        return position.isAfter(other.position)
    }

    /**
     * 같은 학습지 문제인지 검증
     */
    private fun validateSamePiece(other: PieceProblem) {
        if (!belongsToSamePiece(other)) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "different_piece",
                "${pieceId.value} vs ${other.pieceId.value}",
                "다른 학습지에 속한 문제와는 위치 비교를 할 수 없습니다."
            )
        }
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