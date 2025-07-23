package com.freewheelin.pulley.common.exception

/**
 * 권한이 없어 접근이 거부될 때 발생하는 예외
 * 
 * 주로 리소스 소유권 검증 실패 시 사용됩니다.
 */
class AuthorizationException(
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {
    
    constructor(errorCode: ErrorCode, userId: Long, resourceType: String, resourceId: Any) : this(
        errorCode = errorCode,
        message = "${errorCode.defaultMessage} (사용자: $userId, 리소스: $resourceType[$resourceId])"
    )
} 