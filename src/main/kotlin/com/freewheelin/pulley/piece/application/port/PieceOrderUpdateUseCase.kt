package com.freewheelin.pulley.piece.application.port

/**
 * 학습지 문제 순서 수정 Use Case 포트 인터페이스
 *
 * Presentation 레이어에서 호출하는 문제 순서 수정 관련 기능을 정의합니다.
 */
interface PieceOrderUpdateUseCase {

    /**
     * 문제 순서 수정
     *
     * @param command 문제 순서 수정 명령 정보
     */
    fun updateProblemOrder(command: ProblemOrderUpdateCommand): ProblemOrderUpdateResult
}

/**
 * 문제 순서 수정 명령 DTO (PieceProblem ID 기반)
 *
 * "사이" 위치 지정 방식으로 문제 순서를 변경합니다.
 * prevPieceProblemId와 nextPieceProblemId 사이로 pieceProblemId를 이동시킵니다.
 */
data class ProblemOrderUpdateCommand(
    val pieceId: Long,
    val teacherId: Long,  // 권한 검증용
    val pieceProblemId: Long,  // 이동할 PieceProblem ID
    val prevPieceProblemId: Long? = null,  // 이동될 위치의 이전 PieceProblem ID (null이면 맨 앞)
    val nextPieceProblemId: Long? = null   // 이동될 위치의 다음 PieceProblem ID (null이면 맨 뒤)
) {
    init {
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(pieceProblemId > 0) { "PieceProblem ID는 0보다 커야 합니다." }
        require(teacherId > 0) { "선생님 ID는 0보다 커야 합니다." }

        if (prevPieceProblemId != null) {
            require(prevPieceProblemId > 0) { "이전 PieceProblem ID는 0보다 커야 합니다." }
            require(prevPieceProblemId != pieceProblemId) { "자기 자신을 이전 문제로 지정할 수 없습니다." }
        }

        if (nextPieceProblemId != null) {
            require(nextPieceProblemId > 0) { "다음 PieceProblem ID는 0보다 커야 합니다." }
            require(nextPieceProblemId != pieceProblemId) { "자기 자신을 다음 문제로 지정할 수 없습니다." }
        }

        if (prevPieceProblemId != null && nextPieceProblemId != null) {
            require(prevPieceProblemId != nextPieceProblemId) { "이전 문제와 다음 문제가 같을 수 없습니다." }
        }
    }
}

/**
 * 문제 순서 수정 결과 DTO
 */
data class ProblemOrderUpdateResult(
    val pieceId: Long,
    val pieceProblemId: Long,
    val previousPosition: Double,
    val newPosition: Double,
    val success: Boolean
) 