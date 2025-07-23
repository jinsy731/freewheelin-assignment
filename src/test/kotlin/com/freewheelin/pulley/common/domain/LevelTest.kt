package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class LevelTest {
    
    @Test
    fun `fromLevel - 유효한 레벨 변환 테스트`() {
        // Given & When & Then
        assertEquals(Level.LOW, Level.fromLevel(1))
        assertEquals(Level.MIDDLE, Level.fromLevel(2))
        assertEquals(Level.MIDDLE, Level.fromLevel(3))
        assertEquals(Level.MIDDLE, Level.fromLevel(4))
        assertEquals(Level.HIGH, Level.fromLevel(5))
    }
    
    @ParameterizedTest
    @ValueSource(ints = [0, 6, -1, 10])
    fun `fromLevel - 유효하지 않은 레벨 예외 테스트`(levelNumber: Int) {
        // Given & When & Then
        assertThrows<ValidationException> { Level.fromLevel(levelNumber) }
    }
    
    @Test
    fun `getAllLevels - 모든 난이도 레벨 반환 테스트`() {
        // Given & When
        val allLevels = Level.getAllLevels()
        
        // Then
        assertEquals(listOf(1, 2, 3, 4, 5), allLevels)
    }
    
    @Test
    fun `contains - 레벨 포함 여부 확인 테스트`() {
        // Given & When & Then
        assertEquals(true, Level.LOW.contains(1))
        assertEquals(false, Level.LOW.contains(2))
        
        assertEquals(true, Level.MIDDLE.contains(2))
        assertEquals(true, Level.MIDDLE.contains(3))
        assertEquals(true, Level.MIDDLE.contains(4))
        assertEquals(false, Level.MIDDLE.contains(1))
        assertEquals(false, Level.MIDDLE.contains(5))
        
        assertEquals(true, Level.HIGH.contains(5))
        assertEquals(false, Level.HIGH.contains(4))
    }
} 