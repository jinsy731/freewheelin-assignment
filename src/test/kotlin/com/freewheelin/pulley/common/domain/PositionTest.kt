package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.NaN
import kotlin.Double.Companion.POSITIVE_INFINITY

class PositionTest {

    @Test
    fun `유효한 위치값으로 Position을 생성할 수 있다`() {
        // Given
        val validValue = 1.5

        // When
        val position = Position(validValue)

        // Then
        assertThat(position.value).isEqualTo(validValue)
    }

    @Test
    fun `0보다 작거나 같은 값으로 Position 생성시 예외가 발생한다`() {
        // Given & When & Then
        assertThrows<ValidationException> { Position(0.0) }
        assertThrows<ValidationException> { Position(-1.0) }
        assertThrows<ValidationException> { Position(-0.1) }
    }

    @Test
    fun `무한대 값으로 Position 생성시 예외가 발생한다`() {
        // Given & When & Then
        assertThrows<ValidationException> { Position(POSITIVE_INFINITY) }
        assertThrows<ValidationException> { Position(NEGATIVE_INFINITY) }
    }

    @Test
    fun `NaN 값으로 Position 생성시 예외가 발생한다`() {
        // Given & When & Then
        assertThrows<ValidationException> { Position(NaN) }
    }

    @Test
    fun `isBefore 메서드가 올바르게 동작한다`() {
        // Given
        val position1 = Position(1.0)
        val position2 = Position(2.0)

        // When & Then
        assertThat(position1.isBefore(position2)).isTrue()
        assertThat(position2.isBefore(position1)).isFalse()
        assertThat(position1.isBefore(position1)).isFalse()
    }

    @Test
    fun `isAfter 메서드가 올바르게 동작한다`() {
        // Given
        val position1 = Position(1.0)
        val position2 = Position(2.0)

        // When & Then
        assertThat(position2.isAfter(position1)).isTrue()
        assertThat(position1.isAfter(position2)).isFalse()
        assertThat(position1.isAfter(position1)).isFalse()
    }

    @Test
    fun `두 위치 사이의 중간 위치를 올바르게 계산한다`() {
        // Given
        val before = Position(1.0)
        val after = Position(3.0)

        // When
        val middle = Position.between(before, after)

        // Then
        assertThat(middle.value).isEqualTo(2.0)
        assertThat(middle.isAfter(before)).isTrue()
        assertThat(middle.isBefore(after)).isTrue()
    }

    @Test
    fun `before가 null인 경우 after의 절반 위치를 계산한다`() {
        // Given
        val after = Position(4.0)

        // When
        val result = Position.between(null, after)

        // Then
        assertThat(result.value).isEqualTo(2.0)
        assertThat(result.isBefore(after)).isTrue()
    }

    @Test
    fun `after가 null인 경우 before보다 1 큰 위치를 계산한다`() {
        // Given
        val before = Position(5.0)

        // When
        val result = Position.between(before, null)

        // Then
        assertThat(result.value).isEqualTo(6.0)
        assertThat(result.isAfter(before)).isTrue()
    }

    @Test
    fun `before와 after가 모두 null인 경우 기본 위치 1_0을 반환한다`() {
        // Given & When
        val result = Position.between(null, null)

        // Then
        assertThat(result.value).isEqualTo(1.0)
    }

    @Test
    fun `generateInitialPositions는 연속된 위치값들을 생성한다`() {
        // Given
        val count = 5

        // When
        val positions = Position.generateInitialPositions(count)

        // Then
        assertThat(positions).hasSize(count)
        assertThat(positions[0].value).isEqualTo(1.0)
        assertThat(positions[1].value).isEqualTo(2.0)
        assertThat(positions[2].value).isEqualTo(3.0)
        assertThat(positions[3].value).isEqualTo(4.0)
        assertThat(positions[4].value).isEqualTo(5.0)
    }

    @Test
    fun `generateInitialPositions에 0 이하의 개수를 전달하면 예외가 발생한다`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> { Position.generateInitialPositions(0) }
        assertThrows<IllegalArgumentException> { Position.generateInitialPositions(-1) }
    }

    @Test
    fun `initial 메서드는 인덱스 기반으로 위치를 생성한다`() {
        // Given & When & Then
        assertThat(Position.initial(0).value).isEqualTo(1.0)
        assertThat(Position.initial(1).value).isEqualTo(2.0)
        assertThat(Position.initial(5).value).isEqualTo(6.0)
    }

    @Test
    fun `initial 메서드에 음수 인덱스를 전달하면 예외가 발생한다`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> { Position.initial(-1) }
    }

    @Test
    fun `first 메서드는 첫 번째 위치를 반환한다`() {
        // Given & When
        val firstPosition = Position.first()

        // Then
        assertThat(firstPosition.value).isEqualTo(1.0)
    }

    @Test
    fun `매우 가까운 두 위치 사이의 중간값을 올바르게 계산한다`() {
        // Given
        val before = Position(1.0)
        val after = Position(1.1)

        // When
        val middle = Position.between(before, after)

        // Then
        assertThat(middle.value).isEqualTo(1.05)
        assertThat(middle.isAfter(before)).isTrue()
        assertThat(middle.isBefore(after)).isTrue()
    }

    @Test
    fun `소수점 위치값들도 올바르게 비교된다`() {
        // Given
        val position1 = Position(1.1)
        val position2 = Position(1.2)
        val position3 = Position(1.15)

        // When & Then
        assertThat(position1.isBefore(position2)).isTrue()
        assertThat(position1.isBefore(position3)).isTrue()
        assertThat(position3.isBefore(position2)).isTrue()
        assertThat(position3.isAfter(position1)).isTrue()
    }
} 