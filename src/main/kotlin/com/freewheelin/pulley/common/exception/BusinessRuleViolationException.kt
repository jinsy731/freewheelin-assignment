package com.freewheelin.pulley.common.exception

import kotlin.let

/**
 * 비즈니스 규칙 위반 시 발생하는 예외
 * 
 * 도메인 로직의 불변 조건이나 비즈니스 규칙이 위반될 때 사용됩니다.
 */
class BusinessRuleViolationException(
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {
    
    constructor(errorCode: ErrorCode, rule: String, details: String? = null) : this(
        errorCode = errorCode,
        message = "${errorCode.defaultMessage} 규칙: $rule${details?.let { " - $it" } ?: ""}"
    )
} 