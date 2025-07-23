package com.freewheelin.pulley.problem.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class UnitCodeTest {

    @Test
    fun `유닛 코드 생성 성공`() {
        // given
        val id = 1
        val unitCode = "MATH01"
        val name = "기초 수학"

        // when
        val unitCodeObj = UnitCode(id, unitCode, name)

        // then
        assertEquals(id, unitCodeObj.id)
        assertEquals(unitCode, unitCodeObj.unitCode)
        assertEquals(name, unitCodeObj.name)
    }

    @Test
    fun `유닛 코드가 비어있으면 예외 발생`() {
        // given
        val id = 1
        val unitCode = ""
        val name = "기초 수학"

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            UnitCode(id, unitCode, name)
        }

        assertEquals("유닛 코드는 비어있을 수 없습니다.", exception.message)
    }

    @Test
    fun `유닛 이름이 비어있으면 예외 발생`() {
        // given
        val id = 1
        val unitCode = "MATH01"
        val name = ""

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            UnitCode(id, unitCode, name)
        }

        assertEquals("유닛 이름은 비어있을 수 없습니다.", exception.message)
    }
}