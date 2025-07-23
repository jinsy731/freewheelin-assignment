package com.freewheelin.pulley.problem.domain.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProblemTypeTest {

    @Test
    fun `ProblemType enum 값 확인`() {
        // given & when & then
        assertEquals(2, ProblemType.values().size)
        assertEquals("SUBJECTIVE", ProblemType.SUBJECTIVE.name)
        assertEquals("SELECTION", ProblemType.SELECTION.name)
    }
}