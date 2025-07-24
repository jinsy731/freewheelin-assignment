package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.NaN
import kotlin.Double.Companion.POSITIVE_INFINITY

class PositionTest {

    @Test
    fun `정상 생성 - 양수 값`() {
        // Given & When
        val position = Position(1.5)

        // Then
        assertEquals(1.5, position.value)
    }

    @Test
    fun `예외 발생 - 0 이하 값`() {
        // When & Then
        assertThrows<ValidationException> {
            Position(0.0)
        }

        assertThrows<ValidationException> {
            Position(-1.0)
        }
    }

    @Test
    fun `예외 발생 - 무한대 값`() {
        // When & Then
        assertThrows<ValidationException> {
            Position(Double.POSITIVE_INFINITY)
        }

        assertThrows<ValidationException> {
            Position(Double.NEGATIVE_INFINITY)
        }

        assertThrows<ValidationException> {
            Position(Double.NaN)
        }
    }

    @Test
    fun `위치 비교 - isBefore`() {
        // Given
        val position1 = Position(1.0)
        val position2 = Position(2.0)

        // When & Then
        assertTrue(position1.isBefore(position2))
        assertTrue(!position2.isBefore(position1))
    }

    @Test
    fun `위치 비교 - isAfter`() {
        // Given
        val position1 = Position(1.0)
        val position2 = Position(2.0)

        // When & Then
        assertTrue(position2.isAfter(position1))
        assertTrue(!position1.isAfter(position2))
    }

    @Test
    fun `between - 둘 다 null인 경우 (기본 위치)`() {
        // When
        val result = Position.between(null, null)

        // Then
        assertEquals(1.0, result.value)
    }

    @Test
    fun `between - before가 null인 경우 (맨 앞으로 이동)`() {
        // Given
        val after = Position(2.0)

        // When
        val result = Position.between(null, after)

        // Then
        assertEquals(1.0, result.value) // 2.0 / 2 = 1.0
    }

    @Test
    fun `between - after가 null인 경우 (맨 뒤로 이동)`() {
        // Given
        val before = Position(3.0)

        // When
        val result = Position.between(before, null)

        // Then
        assertEquals(4.0, result.value) // 3.0 + 1.0 = 4.0
    }

    @Test
    fun `between - 정상적인 중간 위치 계산`() {
        // Given
        val before = Position(2.0)
        val after = Position(4.0)

        // When
        val result = Position.between(before, after)

        // Then
        assertEquals(3.0, result.value) // (2.0 + 4.0) / 2 = 3.0
    }

    @Test
    fun `between - before가 after보다 큰 경우 예외 발생`() {
        // Given
        val before = Position(5.0)
        val after = Position(3.0)

        // When & Then
        val exception = assertThrows<ValidationException> {
            Position.between(before, after)
        }

        assertTrue(exception.message!!.contains("앞 위치(5.0)는 뒤 위치(3.0)보다 작아야 합니다"))
    }

    @Test
    fun `between - before와 after가 같은 경우 예외 발생`() {
        // Given
        val before = Position(2.0)
        val after = Position(2.0)

        // When & Then
        val exception = assertThrows<ValidationException> {
            Position.between(before, after)
        }

        assertTrue(exception.message!!.contains("앞 위치(2.0)는 뒤 위치(2.0)보다 작아야 합니다"))
    }

    @Test
    fun `generateInitialPositions - 정상적인 개수`() {
        // When
        val positions = Position.generateInitialPositions(3)

        // Then
        assertEquals(3, positions.size)
        assertEquals(1.0, positions[0].value)
        assertEquals(2.0, positions[1].value)
        assertEquals(3.0, positions[2].value)
    }

    @Test
    fun `generateInitialPositions - 0 이하 개수로 예외 발생`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            Position.generateInitialPositions(0)
        }

        assertThrows<IllegalArgumentException> {
            Position.generateInitialPositions(-1)
        }
    }

    @Test
    fun `initial - 인덱스 기반 위치 생성`() {
        // When
        val position0 = Position.initial(0)
        val position5 = Position.initial(5)

        // Then
        assertEquals(1.0, position0.value) // 0 + 1 = 1
        assertEquals(6.0, position5.value) // 5 + 1 = 6
    }

    @Test
    fun `initial - 음수 인덱스로 예외 발생`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            Position.initial(-1)
        }
    }

    @Test
    fun `first - 첫 번째 위치`() {
        // When
        val first = Position.first()

        // Then
        assertEquals(1.0, first.value)
    }

    @Test
    fun `소수점 위치 계산 - 간단한 경우`() {
        // Given
        val before = Position(1.0)
        val after = Position(2.0)

        // When
        val middle = Position.between(before, after)

        // Then
        assertEquals(1.5, middle.value)
    }

    @Test
    fun `연속적인 between 호출 - 실제 사용 시나리오`() {
        // Given - 1.0과 2.0 사이에 여러 문제를 순차적으로 삽입
        val pos1 = Position(1.0)
        val pos2 = Position(2.0)

        // When - 첫 번째 삽입
        val pos1_5 = Position.between(pos1, pos2) // 1.5

        // Then
        assertEquals(1.5, pos1_5.value)

        // When - 두 번째 삽입 (1.0과 1.5 사이)
        val pos1_25 = Position.between(pos1, pos1_5) // 1.25

        // Then
        assertEquals(1.25, pos1_25.value)

        // When - 세 번째 삽입 (1.5와 2.0 사이)
        val pos1_75 = Position.between(pos1_5, pos2) // 1.75

        // Then
        assertEquals(1.75, pos1_75.value)
    }
} 