package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.piece.application.port.*
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 문제 순서 변경 요청 DTO
 * 
 * "사이" 위치 지정 방식:
 * - prevProblemId와 nextProblemId 사이로 이동
 * - prevProblemId가 null이면 맨 앞으로 이동
 * - nextProblemId가 null이면 맨 뒤로 이동
 * - 둘 다 null이면 맨 뒤로 이동
 */
data class ProblemOrderUpdateRequestDto(
    @field:NotNull(message = "문제 ID는 필수입니다")
    @field:Positive(message = "문제 ID는 양수여야 합니다")
    val problemId: Long,
    
    /**
     * 이동될 위치의 이전 문제 ID
     * - null: 맨 앞으로 이동
     */
    val prevProblemId: Long? = null,
    
    /**
     * 이동될 위치의 다음 문제 ID  
     * - null: 맨 뒤로 이동
     */
    val nextProblemId: Long? = null
) {
    
    init {
        // 자기 자신을 이전/다음 문제로 지정하는 경우 검증
        if (prevProblemId == problemId) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "prevProblemId",
                prevProblemId,
                "자기 자신을 이전 문제로 지정할 수 없습니다"
            )
        }
        
        if (nextProblemId == problemId) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "nextProblemId", 
                nextProblemId,
                "자기 자신을 다음 문제로 지정할 수 없습니다"
            )
        }
        
        // 이전 문제와 다음 문제가 같은 경우 검증
        if (prevProblemId != null && nextProblemId != null && prevProblemId == nextProblemId) {
            throw ValidationException(
                ErrorCode.VALIDATION_FAILED,
                "prevProblemId, nextProblemId",
                "$prevProblemId, $nextProblemId",
                "이전 문제와 다음 문제가 같을 수 없습니다"
            )
        }
    }
    
    /**
     * DTO를 Application 레이어 명령 객체로 변환
     */
    fun toApplicationCommand(pieceId: Long, teacherId: Long): ProblemOrderUpdateCommand {
        return ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = prevProblemId,
            nextProblemId = nextProblemId
        )
    }
} 