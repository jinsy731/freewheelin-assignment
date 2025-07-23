package com.freewheelin.pulley.common.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class LevelDistributionTest {
    
    @Test
    fun `생성자 - 유효한 비율 합계 테스트`() {
        // Given & When & Then
        // 정상적인 비율 (합계 1.0)
        LevelDistribution(0.2, 0.3, 0.5) // 예외 없이 생성되어야 함
        LevelDistribution(0.25, 0.5, 0.25)
        LevelDistribution(0.5, 0.3, 0.2)
    }
    
    @Test
    fun `생성자 - 유효하지 않은 비율 합계인 경우 IllegalArgumentException 발생`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> { 
            LevelDistribution(0.2, 0.3, 0.6) // 합계 1.1
        }
        assertThrows<IllegalArgumentException> { 
            LevelDistribution(0.1, 0.2, 0.3) // 합계 0.6
        }
    }
    
    @Test
    fun `forLevel - 각 레벨별 올바른 분배 비율 반환 테스트`() {
        // Given & When
        val highDistribution = LevelDistribution.forLevel(Level.HIGH)
        val middleDistribution = LevelDistribution.forLevel(Level.MIDDLE)
        val lowDistribution = LevelDistribution.forLevel(Level.LOW)
        
        // Then
        // HIGH: 하 20%, 중 30%, 상 50%
        assertEquals(0.2, highDistribution.lowRatio)
        assertEquals(0.3, highDistribution.middleRatio)
        assertEquals(0.5, highDistribution.highRatio)
        
        // MIDDLE: 하 25%, 중 50%, 상 25%
        assertEquals(0.25, middleDistribution.lowRatio)
        assertEquals(0.5, middleDistribution.middleRatio)
        assertEquals(0.25, middleDistribution.highRatio)
        
        // LOW: 하 50%, 중 30%, 상 20%
        assertEquals(0.5, lowDistribution.lowRatio)
        assertEquals(0.3, lowDistribution.middleRatio)
        assertEquals(0.2, lowDistribution.highRatio)
    }
    
    @Test
    fun `calculateCounts - 총 문제 수에 따른 난이도별 문제 수 계산 테스트`() {
        // Given
        val highDistribution = LevelDistribution.forLevel(Level.HIGH)
        
        // When
        val counts20 = highDistribution.calculateCounts(20)
        val counts10 = highDistribution.calculateCounts(10)
        
        // Then
        // 20개 문제: 하 4개(20%), 중 6개(30%), 상 10개(50%)
        assertEquals(4, counts20.lowCount)
        assertEquals(6, counts20.middleCount)
        assertEquals(10, counts20.highCount)
        assertEquals(20, counts20.total)
        
        // 10개 문제: 하 2개(20%), 중 3개(30%), 상 5개(50%)
        assertEquals(2, counts10.lowCount)
        assertEquals(3, counts10.middleCount)
        assertEquals(5, counts10.highCount)
        assertEquals(10, counts10.total)
    }
    
    @Test
    fun `calculateCounts - 나머지가 있는 경우 상 난이도에 할당 테스트`() {
        // Given
        val distribution = LevelDistribution.forLevel(Level.HIGH)
        
        // When
        val counts = distribution.calculateCounts(13) // 13개로 나누어떨어지지 않는 수
        
        // Then
        // 하: 13 * 0.2 = 2.6 -> 2개
        // 중: 13 * 0.3 = 3.9 -> 3개
        // 상: 13 - 2 - 3 = 8개 (나머지 할당)
        assertEquals(2, counts.lowCount)
        assertEquals(3, counts.middleCount)
        assertEquals(8, counts.highCount)
        assertEquals(13, counts.total)
    }
} 