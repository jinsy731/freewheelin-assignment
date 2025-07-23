package com.freewheelin.pulley.problem.domain.service

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.common.domain.LevelDistribution
import org.springframework.stereotype.Service

/**
 * 문제 분배 도메인 서비스
 *
 * 난이도별 비율에 따른 문제 분배 로직을 담당합니다.
 * 실제 존재하는 문제 개수 제약을 고려하여 최적의 분배 계획을 수립합니다.
 *
 * 비즈니스 규칙:
 * - 상 선택시: 하 20%, 중 30%, 상 50%
 * - 중 선택시: 하 25%, 중 50%, 상 25%
 * - 하 선택시: 하 50%, 중 30%, 상 20%
 */
@Service
class ProblemDistributionService {

    /**
     * 실제 존재하는 문제 개수를 고려한 분배 계획 계산
     *
     * @param level 선택된 난이도 (분배 비율 결정)
     * @param totalCount 총 문제 수
     * @param availableCounts 각 난이도별 실제 존재하는 문제 개수
     * @return 실제 제약을 고려한 난이도별 분배 계획
     */
    fun calculateDistribution(
        problemLevel: Level,
        totalCount: Int,
        availableCounts: AvailableProblemCounts
    ): ProblemDistributionPlan {
        // 1. 선택된 난이도에 따른 이상적 분배 비율 계산
        val levelDistribution = LevelDistribution.forLevel(problemLevel)
        val idealCounts = levelDistribution.calculateCounts(totalCount)

        // 2. 실제 존재하는 문제 개수 제약 적용
        val constrainedLowCount = minOf(idealCounts.lowCount, availableCounts.lowCount)
        val constrainedMiddleCount = minOf(idealCounts.middleCount, availableCounts.middleCount)
        val constrainedHighCount = minOf(idealCounts.highCount, availableCounts.highCount)

        val constrainedTotal = constrainedLowCount + constrainedMiddleCount + constrainedHighCount

        // 3. 제약으로 인해 부족한 문제가 있다면 재분배
        val shortfall = totalCount - constrainedTotal

        if (shortfall > 0) {
            // 부족한 문제를 다른 난이도에서 보충
            return redistributeShortfall(
                constrainedLowCount, constrainedMiddleCount, constrainedHighCount,
                availableCounts, shortfall
            )
        }

        return ProblemDistributionPlan(
            lowCount = constrainedLowCount,
            middleCount = constrainedMiddleCount,
            highCount = constrainedHighCount,
            totalCount = constrainedTotal
        )
    }

    /**
     * 부족한 문제를 다른 난이도에 재분배
     *
     * @param lowCount 하 난이도 조회할 문제 수
     * @param middleCount 중 난이도 조회할 문제 수
     * @param highCount 상 난이도 조회할 문제 수
     * @param availableCounts 각 난이도별 실제 존재하는 문제 개수
     * @param shortfall 부족한 문제 수
     * @param requestedTotal 요청한 총 문제 수
     */
    private fun redistributeShortfall(
        lowCount: Int,
        middleCount: Int,
        highCount: Int,
        availableCounts: AvailableProblemCounts,
        shortfall: Int,
    ): ProblemDistributionPlan {
        var adjustedLow = lowCount
        var adjustedMiddle = middleCount
        var adjustedHigh = highCount
        var remainingShortfall = shortfall

        // 하 난이도에서 보충 가능한 만큼
        val additionalLow = minOf(remainingShortfall, availableCounts.lowCount - adjustedLow)
        adjustedLow += additionalLow
        remainingShortfall -= additionalLow

        // 중 난이도에서 보충 가능한 만큼
        if (remainingShortfall > 0) {
            val additionalMiddle = minOf(remainingShortfall, availableCounts.middleCount - adjustedMiddle)
            adjustedMiddle += additionalMiddle
            remainingShortfall -= additionalMiddle
        }

        // 상 난이도에서 보충 가능한 만큼
        if (remainingShortfall > 0) {
            val additionalHigh = minOf(remainingShortfall, availableCounts.highCount - adjustedHigh)
            adjustedHigh += additionalHigh
        }

        val actualTotal = adjustedLow + adjustedMiddle + adjustedHigh

        return ProblemDistributionPlan(
            lowCount = adjustedLow,
            middleCount = adjustedMiddle,
            highCount = adjustedHigh,
            totalCount = actualTotal
        )
    }
}

/**
 * 각 난이도별 실제 존재하는 문제 개수
 */
data class AvailableProblemCounts(
    val lowCount: Int,      // 하 난이도 실제 문제 수 (level 1)
    val middleCount: Int,   // 중 난이도 실제 문제 수 (level 2,3,4)
    val highCount: Int      // 상 난이도 실제 문제 수 (level 5)
) {
    init {
        require(lowCount >= 0) { "하 난이도 실제 문제 수는 0 이상이어야 합니다." }
        require(middleCount >= 0) { "중 난이도 실제 문제 수는 0 이상이어야 합니다." }
        require(highCount >= 0) { "상 난이도 실제 문제 수는 0 이상이어야 합니다." }
    }
}

/**
 * 문제 분배 계획
 *
 * 각 난이도별로 실제 조회할 문제 수를 담은 데이터 클래스
 */
data class ProblemDistributionPlan(
    val lowCount: Int,      // 하 난이도 조회할 문제 수 (level 1)
    val middleCount: Int,   // 중 난이도 조회할 문제 수 (level 2,3,4)
    val highCount: Int,     // 상 난이도 조회할 문제 수 (level 5)
    val totalCount: Int     // 실제 조회할 총 문제 수
) {
    init {
        require(lowCount >= 0) { "하 난이도 문제 수는 0 이상이어야 합니다." }
        require(middleCount >= 0) { "중 난이도 문제 수는 0 이상이어야 합니다." }
        require(highCount >= 0) { "상 난이도 문제 수는 0 이상이어야 합니다." }
        require(lowCount + middleCount + highCount == totalCount) {
            "각 난이도별 문제 수의 합은 총 문제 수와 일치해야 합니다."
        }
    }
}