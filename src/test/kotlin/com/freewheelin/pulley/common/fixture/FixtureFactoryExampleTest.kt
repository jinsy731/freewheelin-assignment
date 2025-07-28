package com.freewheelin.pulley.common.fixture

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * 타입 안전한 Fixture Factory 사용 예시
 */
class FixtureFactoryExampleTest {

    @Test
    fun `타입 안전한 Fixture Factory 사용 테스트`() {
        assertDoesNotThrow {
            // 기본 사용자 생성
            val user = Fixtures.user.create()
            println("Generated user: $user")

            // 여러 사용자 생성
            val users = Fixtures.user.createList(3)
            println("Generated users: $users")

            // 특정 나이를 가진 사용자 생성
            val youngUser = Fixtures.user.withSpecificAge(20)
            println("Young user: $youngUser")

            // 특정 이름을 가진 사용자 생성
            val namedUser = Fixtures.user.withSpecificName("김개발")
            println("Named user: $namedUser")

            // 커스텀 설정으로 사용자 생성
            val customUser = Fixtures.user.customize {
                setValue(UserFixtureFactory.User::name, "박테스터")
                    .setValue(UserFixtureFactory.User::age, 30)
                    .setValue(UserFixtureFactory.User::id, 999L)
            }
            println("Custom user: $customUser")
        }
    }
}