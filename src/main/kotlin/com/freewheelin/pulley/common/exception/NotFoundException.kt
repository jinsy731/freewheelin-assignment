package com.freewheelin.pulley.common.exception

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 * 
 * 주로 Repository에서 엔티티 조회 실패 시 사용됩니다.
 */
class NotFoundException(
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {
    
    constructor(errorCode: ErrorCode, resourceId: Any) : this(
        errorCode = errorCode,
        message = "${errorCode.defaultMessage} (ID: $resourceId)"
    )
} 