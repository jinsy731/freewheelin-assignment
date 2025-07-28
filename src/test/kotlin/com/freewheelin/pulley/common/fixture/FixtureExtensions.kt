package com.freewheelin.pulley.common.fixture

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import kotlin.reflect.KProperty1

/**
 * Fixture Monkey 확장 함수들
 */

inline fun <reified T> FixtureMonkey.giveMeOne(): T = this.giveMeOne(T::class.java)

inline fun <reified T> FixtureMonkey.giveMeBuilder(): ArbitraryBuilder<T> = this.giveMeBuilder(T::class.java)

inline fun <reified T> FixtureMonkey.giveMe(size: Int): List<T> = this.giveMe(T::class.java, size)

/**
 * 전역 Fixture Monkey 인스턴스를 사용하는 편의 함수들
 */
inline fun <reified T> giveMeOne(): T = FixtureMonkeyConfig.fixtureMonkey.giveMeOne<T>()

inline fun <reified T> giveMeBuilder(): ArbitraryBuilder<T> = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder<T>()

inline fun <reified T> giveMe(size: Int): List<T> = FixtureMonkeyConfig.fixtureMonkey.giveMe<T>(size)

/**
 * 커스텀 제너레이터를 사용하는 편의 함수들
 */
inline fun <reified T> giveMeOneWith(block: ArbitraryBuilder<T>.() -> ArbitraryBuilder<T>): T {
    return giveMeBuilder<T>().block().sample()
}

/**
 * 타입 안전한 프로퍼티 설정을 위한 확장 함수들
 */
fun <T, R> ArbitraryBuilder<T>.withKoreanName(property: KProperty1<T, R>): ArbitraryBuilder<T> = 
    this.set(property.name, CustomGenerators.koreanName())

fun <T, R> ArbitraryBuilder<T>.withEmail(property: KProperty1<T, R>): ArbitraryBuilder<T> = 
    this.set(property.name, CustomGenerators.email())

/**
 * 값 설정을 위한 타입 안전한 확장 함수
 */
fun <T, R> ArbitraryBuilder<T>.setValue(property: KProperty1<T, R>, value: R): ArbitraryBuilder<T> = 
    this.set(property.name, value)