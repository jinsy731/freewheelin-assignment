package com.freewheelin.pulley.common.exception

import kotlin.let
import kotlin.to

/**
 * 객체의 현재 상태에서 요청된 작업을 수행할 수 없을 때 발생하는 예외
 * 
 * 상태 전이 규칙 위반이나 상태 불일치 시 사용됩니다.
 */
class InvalidStateException(
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null,
    override val context: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause) {
    
    constructor(
        errorCode: ErrorCode,
        currentState: String,
        requestedAction: String,
        expectedState: String? = null
    ) : this(
        errorCode = errorCode,
        message = "${errorCode.defaultMessage} 현재 상태: $currentState, 요청 작업: $requestedAction${expectedState?.let { ", 필요 상태: $it" } ?: ""}",
        context = mapOf(
            "currentState" to currentState,
            "requestedAction" to requestedAction,
            "expectedState" to (expectedState ?: "")
        )
    )
} 