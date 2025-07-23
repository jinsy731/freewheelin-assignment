package com.freewheelin.pulley.common.exception

/**
 * 데이터 검증 실패 시 발생하는 예외
 *
 * 입력값이나 데이터의 형식, 범위 등의 검증이 실패할 때 사용됩니다.
 */
class ValidationException(
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null,
    override val context: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause) {

    constructor(
        errorCode: ErrorCode,
        fieldName: String,
        invalidValue: Any?,
        reason: String? = null
    ) : this(
        errorCode = errorCode,
        message = "${errorCode.defaultMessage} 필드: $fieldName, 값: $invalidValue${reason?.let { " - $it" } ?: ""}",
        context = mapOf(
            "field" to fieldName,
            "value" to (invalidValue ?: "null"),
            "reason" to (reason ?: "")
        )
    )
}