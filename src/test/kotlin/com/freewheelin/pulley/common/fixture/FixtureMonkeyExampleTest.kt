package com.freewheelin.pulley.common.fixture

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Fixture Monkey 사용 예시 테스트
 */
class FixtureMonkeyExampleTest {

    data class SampleDto(
        val id: Long,
        val name: String,
        val email: String,
        val age: Int,
        val description: String
    )

    @Test
    fun `Fixture Monkey로 객체 생성 테스트`() {
        assertDoesNotThrow {
            // 기본 객체 생성
            val sample = giveMeOne<SampleDto>()
            println("Generated sample: $sample")

            // 여러 객체 생성
            val samples = giveMe<SampleDto>(3)
            println("Generated samples: $samples")

            // 타입 안전한 커스텀 제너레이터를 사용한 객체 생성
            val customSample = giveMeBuilder<SampleDto>()
                .withKoreanName(SampleDto::name)
                .withEmail(SampleDto::email)
                .sample()
            println("Custom sample with generators: $customSample")

            // 특정 값 설정과 제너레이터 혼합 사용 (타입 안전)
            val mixedSample = giveMeOneWith<SampleDto> {
                withKoreanName(SampleDto::name)
                    .withEmail(SampleDto::email)
                    .setValue(SampleDto::age, 25)
                    .setValue(SampleDto::id, 12345L)
            }
            println("Mixed sample: $mixedSample")
        }
    }
}