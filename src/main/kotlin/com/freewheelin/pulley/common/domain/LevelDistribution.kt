package com.freewheelin.pulley.common.domain

/**
 * 난이도별 문제 분배를 위한 비율 정보
 */
data class LevelDistribution(
    val lowRatio: Double,    // 하 비율
    val middleRatio: Double, // 중 비율
    val highRatio: Double    // 상 비율
) {
    init {
        require(lowRatio + middleRatio + highRatio == 1.0) {
            "비율의 합은 1.0이어야 합니다."
        }
    }

    /**
     * 총 문제 수에 따른 각 난이도별 문제 수 계산
     *
     * @param totalCount 총 문제 수
     * @return 각 난이도별 문제 수
     */
    fun calculateCounts(totalCount: Int): LevelCounts {
        val lowCount = (totalCount * lowRatio).toInt()
        val middleCount = (totalCount * middleRatio).toInt()
        val highCount = totalCount - lowCount - middleCount // 나머지는 상 난이도에 할당

        return LevelCounts(lowCount, middleCount, highCount)
    }

    companion object {
        /**
         * 선택된 난이도에 따른 분배 비율 반환
         *
         * @param level 난이도
         * @return 난이도별 분배 비율
         */
        fun forLevel(level: Level): LevelDistribution = when (level) {
            Level.HIGH -> LevelDistribution(0.2, 0.3, 0.5)   // 상: 하 20%, 중 30%, 상 50%
            Level.MIDDLE -> LevelDistribution(0.25, 0.5, 0.25) // 중: 하 25%, 중 50%, 상 25%
            Level.LOW -> LevelDistribution(0.5, 0.3, 0.2)    // 하: 하 50%, 중 30%, 상 20%
        }
    }
}

/**
 * 각 난이도별 문제 수
 */
data class LevelCounts(
    val lowCount: Int,
    val middleCount: Int,
    val highCount: Int
) {
    val total: Int get() = lowCount + middleCount + highCount
}