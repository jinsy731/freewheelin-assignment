package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.common.exception.ValidationException

/**
 * 학습지 문제들의 일급 컬렉션
 * 
 * 문제 순서 변경과 관련된 비즈니스 로직을 캡슐화합니다.
 * - 연속성 검증
 * - 순서 변경 로직
 * - 위치 관련 비즈니스 규칙
 */
data class PieceProblems(
    private val problems: List<PieceProblem>
) {
    
    /**
     * PieceProblem ID로 문제 찾기
     */
    fun findByPieceProblemId(pieceProblemId: Long): PieceProblem? {
        return problems.find { it.id == pieceProblemId }
    }
    
    /**
     * PieceProblem ID로 문제 찾기 (없으면 예외)
     */
    fun getByPieceProblemId(pieceProblemId: Long): PieceProblem {
        return findByPieceProblemId(pieceProblemId)
            ?: throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "PieceProblem을 찾을 수 없습니다: $pieceProblemId"
            )
    }
    
    /**
     * Problem ID로 문제 찾기 (기존 호환성을 위해 유지)
     */
    fun findByProblemId(problemId: Long): PieceProblem? {
        return problems.find { it.problemId.value == problemId }
    }
    
    /**
     * Problem ID로 문제 찾기 (없으면 예외, 기존 호환성을 위해 유지)
     */
    fun getByProblemId(problemId: Long): PieceProblem {
        return findByProblemId(problemId)
            ?: throw NotFoundException(
                ErrorCode.PROBLEM_NOT_IN_PIECE,
                "문제를 찾을 수 없습니다: $problemId"
            )
    }
    
    /**
     * 연속성 검증: prevPieceProblemId와 nextPieceProblemId가 실제로 연속되어 있는지 확인
     * 
     * 개선된 알고리즘:
     * - prev와 next position 사이에 다른 문제가 있는지만 확인
     * - 모든 문제를 조회할 필요 없음
     * 
     * @param prevProblem 이전 문제 (null 가능)
     * @param nextProblem 다음 문제 (null 가능)
     * @param excludePieceProblemId 제외할 PieceProblem ID (이동할 문제)
     */
    fun validateConsecutiveness(
        prevProblem: PieceProblem?,
        nextProblem: PieceProblem?,
        excludePieceProblemId: Long
    ) {
        // 둘 중 하나라도 null이면 연속성 검증 불필요
        if (prevProblem == null || nextProblem == null) return
        
        // 순서 검증: prev < next
        if (!prevProblem.position.isBefore(nextProblem.position)) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "order",
                "${prevProblem.position.value} >= ${nextProblem.position.value}",
                "이전 문제의 위치가 다음 문제의 위치보다 뒤에 있습니다."
            )
        }
        
        // 연속성 검증: prev와 next 사이에 다른 문제(이동할 문제 제외)가 있는지 확인
        val problemsBetween = problems.filter { problem ->
            problem.id != excludePieceProblemId &&
            problem.position.isAfter(prevProblem.position) &&
            problem.position.isBefore(nextProblem.position)
        }
        
        if (problemsBetween.isNotEmpty()) {
            val betweenProblemIds = problemsBetween.map { it.id }
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "consecutiveness",
                "prev: ${prevProblem.id}, next: ${nextProblem.id}, between: $betweenProblemIds",
                "이전 문제와 다음 문제가 연속되어 있지 않습니다. " +
                "사이에 다른 문제들이 있습니다: $betweenProblemIds"
            )
        }
    }
    
    /**
     * 문제 순서 변경 (PieceProblem ID 기반)
     * 
     * @param pieceProblemId 이동할 PieceProblem ID
     * @param prevPieceProblemId 이전 PieceProblem ID (null이면 맨 앞)
     * @param nextPieceProblemId 다음 PieceProblem ID (null이면 맨 뒤)
     * @return 위치가 변경된 PieceProblem (변경이 불필요하면 null)
     */
    fun moveOrderTo(
        pieceProblemId: Long,
        prevPieceProblemId: Long?,
        nextPieceProblemId: Long?
    ): PieceProblem? {
        // 1. 이동할 문제 찾기
        val problemToMove = getByPieceProblemId(pieceProblemId)
        
        // 2. 이전/다음 문제 찾기
        val prevProblem = prevPieceProblemId?.let { getByPieceProblemId(it) }
        val nextProblem = nextPieceProblemId?.let { getByPieceProblemId(it) }
        
        // 3. 연속성 검증
        validateConsecutiveness(prevProblem, nextProblem, pieceProblemId)
        
        // 4. 이미 올바른 위치에 있는지 확인
        if (isAlreadyInCorrectPosition(problemToMove, prevProblem, nextProblem)) {
            return null // 이동 불필요
        }
        
        // 5. 위치 변경
        return problemToMove.moveTo(prevProblem, nextProblem)
    }
    
    /**
     * 문제 순서 변경 (Problem ID 기반, 기존 호환성을 위해 유지)
     * 
     * @param problemId 이동할 문제 ID
     * @param prevProblemId 이전 문제 ID (null이면 맨 앞)
     * @param nextProblemId 다음 문제 ID (null이면 맨 뒤)
     * @return 위치가 변경된 PieceProblem (변경이 불필요하면 null)
     */
    @Deprecated("Use moveOrderTo with PieceProblem ID instead", ReplaceWith("moveOrderTo(pieceProblemId, prevPieceProblemId, nextPieceProblemId)"))
    fun moveOrderToByProblemId(
        problemId: Long,
        prevProblemId: Long?,
        nextProblemId: Long?
    ): PieceProblem? {
        // 1. 이동할 문제 찾기
        val problemToMove = getByProblemId(problemId)
        
        // 2. 이전/다음 문제 찾기
        val prevProblem = prevProblemId?.let { getByProblemId(it) }
        val nextProblem = nextProblemId?.let { getByProblemId(it) }
        
        // 3. 연속성 검증
        validateConsecutiveness(prevProblem, nextProblem, problemToMove.id)
        
        // 4. 이미 올바른 위치에 있는지 확인
        if (isAlreadyInCorrectPosition(problemToMove, prevProblem, nextProblem)) {
            return null // 이동 불필요
        }
        
        // 5. 위치 변경
        return problemToMove.moveTo(prevProblem, nextProblem)
    }
    
    /**
     * 이미 올바른 위치에 있는지 확인
     */
    private fun isAlreadyInCorrectPosition(
        problemToMove: PieceProblem,
        targetPrevProblem: PieceProblem?,
        targetNextProblem: PieceProblem?
    ): Boolean {
        return when {
            targetPrevProblem == null && targetNextProblem == null -> false // 맨 뒤로 이동하는 경우는 항상 허용
            targetPrevProblem == null -> problemToMove.position.isBefore(targetNextProblem!!.position)
            targetNextProblem == null -> problemToMove.position.isAfter(targetPrevProblem.position)
            else -> problemToMove.position.isAfter(targetPrevProblem.position) && 
                    problemToMove.position.isBefore(targetNextProblem.position)
        }
    }
    
    /**
     * 컬렉션이 비어있는지 확인
     */
    fun isEmpty(): Boolean = problems.isEmpty()
    
    /**
     * 컬렉션의 크기
     */
    fun size(): Int = problems.size
    
    /**
     * 불변 리스트로 변환 (외부 노출용)
     */
    fun toList(): List<PieceProblem> = problems.toList()
    
    companion object {
        /**
         * 빈 컬렉션 생성
         */
        fun empty(): PieceProblems = PieceProblems(emptyList())
        
        /**
         * 단일 문제로 컬렉션 생성
         */
        fun of(problem: PieceProblem): PieceProblems = PieceProblems(listOf(problem))
        
        /**
         * 여러 문제로 컬렉션 생성
         */
        fun of(problems: List<PieceProblem>): PieceProblems = PieceProblems(problems)
    }
} 