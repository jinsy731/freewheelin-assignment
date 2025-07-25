package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PieceProblemsTest {

    private val pieceId = PieceId(100L)

    private fun createTestProblem(id: Long, problemId: Long, position: Double): PieceProblem {
        return PieceProblem(
            id = id,
            pieceId = pieceId,
            problemId = ProblemId(problemId),
            position = Position(position)
        )
    }

    @Test
    fun `정상 생성 - 빈 컬렉션`() {
        // When
        val pieceProblems = PieceProblems.empty()

        // Then
        assertTrue(pieceProblems.isEmpty())
        assertEquals(0, pieceProblems.size())
    }

    @Test
    fun `정상 생성 - 단일 문제`() {
        // Given
        val problem = createTestProblem(1L, 101L, 1.0)

        // When
        val pieceProblems = PieceProblems.of(problem)

        // Then
        assertEquals(1, pieceProblems.size())
        assertEquals(problem, pieceProblems.findByPieceProblemId(1L))
        assertEquals(problem, pieceProblems.findByProblemId(101L)) // 기존 호환성
    }

    @Test
    fun `정상 생성 - 여러 문제`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0)
        )

        // When
        val pieceProblems = PieceProblems.of(problems)

        // Then
        assertEquals(3, pieceProblems.size())
        assertNotNull(pieceProblems.findByPieceProblemId(1L))
        assertNotNull(pieceProblems.findByPieceProblemId(2L))
        assertNotNull(pieceProblems.findByPieceProblemId(3L))
    }

    @Test
    fun `PieceProblem ID로 문제 찾기 - findByPieceProblemId`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When & Then
        assertNotNull(pieceProblems.findByPieceProblemId(1L))
        assertNotNull(pieceProblems.findByPieceProblemId(2L))
        assertNull(pieceProblems.findByPieceProblemId(999L))
    }

    @Test
    fun `PieceProblem ID로 문제 찾기 - getByPieceProblemId (성공)`() {
        // Given
        val problem = createTestProblem(1L, 101L, 1.0)
        val pieceProblems = PieceProblems.of(problem)

        // When
        val found = pieceProblems.getByPieceProblemId(1L)

        // Then
        assertEquals(problem, found)
    }

    @Test
    fun `PieceProblem ID로 문제 찾기 - getByPieceProblemId (실패)`() {
        // Given
        val pieceProblems = PieceProblems.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            pieceProblems.getByPieceProblemId(999L)
        }

        assertTrue(exception.message!!.contains("PieceProblem을 찾을 수 없습니다: 999"))
    }

    @Test
    fun `연속성 검증 성공 - 연속된 문제들`() {
        // Given: 1(1.0), 2(2.0), 3(3.0)
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        val prevProblem = pieceProblems.getByPieceProblemId(1L)
        val nextProblem = pieceProblems.getByPieceProblemId(2L)

        // When & Then - 예외 발생하지 않음
        pieceProblems.validateConsecutiveness(prevProblem, nextProblem, 3L)
    }

    @Test
    fun `연속성 검증 성공 - prev 또는 next가 null`() {
        // Given
        val problems = listOf(createTestProblem(1L, 101L, 1.0))
        val pieceProblems = PieceProblems.of(problems)

        // When & Then - null인 경우 검증 스킵
        pieceProblems.validateConsecutiveness(null, null, 1L)
        pieceProblems.validateConsecutiveness(null, problems[0], 1L)
        pieceProblems.validateConsecutiveness(problems[0], null, 1L)
    }

    @Test
    fun `연속성 검증 실패 - 순서가 잘못된 경우`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 3.0), // 순서가 뒤바뀜
            createTestProblem(2L, 102L, 1.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        val prevProblem = pieceProblems.getByPieceProblemId(1L) // position 3.0
        val nextProblem = pieceProblems.getByPieceProblemId(2L) // position 1.0

        // When & Then
        val exception = assertThrows<ValidationException> {
            pieceProblems.validateConsecutiveness(prevProblem, nextProblem, 999L)
        }

        assertTrue(exception.message!!.contains("이전 문제의 위치가 다음 문제의 위치보다 뒤에 있습니다"))
    }

    @Test
    fun `연속성 검증 실패 - 사이에 다른 문제가 있는 경우`() {
        // Given: 1(1.0), 2(2.0), 3(3.0) - 1과 3 사이에 2가 있음
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0),
            createTestProblem(4L, 104L, 4.0) // 이동할 문제
        )
        val pieceProblems = PieceProblems.of(problems)

        val prevProblem = pieceProblems.getByPieceProblemId(1L)
        val nextProblem = pieceProblems.getByPieceProblemId(3L)

        // When & Then
        val exception = assertThrows<ValidationException> {
            pieceProblems.validateConsecutiveness(prevProblem, nextProblem, 4L)
        }

        assertTrue(exception.message!!.contains("연속되어 있지 않습니다"))
        assertTrue(exception.message!!.contains("2"))
    }

    @Test
    fun `순서 변경 성공 - 연속된 두 문제 사이로 이동 (PieceProblem ID 기반)`() {
        // Given: 1(1.0), 2(2.0), 3(3.0), 4(4.0)
        // 4를 1과 2 사이로 이동
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0),
            createTestProblem(4L, 104L, 4.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When
        val result = pieceProblems.moveOrderTo(4L, 1L, 2L)

        // Then
        assertNotNull(result)
        assertEquals(4L, result!!.id)
        assertEquals(1.5, result.position.value) // (1.0 + 2.0) / 2
    }

    @Test
    fun `순서 변경 성공 - 맨 앞으로 이동 (PieceProblem ID 기반)`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When - 2를 맨 앞으로
        val result = pieceProblems.moveOrderTo(2L, null, 1L)

        // Then
        assertNotNull(result)
        assertEquals(0.5, result!!.position.value) // 1.0 / 2
    }

    @Test
    fun `순서 변경 성공 - 맨 뒤로 이동 (PieceProblem ID 기반)`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When - 1을 맨 뒤로
        val result = pieceProblems.moveOrderTo(1L, 2L, null)

        // Then
        assertNotNull(result)
        assertEquals(3.0, result!!.position.value) // 2.0 + 1.0
    }

    @Test
    fun `순서 변경 스킵 - 이미 올바른 위치에 있는 경우`() {
        // Given: 1(1.0), 2(2.0), 3(3.0)
        // 2는 이미 1과 3 사이에 있음
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When
        val result = pieceProblems.moveOrderTo(2L, 1L, 3L)

        // Then
        assertNull(result) // 이동 불필요
    }

    @Test
    fun `순서 변경 실패 - 존재하지 않는 PieceProblem`() {
        // Given
        val pieceProblems = PieceProblems.empty()

        // When & Then
        assertThrows<NotFoundException> {
            pieceProblems.moveOrderTo(999L, null, null)
        }
    }

    @Test
    fun `순서 변경 실패 - 연속성 검증 실패`() {
        // Given: 1(1.0), 2(2.0), 3(3.0), 4(4.0)
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0),
            createTestProblem(4L, 104L, 4.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When & Then - 1과 3은 연속되지 않음 (2가 사이에 있음)
        val exception = assertThrows<ValidationException> {
            pieceProblems.moveOrderTo(4L, 1L, 3L)
        }

        assertTrue(exception.message!!.contains("연속되어 있지 않습니다"))
    }

    @Test
    fun `기존 호환성 - Problem ID 기반 메소드들 (Deprecated)`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0),
            createTestProblem(3L, 103L, 3.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When & Then - 기존 Problem ID 기반 메소드들이 여전히 작동
        assertEquals(problems[0], pieceProblems.getByProblemId(101L))
        assertEquals(problems[1], pieceProblems.getByProblemId(102L))
        assertEquals(problems[2], pieceProblems.getByProblemId(103L))

        // Deprecated 메소드도 작동
        @Suppress("DEPRECATION")
        val result = pieceProblems.moveOrderToByProblemId(103L, 101L, 102L)
        assertNotNull(result)
        assertEquals(1.5, result!!.position.value)
    }

    @Test
    fun `toList - 불변 리스트 반환`() {
        // Given
        val problems = listOf(
            createTestProblem(1L, 101L, 1.0),
            createTestProblem(2L, 102L, 2.0)
        )
        val pieceProblems = PieceProblems.of(problems)

        // When
        val result = pieceProblems.toList()

        // Then
        assertEquals(2, result.size)
        assertEquals(problems, result)
    }
}