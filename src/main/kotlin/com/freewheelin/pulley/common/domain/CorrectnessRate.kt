package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.assignment.domain.model.Submission
import kotlin.collections.count
import kotlin.ranges.rangeTo

/**
 * 정답률 값객체
 * 
 * 0.0~1.0 범위의 정답률을 나타내며, 도메인 규칙과 검증 로직을 포함합니다.
 */
data class CorrectnessRate(val value: Double) {
    init {
        require(value in 0.0..1.0) { "정답률은 0.0~1.0 사이여야 합니다: $value" }
    }
    
    /**
     * 백분율로 변환
     */
    fun toPercentage(): Double = value * 100.0
    
    companion object {
        /**
         * 제출 목록으로부터 정답률 계산
         */
        fun calculate(submissions: List<Submission>): CorrectnessRate {
            if (submissions.isEmpty()) {
                return CorrectnessRate(0.0)
            }
            
            val correctCount = submissions.count { it.isCorrect }
            return CorrectnessRate(correctCount.toDouble() / submissions.size)
        }

        /**
         * 0% 정답률
         */
        fun zero(): CorrectnessRate = CorrectnessRate(0.0)
    }
} 