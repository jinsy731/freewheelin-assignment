package com.freewheelin.pulley.problem.domain.model

import com.freewheelin.pulley.common.domain.Level
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ProblemTest {

    @Test
    fun `문제 생성 성공`() {
        // given
        val id = 1L
        val answer = "42"
        val unitCode = "MATH01"
        val level = 3
        val problemType = ProblemType.SUBJECTIVE

        // when
        val problem = Problem(id, answer, unitCode, level, problemType)

        // then
        assertEquals(id, problem.id)
        assertEquals(answer, problem.answer)
        assertEquals(unitCode, problem.unitCode)
        assertEquals(level, problem.level)
        assertEquals(problemType, problem.problemType)
    }

    @Test
    fun `난이도가 범위를 벗어나면 예외 발생`() {
        // given
        val id = 1L
        val answer = "42"
        val unitCode = "MATH01"
        val invalidLevel = 6
        val problemType = ProblemType.SUBJECTIVE

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Problem(id, answer, unitCode, invalidLevel, problemType)
        }

        assertEquals("문제 난이도는 1~5 사이여야 합니다.", exception.message)
    }

    @Test
    fun `정답이 비어있으면 예외 발생`() {
        // given
        val id = 1L
        val answer = ""
        val unitCode = "MATH01"
        val level = 3
        val problemType = ProblemType.SUBJECTIVE

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Problem(id, answer, unitCode, level, problemType)
        }

        assertEquals("정답은 비어있을 수 없습니다.", exception.message)
    }

    @Test
    fun `유형코드가 비어있으면 예외 발생`() {
        // given
        val id = 1L
        val answer = "42"
        val unitCode = ""
        val level = 3
        val problemType = ProblemType.SUBJECTIVE

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Problem(id, answer, unitCode, level, problemType)
        }

        assertEquals("유형코드는 비어있을 수 없습니다.", exception.message)
    }

    @Test
    fun `getLevel 메서드는 올바른 Level enum을 반환한다`() {
        // given
        val lowLevelProblem = Problem(1L, "42", "MATH01", 1, ProblemType.SUBJECTIVE)
        val middleLevelProblem = Problem(2L, "42", "MATH01", 3, ProblemType.SUBJECTIVE)
        val highLevelProblem = Problem(3L, "42", "MATH01", 5, ProblemType.SUBJECTIVE)

        // when & then
        assertEquals(Level.LOW, lowLevelProblem.getLevel())
        assertEquals(Level.MIDDLE, middleLevelProblem.getLevel())
        assertEquals(Level.HIGH, highLevelProblem.getLevel())
    }

    @Test
    fun `isCorrectAnswer 메서드는 정답 여부를 올바르게 판단한다`() {
        // given
        val problem = Problem(1L, "42", "MATH01", 3, ProblemType.SUBJECTIVE)

        // when & then
        assertTrue(problem.isCorrectAnswer("42"))
        assertTrue(problem.isCorrectAnswer(" 42 ")) // 공백 처리 테스트
        assertTrue(problem.isCorrectAnswer("42")) // 대소문자 무시 테스트
        assertFalse(problem.isCorrectAnswer("43"))
        assertFalse(problem.isCorrectAnswer(""))
    }
}