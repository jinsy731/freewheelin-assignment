package com.freewheelin.pulley.statistics.domain.model

import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.ProblemId


/**
 * 문제별 학습지 통계 도메인 모델
 */
data class PieceProblemStat(
    val id: Long = 0L,
    val pieceId: PieceId,
    val problemId: ProblemId,
    val totalCount: Int,
    val correctCount: Int,
    val correctnessRate: CorrectnessRate
) {
    init {
        require(totalCount >= MIN_COUNT) { "전체 제출 수는 $MIN_COUNT 이상이어야 합니다." }
        require(correctCount >= MIN_COUNT) { "정답 수는 $MIN_COUNT 이상이어야 합니다." }
        require(correctCount <= totalCount) { "정답 수는 전체 제출 수를 초과할 수 없습니다." }
    }
    
    /**
     * 통계 업데이트 (증분)
     */
    fun updateIncrement(isCorrect: Boolean): PieceProblemStat {
        val newTotalCount = totalCount + INCREMENT_UNIT
        val newCorrectCount = if (isCorrect) correctCount + INCREMENT_UNIT else correctCount
        val newRate = calculateCorrectnessRate(newTotalCount, newCorrectCount)
        
        return copy(
            totalCount = newTotalCount,
            correctCount = newCorrectCount,
            correctnessRate = newRate
        )
    }
    
    /**
     * 통계 업데이트 (전체 재계산)
     */
    fun update(newTotalCount: Int, newCorrectCount: Int): PieceProblemStat {
        val newRate = calculateCorrectnessRate(newTotalCount, newCorrectCount)
        
        return copy(
            totalCount = newTotalCount,
            correctCount = newCorrectCount,
            correctnessRate = newRate
        )
    }
    
    /**
     * 정답률 계산
     */
    private fun calculateCorrectnessRate(totalCount: Int, correctCount: Int): CorrectnessRate {
        return if (hasValidSubmissions(totalCount)) {
            CorrectnessRate(correctCount.toDouble() / totalCount)
        } else {
            CorrectnessRate.zero()
        }
    }
    
    /**
     * 유효한 제출이 있는지 확인
     */
    private fun hasValidSubmissions(totalCount: Int): Boolean {
        return totalCount > MIN_COUNT
    }
    
    companion object {
        private const val MIN_COUNT = 0
        private const val INCREMENT_UNIT = 1
        
        /**
         * 초기 통계 생성
         */
        fun create(
            pieceId: Long,
            problemId: Long,
            totalCount: Int = MIN_COUNT,
            correctCount: Int = MIN_COUNT
        ): PieceProblemStat {
            val rate = if (totalCount > MIN_COUNT) {
                CorrectnessRate(correctCount.toDouble() / totalCount)
            } else {
                CorrectnessRate.zero()
            }
            
            return PieceProblemStat(
                pieceId = PieceId(pieceId),
                problemId = ProblemId(problemId),
                totalCount = totalCount,
                correctCount = correctCount,
                correctnessRate = rate
            )
        }
    }
} 