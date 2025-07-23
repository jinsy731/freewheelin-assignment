package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.nextUp

class PieceProblemTest {

    @Test
    fun `유효한 정보로 새로운 PieceProblem을 생성할 수 있다`() {
        // Given
        val pieceId = PieceId(1L)
        val problemId = ProblemId(2L)
        val position = Position(1.0)

        // When
        val pieceProblem = PieceProblem.create(pieceId, problemId, position)

        // Then
        assertThat(pieceProblem.pieceId).isEqualTo(pieceId)
        assertThat(pieceProblem.problemId).isEqualTo(problemId)
        assertThat(pieceProblem.position).isEqualTo(position)
        assertThat(pieceProblem.id).isEqualTo(0L) // JPA가 자동 생성할 임시 ID
    }

    @Test
    fun `위치를 업데이트할 수 있다`() {
        // Given
        val originalPosition = Position(1.0)
        val newPosition = Position(2.0)
        val pieceProblem = createTestPieceProblem(position = originalPosition)

        // When
        val updatedPieceProblem = pieceProblem.updatePosition(newPosition)

        // Then
        assertThat(updatedPieceProblem.position).isEqualTo(newPosition)
        assertThat(updatedPieceProblem.pieceId).isEqualTo(pieceProblem.pieceId)
        assertThat(updatedPieceProblem.problemId).isEqualTo(pieceProblem.problemId)
        assertThat(updatedPieceProblem.id).isEqualTo(pieceProblem.id)
    }

    @Test
    fun `두 문제 사이로 이동할 수 있다`() {
        // Given
        val targetProblem = createTestPieceProblem(position = Position(5.0))
        val prevProblem = createTestPieceProblem(position = Position(1.0))
        val nextProblem = createTestPieceProblem(position = Position(3.0))

        // When
        val movedProblem = targetProblem.moveTo(prevProblem, nextProblem)

        // Then
        assertThat(movedProblem.position.value).isEqualTo(2.0) // (1.0 + 3.0) / 2
        assertThat(movedProblem.position.isAfter(prevProblem.position)).isTrue()
        assertThat(movedProblem.position.isBefore(nextProblem.position)).isTrue()
    }

    @Test
    fun `맨 앞으로 이동할 수 있다 (prevProblem이 null)`() {
        // Given
        val targetProblem = createTestPieceProblem(position = Position(5.0))
        val nextProblem = createTestPieceProblem(position = Position(4.0))

        // When
        val movedProblem = targetProblem.moveTo(null, nextProblem)

        // Then
        assertThat(movedProblem.position.value).isEqualTo(2.0) // 4.0 / 2
        assertThat(movedProblem.position.isBefore(nextProblem.position)).isTrue()
    }

    @Test
    fun `맨 뒤로 이동할 수 있다 (nextProblem이 null)`() {
        // Given
        val targetProblem = createTestPieceProblem(position = Position(1.0))
        val prevProblem = createTestPieceProblem(position = Position(3.0))

        // When
        val movedProblem = targetProblem.moveTo(prevProblem, null)

        // Then
        assertThat(movedProblem.position.value).isEqualTo(4.0) // 3.0 + 1.0
        assertThat(movedProblem.position.isAfter(prevProblem.position)).isTrue()
    }

    @Test
    fun `첫 번째 위치로 이동할 수 있다 (prevProblem과 nextProblem이 모두 null)`() {
        // Given
        val targetProblem = createTestPieceProblem(position = Position(5.0))

        // When
        val movedProblem = targetProblem.moveTo(null, null)

        // Then
        assertThat(movedProblem.position.value).isEqualTo(1.0) // 기본 위치
    }

    @Test
    fun `위치 업데이트 시 다른 속성은 변경되지 않는다`() {
        // Given
        val originalPieceProblem = createTestPieceProblem(
            id = 100L,
            pieceId = PieceId(10L),
            problemId = ProblemId(20L),
            position = Position(1.5)
        )
        val newPosition = Position(3.7)

        // When
        val updatedPieceProblem = originalPieceProblem.updatePosition(newPosition)

        // Then
        assertThat(updatedPieceProblem.id).isEqualTo(originalPieceProblem.id)
        assertThat(updatedPieceProblem.pieceId).isEqualTo(originalPieceProblem.pieceId)
        assertThat(updatedPieceProblem.problemId).isEqualTo(originalPieceProblem.problemId)
        assertThat(updatedPieceProblem.position).isEqualTo(newPosition)
        assertThat(updatedPieceProblem.position).isNotEqualTo(originalPieceProblem.position)
    }


    @Test
    fun `소수점 위치로 이동할 수 있다`() {
        // Given
        val targetProblem = createTestPieceProblem(position = Position(1.0))
        val prevProblem = createTestPieceProblem(position = Position(2.5))
        val nextProblem = createTestPieceProblem(position = Position(4.7))

        // When
        val movedProblem = targetProblem.moveTo(prevProblem, nextProblem)

        // Then
        assertThat(movedProblem.position.value).isEqualTo(3.6) // (2.5 + 4.7) / 2
        assertThat(movedProblem.position.isAfter(prevProblem.position)).isTrue()
        assertThat(movedProblem.position.isBefore(nextProblem.position)).isTrue()
    }

    @Test
    fun `동일한 위치의 다른 PieceProblem들을 구분할 수 있다`() {
        // Given
        val samePosition = Position(1.0)
        val problem1 = createTestPieceProblem(
            problemId = ProblemId(1L),
            position = samePosition
        )
        val problem2 = createTestPieceProblem(
            problemId = ProblemId(2L),
            position = samePosition
        )

        // When & Then
        assertThat(problem1.position).isEqualTo(problem2.position)
        assertThat(problem1.problemId).isNotEqualTo(problem2.problemId)
        assertThat(problem1).isNotEqualTo(problem2)
    }

    @Test
    fun `여러 번의 위치 업데이트가 올바르게 수행된다`() {
        // Given
        val originalProblem = createTestPieceProblem(position = Position(1.0))

        // When
        val step1 = originalProblem.updatePosition(Position(2.0))
        val step2 = step1.updatePosition(Position(3.0))
        val step3 = step2.updatePosition(Position(1.5))

        // Then
        assertThat(originalProblem.position.value).isEqualTo(1.0)
        assertThat(step1.position.value).isEqualTo(2.0)
        assertThat(step2.position.value).isEqualTo(3.0)
        assertThat(step3.position.value).isEqualTo(1.5)
        
        // 다른 속성들은 모두 동일해야 함
        assertThat(step3.id).isEqualTo(originalProblem.id)
        assertThat(step3.pieceId).isEqualTo(originalProblem.pieceId)
        assertThat(step3.problemId).isEqualTo(originalProblem.problemId)
    }

    private fun createTestPieceProblem(
        id: Long = 1L,
        pieceId: PieceId = PieceId(1L),
        problemId: ProblemId = ProblemId(1L),
        position: Position = Position(1.0)
    ): PieceProblem {
        return PieceProblem(
            id = id,
            pieceId = pieceId,
            problemId = problemId,
            position = position
        )
    }
} 