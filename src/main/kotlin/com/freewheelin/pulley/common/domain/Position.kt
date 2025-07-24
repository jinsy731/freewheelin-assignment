package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException
import kotlin.collections.map
import kotlin.isFinite

/**
 * 위치 값객체
 *
 * Double 형태로 저장되는 위치 정보를 캡슐화합니다.
 */
data class Position(val value: Double) {

    init {
        if (!value.isFinite()) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "position",
                value,
                "유효하지 않은 위치 값입니다"
            )
        }

        if (value <= 0.0) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "position",
                value,
                "위치 값은 0보다 커야 합니다"
            )
        }
    }

    /**
     * 다른 위치보다 앞에 있는지 판단
     */
    fun isBefore(other: Position): Boolean = value < other.value

    /**
     * 다른 위치보다 뒤에 있는지 판단
     */
    fun isAfter(other: Position): Boolean = value > other.value

    companion object {
        /**
         * 두 위치 사이의 중간 위치 계산
         *
         * @param before 앞 위치 (null이면 맨 앞)
         * @param after 뒤 위치 (null이면 맨 뒤)
         * @return 새로운 중간 위치
         * @throws ValidationException before가 after보다 크거나 같은 경우
         */
        fun between(before: Position?, after: Position?): Position {
            // 입력 검증: before가 after보다 크거나 같으면 안됨
            if (before != null && after != null) {
                if (before.value >= after.value) {
                    throw ValidationException(
                        ErrorCode.VALIDATION_FAILED,
                        "position",
                        "before: ${before.value}, after: ${after.value}",
                        "앞 위치(${before.value})는 뒤 위치(${after.value})보다 작아야 합니다"
                    )
                }
            }

            return when {
                before == null && after == null -> Position(1.0)
                before == null -> Position(after!!.value / 2.0)
                after == null -> Position(before.value + 1.0)
                else -> Position((before.value + after.value) / 2.0)
            }
        }

        /**
         * 초기 위치 값들 생성 (Position 객체 반환)
         */
        fun generateInitialPositions(count: Int): List<Position> {
            require(count > 0) { "개수는 0보다 커야 합니다: $count" }

            return (1..count).map { index ->
                Position(index.toDouble())
            }
        }

        /**
         * 인덱스 기반 초기 위치 생성
         */
        fun initial(index: Int): Position {
            require(index >= 0) { "인덱스는 0 이상이어야 합니다: $index" }
            return Position((index + 1).toDouble())
        }

        /**
         * 첫 번째 위치
         */
        fun first(): Position = Position(1.0)
    }
} 