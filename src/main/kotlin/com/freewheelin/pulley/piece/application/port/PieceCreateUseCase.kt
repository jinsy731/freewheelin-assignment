package com.freewheelin.pulley.piece.application.port

import kotlin.collections.distinct
import kotlin.collections.isNotEmpty
import kotlin.text.isNotBlank

/**
 * 학습지 생성 Use Case 포트 인터페이스
 * 
 * Presentation 레이어에서 호출하는 학습지 생성 관련 기능을 정의합니다.
 */
interface PieceCreateUseCase {
    
    /**
     * 학습지 생성
     * 
     * @param request 학습지 생성 요청 정보
     * @return 생성된 학습지 정보
     */
    fun createPiece(request: PieceCreateRequest): PieceCreateResult
}

/**
 * 학습지 생성 요청 DTO
 */
data class PieceCreateRequest(
    val teacherId: Long,
    val title: String,
    val problemIds: List<Long>
) {
    init {
        require(title.isNotBlank()) { "학습지 이름은 비어있을 수 없습니다." }
        require(problemIds.isNotEmpty()) { "문제 ID 리스트는 비어있을 수 없습니다." }
        require(problemIds.size <= MAX_PROBLEMS) { "학습지에 포함될 수 있는 최대 문제 수는 ${MAX_PROBLEMS}개입니다." }
        require(problemIds.distinct().size == problemIds.size) { "중복된 문제 ID가 존재합니다." }
    }
    
    companion object {
        const val MAX_PROBLEMS = 50
    }
}

/**
 * 학습지 생성 결과 DTO
 */
data class PieceCreateResult(
    val pieceId: Long,
    val name: String
) 