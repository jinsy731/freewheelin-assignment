package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProblemOrderUpdateRequestDtoTest {
    
    @Test
    fun `정상 생성 - 모든 값이 유효한 경우`() {
        // Given & When
        val dto = ProblemOrderUpdateRequestDto(
            problemId = 1001L,
            prevProblemId = 1002L,
            nextProblemId = 1003L
        )
        
        // Then
        assertEquals(1001L, dto.problemId)
        assertEquals(1002L, dto.prevProblemId)
        assertEquals(1003L, dto.nextProblemId)
    }
    
    @Test
    fun `정상 생성 - prevProblemId와 nextProblemId가 null인 경우`() {
        // Given & When
        val dto = ProblemOrderUpdateRequestDto(
            problemId = 1001L,
            prevProblemId = null,
            nextProblemId = null
        )
        
        // Then
        assertEquals(1001L, dto.problemId)
        assertNull(dto.prevProblemId)
        assertNull(dto.nextProblemId)
    }
    
    @Test
    fun `예외 발생 - prevProblemId가 problemId와 같은 경우`() {
        // Given & When & Then
        val exception = assertThrows<ValidationException> {
            ProblemOrderUpdateRequestDto(
                problemId = 1001L,
                prevProblemId = 1001L,  // problemId와 동일
                nextProblemId = 1003L
            )
        }
        
        assertEquals("입력값 검증에 실패했습니다. 필드: prevProblemId, 값: 1001 - 자기 자신을 이전 문제로 지정할 수 없습니다", exception.message)
    }
    
    @Test
    fun `예외 발생 - nextProblemId가 problemId와 같은 경우`() {
        // Given & When & Then
        val exception = assertThrows<ValidationException> {
            ProblemOrderUpdateRequestDto(
                problemId = 1001L,
                prevProblemId = 1002L,
                nextProblemId = 1001L  // problemId와 동일
            )
        }
        
        assertEquals("입력값 검증에 실패했습니다. 필드: nextProblemId, 값: 1001 - 자기 자신을 다음 문제로 지정할 수 없습니다", exception.message)
    }
    
    @Test
    fun `예외 발생 - prevProblemId와 nextProblemId가 같은 경우`() {
        // Given & When & Then
        val exception = assertThrows<ValidationException> {
            ProblemOrderUpdateRequestDto(
                problemId = 1001L,
                prevProblemId = 1002L,
                nextProblemId = 1002L  // prevProblemId와 동일
            )
        }
        
        assertEquals("입력값 검증에 실패했습니다. 필드: prevProblemId, nextProblemId, 값: 1002, 1002 - 이전 문제와 다음 문제가 같을 수 없습니다", exception.message)
    }
    
    @Test
    fun `toApplicationCommand - 모든 값이 존재하는 경우`() {
        // Given
        val dto = ProblemOrderUpdateRequestDto(
            problemId = 1001L,
            prevProblemId = 1002L,
            nextProblemId = 1003L
        )
        val pieceId = 100L
        val teacherId = 1L
        
        // When
        val command = dto.toApplicationCommand(pieceId, teacherId)
        
        // Then
        assertEquals(100L, command.pieceId)
        assertEquals(1L, command.teacherId)
        assertEquals(1001L, command.problemId)
        assertEquals(1002L, command.prevProblemId)
        assertEquals(1003L, command.nextProblemId)
    }
    
    @Test
    fun `toApplicationCommand - null 값이 포함된 경우`() {
        // Given
        val dto = ProblemOrderUpdateRequestDto(
            problemId = 1001L,
            prevProblemId = null,
            nextProblemId = null
        )
        val pieceId = 100L
        val teacherId = 1L
        
        // When
        val command = dto.toApplicationCommand(pieceId, teacherId)
        
        // Then
        assertEquals(100L, command.pieceId)
        assertEquals(1L, command.teacherId)
        assertEquals(1001L, command.problemId)
        assertNull(command.prevProblemId)
        assertNull(command.nextProblemId)
    }
} 