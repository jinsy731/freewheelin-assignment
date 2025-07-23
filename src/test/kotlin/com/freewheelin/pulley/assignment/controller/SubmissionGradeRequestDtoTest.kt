package com.freewheelin.pulley.assignment.controller

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubmissionGradeRequestDtoTest {
    
    @Test
    fun `생성자 - 정상 생성 테스트`() {
        // Given & When & Then
        SubmissionGradeRequestDto(
            answers = listOf(
                AnswerSubmissionDto(1001L, "10"),
                AnswerSubmissionDto(1002L, "20")
            )
        )
    }
    
    @Test
    fun `toApplicationRequest - 정상 변환 테스트`() {
        // Given
        val dto = SubmissionGradeRequestDto(
            answers = listOf(
                AnswerSubmissionDto(1001L, "10"),
                AnswerSubmissionDto(1002L, "20"),
                AnswerSubmissionDto(1003L, " 답안 ")  // 공백 포함
            )
        )
        val pieceId = 100L
        val studentId = 10L
        
        // When
        val applicationRequest = dto.toApplicationRequest(pieceId, studentId)
        
        // Then
        assertEquals(100L, applicationRequest.pieceId)
        assertEquals(10L, applicationRequest.studentId)
        assertEquals(3, applicationRequest.answers.size)

        assertEquals(1001L, applicationRequest.answers[0].problemId)
        assertEquals("10", applicationRequest.answers[0].answer)

        assertEquals(1002L, applicationRequest.answers[1].problemId)
        assertEquals("20", applicationRequest.answers[1].answer)

        assertEquals(1003L, applicationRequest.answers[2].problemId)
        assertEquals("답안", applicationRequest.answers[2].answer)  // 공백 제거됨
    }
    
    @Test
    fun `AnswerSubmissionDto toApplicationObject - 공백 제거 테스트`() {
        // Given
        val dto = AnswerSubmissionDto(1001L, "  답안  ")
        
        // When
        val applicationObject = dto.toApplicationObject()
        
        // Then
        assertEquals(1001L, applicationObject.problemId)
        assertEquals("답안", applicationObject.answer)  // 앞뒤 공백 제거
    }
    
    @Test
    fun `AnswerSubmissionDto - 단일 답안 변환 테스트`() {
        // Given
        val dto = AnswerSubmissionDto(1001L, "정답")
        
        // When
        val applicationObject = dto.toApplicationObject()
        
        // Then
        assertEquals(1001L, applicationObject.problemId)
        assertEquals("정답", applicationObject.answer)
    }
} 