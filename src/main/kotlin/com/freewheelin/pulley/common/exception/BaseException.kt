package com.freewheelin.pulley.common.exception

/**
 * 모든 비즈니스 예외의 기본 클래스
 *
 * ErrorCode를 통해 예외를 구분하고, 일관된 예외 처리를 위한 기반을 제공합니다.
 */
abstract class BaseException(
    val errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    /**
     * 추가 컨텍스트 정보를 담을 수 있는 필드
     */
    open val context: Map<String, Any> = emptyMap()

    override fun toString(): String {
        return "${this::class.simpleName}(errorCode=$errorCode, message='$message')"
    }
}