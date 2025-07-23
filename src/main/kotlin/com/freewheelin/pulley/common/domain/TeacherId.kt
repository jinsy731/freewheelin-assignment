package com.freewheelin.pulley.common.domain

/**
 * 선생님 식별자 값객체
 * 
 * 타입 안전성을 제공하고 다른 ID와의 혼동을 방지합니다.
 */
@JvmInline
value class TeacherId(val value: Long) {
    init {
        require(value >= 0) { "선생님 ID는 0 이상이어야 합니다." }
    }
    
    override fun toString(): String = value.toString()
} 