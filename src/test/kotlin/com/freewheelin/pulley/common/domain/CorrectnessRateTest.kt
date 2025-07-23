package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.assignment.domain.model.Submission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * CorrectnessRate 값객체 단위 테스트
 */
class CorrectnessRateTest {
    
    @Test
    fun `정답률 생성 - 유효한 값으로 생성 성공`() {
        val rate = CorrectnessRate(0.75)
        assertEquals(0.75, rate.value)
    }
    
    @Test
    fun `정답률 생성 - 0점으로 생성 성공`() {
        val rate = CorrectnessRate(0.0)
        assertEquals(0.0, rate.value)
    }
    
    @Test
    fun `정답률 생성 - 만점으로 생성 성공`() {
        val rate = CorrectnessRate(1.0)
        assertEquals(1.0, rate.value)
    }
    
    @Test
    fun `정답률 생성 - 범위 초과시 예외 발생`() {
        assertThrows<IllegalArgumentException> {
            CorrectnessRate(1.1)
        }
    }
    
    @Test
    fun `정답률 생성 - 음수값일 때 예외 발생`() {
        assertThrows<IllegalArgumentException> {
            CorrectnessRate(-0.1)
        }
    }
    
    @Test
    fun `백분율 변환`() {
        val rate = CorrectnessRate(0.75)
        assertEquals(75.0, rate.toPercentage())
    }
    
    @Test
    fun `제출 목록으로부터 정답률 계산 - 정상 케이스`() {
        val submissions = listOf(
            createSubmission(1L, true),
            createSubmission(2L, false),
            createSubmission(3L, true),
            createSubmission(4L, true)
        )
        
        val rate = CorrectnessRate.calculate(submissions)
        assertEquals(0.75, rate.value)
    }
    
    @Test
    fun `제출 목록으로부터 정답률 계산 - 빈 목록`() {
        val rate = CorrectnessRate.calculate(emptyList())
        assertEquals(0.0, rate.value)
    }
    
    @Test
    fun `제출 목록으로부터 정답률 계산 - 모두 정답`() {
        val submissions = listOf(
            createSubmission(1L, true),
            createSubmission(2L, true)
        )
        
        val rate = CorrectnessRate.calculate(submissions)
        assertEquals(1.0, rate.value)
    }
    
    @Test
    fun `제출 목록으로부터 정답률 계산 - 모두 오답`() {
        val submissions = listOf(
            createSubmission(1L, false),
            createSubmission(2L, false)
        )
        
        val rate = CorrectnessRate.calculate(submissions)
        assertEquals(0.0, rate.value)
    }
    
    private fun createSubmission(id: Long, isCorrect: Boolean): Submission {
        return Submission(
            id = id,
            assignmentId = 1L,
            problemId = 1L,
            answer = "test",
            isCorrect = isCorrect,
        )
    }
} 