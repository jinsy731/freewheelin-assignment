package com.freewheelin.pulley.common.presentation

import com.freewheelin.pulley.common.exception.BaseException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.ConstraintViolationException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime
import kotlin.collections.map
import kotlin.text.removePrefix

private val logger = KotlinLogging.logger {}

/**
 * 전역 예외 처리기
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    /**
     * BaseException 및 모든 커스텀 예외 처리
     */
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        ex: BaseException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        // 예외 타입에 따른 로그 레벨 결정
        when (ex.errorCode.httpStatus) {
            HttpStatus.INTERNAL_SERVER_ERROR -> {
                logger.error(ex) { 
                    "서버 내부 오류 발생 - path: $requestPath, errorCode: ${ex.errorCode.code}, message: ${ex.message}" 
                }
            }
            HttpStatus.FORBIDDEN -> {
                logger.warn { 
                    "권한 없는 접근 시도 - path: $requestPath, errorCode: ${ex.errorCode.code}, message: ${ex.message}" 
                }
            }
            HttpStatus.NOT_FOUND -> {
                logger.info { 
                    "리소스 찾을 수 없음 - path: $requestPath, errorCode: ${ex.errorCode.code}, message: ${ex.message}" 
                }
            }
            else -> {
                logger.warn { 
                    "비즈니스 예외 발생 - path: $requestPath, errorCode: ${ex.errorCode.code}, message: ${ex.message}" 
                }
            }
        }
        
        val errorResponse = ErrorResponse.from(ex, requestPath)
        return ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "입력값 검증 실패 - path: $requestPath, errorCode: ${ex.errorCode.code}, " +
            "message: ${ex.message}, context: ${ex.context}" 
        }
        
        val errorResponse = ErrorResponse.from(ex, requestPath)
            .copy(details = ex.context.map { "${it.key}: ${it.value}" })
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Spring Validation 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        val errors = ex.bindingResult.fieldErrors.map { 
            "${it.field}: ${it.defaultMessage}" 
        }
        
        logger.warn { 
            "Spring 검증 실패 - path: $requestPath, 검증 오류: $errors" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.VALIDATION_FAILED.code,
            message = "입력값 검증에 실패했습니다",
            details = errors,
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * Jakarta Validation ConstraintViolationException 처리
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        val errors = ex.constraintViolations.map { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }
        
        logger.warn { 
            "제약조건 위반 - path: $requestPath, 제약조건 오류: $errors" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.VALIDATION_FAILED.code,
            message = "입력값 검증에 실패했습니다",
            details = errors,
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameterException(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "필수 파라미터 누락 - path: $requestPath, parameter: ${ex.parameterName}, type: ${ex.parameterType}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.VALIDATION_FAILED.code,
            message = "필수 파라미터가 누락되었습니다",
            details = listOf("${ex.parameterName}: 필수 파라미터입니다"),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "파라미터 타입 불일치 - path: $requestPath, parameter: ${ex.name}, " +
            "value: ${ex.value}, requiredType: ${ex.requiredType?.simpleName}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.VALIDATION_FAILED.code,
            message = "파라미터 타입이 잘못되었습니다",
            details = listOf("${ex.name}: 올바른 형식으로 입력해주세요"),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * JSON 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "JSON 파싱 실패 - path: $requestPath, message: ${ex.message}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.INVALID_INPUT.code,
            message = "JSON 형식이 올바르지 않습니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 지원하지 않는 미디어 타입 예외 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaTypeException(
        ex: HttpMediaTypeNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "지원하지 않는 미디어 타입 - path: $requestPath, contentType: ${ex.contentType}, " +
            "supportedTypes: ${ex.supportedMediaTypes}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.INVALID_INPUT.code,
            message = "지원하지 않는 미디어 타입입니다",
            details = listOf("Content-Type: application/json이 필요합니다"),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse)
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "잘못된 인수 예외 - path: $requestPath, message: ${ex.message}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.INVALID_INPUT.code,
            message = ex.message ?: "잘못된 요청입니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.warn { 
            "잘못된 상태 예외 - path: $requestPath, message: ${ex.message}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.INVALID_STATE.code,
            message = ex.message ?: "현재 상태에서는 해당 작업을 수행할 수 없습니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(
        ex: AuthorizationDeniedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        val username = request.userPrincipal?.name ?: "anonymous"
        logger.warn {
            "접근 권한 부족 - 권한 없는 리소스 접근 시도: " +
                    "user=$username, path=$requestPath, error=${ex.message}"
        }

        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.UNAUTHORIZED_ACCESS.code,
            message = "접근 권한이 없습니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)

        logger.warn {
            "존재하지 않는 리소스 접근 시도" +
                    "path: $requestPath, message: ${ex.message}"
        }

        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.RESOURCE_NOT_FOUND.code,
            message = "요청한 리소스를 찾을 수 없습니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }


    /**
     * 기타 모든 예외 처리 (최종 fallback)
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val requestPath = getRequestPath(request)
        
        logger.error(ex) { 
            "예상치 못한 서버 오류 - path: $requestPath, type: ${ex::class.simpleName}, message: ${ex.message}" 
        }
        
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR.code,
            message = "서버 내부 오류가 발생했습니다",
            details = emptyList(),
            timestamp = LocalDateTime.now(),
            path = requestPath
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    /**
     * 요청 경로 추출
     */
    private fun getRequestPath(request: WebRequest): String {
        return request.getDescription(false).removePrefix("uri=")
    }
}

/**
 * 에러 응답 DTO
 * 
 * ErrorCode 기반의 일관된 응답 구조를 제공합니다.
 */
@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "VAL001")
    val errorCode: String,
    @Schema(description = "에러 메시지", example = "입력값 검증에 실패했습니다")
    val message: String,
    @Schema(description = "상세 에러 정보", example = "[\"title: 학습지 이름은 필수입니다\"]")
    val details: List<String> = emptyList(),
    @Schema(description = "에러 발생 시간", example = "2025-07-25T18:30:00")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    @Schema(description = "요청 경로", example = "/piece")
    val path: String? = null
) {
    companion object {
        /**
         * BaseException으로부터 ErrorResponse 생성
         */
        fun from(ex: BaseException, path: String? = null): ErrorResponse {
            return ErrorResponse(
                errorCode = ex.errorCode.code,
                message = ex.message ?: ex.errorCode.defaultMessage,
                details = emptyList(),
                timestamp = LocalDateTime.now(),
                path = path
            )
        }
    }
} 