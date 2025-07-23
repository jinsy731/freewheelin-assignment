package com.freewheelin.pulley.common.domain

/**
 * 문제 개수 값객체
 * 
 * 1~50개 범위의 문제 개수를 나타내며, 도메인 규칙을 포함합니다.
 */
@JvmInline
value class ProblemCount(val value: Int) {
    init {
        require(value in 1..50) { "문제 수는 1~${50}개 사이여야 합니다: $value" }
    }
} 