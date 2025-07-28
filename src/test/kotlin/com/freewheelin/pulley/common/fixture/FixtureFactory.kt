package com.freewheelin.pulley.common.fixture

import com.navercorp.fixturemonkey.ArbitraryBuilder

/**
 * 타입 안전한 Fixture 팩토리 클래스들
 * 각 도메인 객체별로 전용 팩토리를 만들어 타입 안정성과 재사용성을 높입니다.
 */

abstract class FixtureFactory<T> {
    protected abstract fun builder(): ArbitraryBuilder<T>
    
    fun create(): T = builder().sample()
    
    fun createList(size: Int): List<T> = (1..size).map { create() }
    
    fun customize(block: ArbitraryBuilder<T>.() -> ArbitraryBuilder<T>): T {
        return builder().block().sample()
    }
}

/**
 * 사용 예시: User 도메인을 위한 팩토리
 */
class UserFixtureFactory : FixtureFactory<UserFixtureFactory.User>() {
    
    data class User(
        val id: Long,
        val name: String,
        val email: String,
        val age: Int
    )
    
    override fun builder(): ArbitraryBuilder<User> {
        return giveMeBuilder<User>()
            .withKoreanName(User::name)
            .withEmail(User::email)
    }
    
    fun withSpecificAge(age: Int): User {
        return customize { setValue(User::age, age) }
    }
    
    fun withSpecificName(name: String): User {
        return customize { setValue(User::name, name) }
    }
}

/**
 * 전역 팩토리 인스턴스들
 */
object Fixtures {
    val user = UserFixtureFactory()
}