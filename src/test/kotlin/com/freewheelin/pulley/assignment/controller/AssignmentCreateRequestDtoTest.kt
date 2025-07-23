package com.freewheelin.pulley.assignment.controller

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AssignmentCreateRequestDtoTest {
    
    @Test
    fun `생성자 - 정상 생성 테스트`() {
        // Given & When & Then
        AssignmentCreateRequestDto(
            studentIds = listOf(10L, 20L, 30L)
        )
    }
    
    @Test
    fun `생성자 - 단일 학생으로 정상 생성`() {
        // Given & When & Then
        AssignmentCreateRequestDto(
            studentIds = listOf(10L)
        )
    }
    
    @Test
    fun `toCommand - 정상 변환 테스트`() {
        // Given
        val dto = AssignmentCreateRequestDto(
            studentIds = listOf(10L, 20L, 30L)
        )
        val teacherId = 1L
        val pieceId = 100L
        
        // When
        val applicationRequest = dto.toCommand(teacherId, pieceId)
        
        // Then
        assertEquals(1L, applicationRequest.teacherId)
        assertEquals(100L, applicationRequest.pieceId)
        assertEquals(listOf(10L, 20L, 30L), applicationRequest.studentIds)
    }
    
    @Test
    fun `toCommand - 단일 학생 변환 테스트`() {
        // Given
        val dto = AssignmentCreateRequestDto(
            studentIds = listOf(10L)
        )
        val teacherId = 1L
        val pieceId = 100L
        
        // When
        val applicationRequest = dto.toCommand(teacherId, pieceId)
        
        // Then
        assertEquals(1L, applicationRequest.teacherId)
        assertEquals(100L, applicationRequest.pieceId)
        assertEquals(listOf(10L), applicationRequest.studentIds)
    }
} 