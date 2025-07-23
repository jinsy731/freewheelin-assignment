package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException

/**
 * 문제 난이도를 나타내는 enum
 * - LOW: 하 (level 1)
 * - MIDDLE: 중 (level 2,3,4)
 * - HIGH: 상 (level 5)
 */
enum class Level(val levels: List<Int>) {
    LOW(listOf(1)),        // 하
    MIDDLE(listOf(2, 3, 4)), // 중
    HIGH(listOf(5));         // 상

    /**
     * 특정 레벨 값이 이 카테고리에 포함되는지 확인
     */
    fun contains(level: Int): Boolean = levels.contains(level)

    companion object {
        /**
         * 레벨 값을 Level enum으로 변환
         */
        fun fromLevel(level: Int): Level = values().find { it.contains(level) }
            ?: throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "level",
                level,
                "유효하지 않은 level입니다. 1-5 사이의 값이어야 합니다."
            )

        /**
         * 모든 난이도 레벨을 반환 (1~5)
         */
        fun getAllLevels(): List<Int> = (1..5).toList()
    }
}