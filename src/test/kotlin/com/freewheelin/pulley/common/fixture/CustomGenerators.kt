package com.freewheelin.pulley.common.fixture

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary

object CustomGenerators {
    
    fun koreanName(): Arbitrary<String> = 
        Arbitraries.of("김철수", "이영희", "박민수", "최지영", "정다은", "한상우", "임수진", "조현민")
    
    fun email(): Arbitrary<String> = 
        Arbitraries.strings()
            .alpha()
            .ofMinLength(5)
            .ofMaxLength(10)
            .map { "${it}@example.com" }
}