package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.exception.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.nextUp

class PieceProblemTest {

    @Test
    fun `정상 생성 - create 메소드`() {
        // Given
        val pieceId = PieceId(100L)
        val problemId = ProblemId(1001L)
        val position = Position(1.5)

        // When
        val pieceProblem = PieceProblem.create(pieceId, problemId, position)

        // Then
        assertEquals(0L, pieceProblem.id) // JPA 임시 ID
        assertEquals(pieceId, pieceProblem.pieceId)
        assertEquals(problemId, pieceProblem.problemId)
        assertEquals(position, pieceProblem.position)
    }

    @Test
    fun `위치 업데이트 - updatePosition`() {
        // Given
        val pieceProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(1.0)
        )
        val newPosition = Position(2.5)

        // When
        val updated = pieceProblem.updatePosition(newPosition)

        // Then
        assertEquals(newPosition, updated.position)
        assertEquals(pieceProblem.pieceId, updated.pieceId)
        assertEquals(pieceProblem.problemId, updated.problemId)
    }

    @Test
    fun `정상적인 이동 - 두 문제 사이로 이동`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1003L),
            Position(3.0)
        )

        val prevProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(1.0)
        )

        val nextProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1002L),
            Position(2.0)
        )

        // When
        val moved = problemToMove.moveTo(prevProblem, nextProblem)

        // Then
        assertEquals(1.5, moved.position.value) // (1.0 + 2.0) / 2
        assertEquals(problemToMove.pieceId, moved.pieceId)
        assertEquals(problemToMove.problemId, moved.problemId)
    }

    @Test
    fun `정상적인 이동 - 맨 앞으로 이동 (prev=null)`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1003L),
            Position(3.0)
        )

        val nextProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(2.0)
        )

        // When
        val moved = problemToMove.moveTo(null, nextProblem)

        // Then
        assertEquals(1.0, moved.position.value) // 2.0 / 2
    }

    @Test
    fun `정상적인 이동 - 맨 뒤로 이동 (next=null)`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(1.0)
        )

        val prevProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1003L),
            Position(3.0)
        )

        // When
        val moved = problemToMove.moveTo(prevProblem, null)

        // Then
        assertEquals(4.0, moved.position.value) // 3.0 + 1.0
    }

    @Test
    fun `예외 발생 - 자기 자신을 이전 문제로 지정`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(2.0)
        )

        // When & Then
        val exception = assertThrows<ValidationException> {
            problemToMove.moveTo(problemToMove, null) // 자기 자신을 prev로 지정
        }

        assertTrue(exception.message!!.contains("자기 자신을 이전 문제로 지정할 수 없습니다"))
        assertTrue(exception.message!!.contains("1001"))
    }

    @Test
    fun `예외 발생 - 자기 자신을 다음 문제로 지정`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(2.0)
        )

        // When & Then
        val exception = assertThrows<ValidationException> {
            problemToMove.moveTo(null, problemToMove) // 자기 자신을 next로 지정
        }

        assertTrue(exception.message!!.contains("자기 자신을 다음 문제로 지정할 수 없습니다"))
        assertTrue(exception.message!!.contains("1001"))
    }

    @Test
    fun `같은 학습지 문제 확인 - belongsToSamePiece`() {
        // Given
        val pieceId = PieceId(100L)
        val problem1 = PieceProblem.create(pieceId, ProblemId(1001L), Position(1.0))
        val problem2 = PieceProblem.create(pieceId, ProblemId(1002L), Position(2.0))
        val problemDifferentPiece = PieceProblem.create(PieceId(200L), ProblemId(1003L), Position(3.0))

        // When & Then
        assertTrue(problem1.belongsToSamePiece(problem2))
        assertFalse(problem1.belongsToSamePiece(problemDifferentPiece))
    }

    @Test
    fun `위치 비교 - isBefore (같은 학습지)`() {
        // Given
        val pieceId = PieceId(100L)
        val problem1 = PieceProblem.create(pieceId, ProblemId(1001L), Position(1.0))
        val problem2 = PieceProblem.create(pieceId, ProblemId(1002L), Position(2.0))

        // When & Then
        assertTrue(problem1.isBefore(problem2))
        assertFalse(problem2.isBefore(problem1))
    }

    @Test
    fun `위치 비교 - isAfter (같은 학습지)`() {
        // Given
        val pieceId = PieceId(100L)
        val problem1 = PieceProblem.create(pieceId, ProblemId(1001L), Position(1.0))
        val problem2 = PieceProblem.create(pieceId, ProblemId(1002L), Position(2.0))

        // When & Then
        assertTrue(problem2.isAfter(problem1))
        assertFalse(problem1.isAfter(problem2))
    }

    @Test
    fun `예외 발생 - 다른 학습지 문제와 위치 비교 (isBefore)`() {
        // Given
        val problem1 = PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0))
        val problemDifferentPiece = PieceProblem.create(PieceId(200L), ProblemId(1002L), Position(2.0))

        // When & Then
        val exception = assertThrows<ValidationException> {
            problem1.isBefore(problemDifferentPiece)
        }

        assertTrue(exception.message!!.contains("다른 학습지에 속한 문제와는 위치 비교를 할 수 없습니다"))
    }

    @Test
    fun `예외 발생 - 다른 학습지 문제와 위치 비교 (isAfter)`() {
        // Given
        val problem1 = PieceProblem.create(PieceId(100L), ProblemId(1001L), Position(1.0))
        val problemDifferentPiece = PieceProblem.create(PieceId(200L), ProblemId(1002L), Position(2.0))

        // When & Then
        val exception = assertThrows<ValidationException> {
            problem1.isAfter(problemDifferentPiece)
        }

        assertTrue(exception.message!!.contains("다른 학습지에 속한 문제와는 위치 비교를 할 수 없습니다"))
    }

    @Test
    fun `복잡한 시나리오 - 연속적인 이동 시뮬레이션`() {
        // Given - 초기 상태: 1(1.0), 2(2.0), 3(3.0), 4(4.0)
        val pieceId = PieceId(100L)
        val problem1 = PieceProblem.create(pieceId, ProblemId(1001L), Position(1.0))
        val problem2 = PieceProblem.create(pieceId, ProblemId(1002L), Position(2.0))
        val problem3 = PieceProblem.create(pieceId, ProblemId(1003L), Position(3.0))
        val problem4 = PieceProblem.create(pieceId, ProblemId(1004L), Position(4.0))

        // When - 4번 문제를 1번과 2번 사이로 이동
        val moved4 = problem4.moveTo(problem1, problem2)

        // Then
        assertEquals(1.5, moved4.position.value)
        assertTrue(moved4.isAfter(problem1))
        assertTrue(moved4.isBefore(problem2))

        // When - 3번 문제를 맨 앞으로 이동
        val moved3 = problem3.moveTo(null, problem1)

        // Then
        assertEquals(0.5, moved3.position.value)
        assertTrue(moved3.isBefore(problem1))

        // When - 2번 문제를 맨 뒤로 이동 (현재 가장 뒤는 3.0)
        val moved2 = problem2.moveTo(problem3, null)

        // Then
        assertEquals(4.0, moved2.position.value)
        assertTrue(moved2.isAfter(problem3))
    }

    @Test
    fun `동일한 위치로 이동하는 경우 - 위치 변경 없음`() {
        // Given
        val problemToMove = PieceProblem.create(
            PieceId(100L),
            ProblemId(1002L),
            Position(2.0)
        )

        val prevProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1001L),
            Position(1.0)
        )

        val nextProblem = PieceProblem.create(
            PieceId(100L),
            ProblemId(1003L),
            Position(3.0)
        )

        // When - 이미 올바른 위치(1과 3 사이)에 있는 문제를 같은 위치로 이동
        val moved = problemToMove.moveTo(prevProblem, nextProblem)

        // Then
        assertEquals(2.0, moved.position.value) // (1.0 + 3.0) / 2 = 2.0 (동일)
    }
}