package com.freewheelin.pulley.assignment.application.port

import kotlin.collections.all
import kotlin.collections.distinct
import kotlin.collections.isNotEmpty

/**
 * 학습지 출제 Use Case 포트 인터페이스
 * 
 * Presentation 레이어에서 호출하는 학습지 출제 관련 기능을 정의합니다.
 */
interface AssignmentCreateUseCase {
    
    /**
     * 학습지 출제
     * 
     * @param request 학습지 출제 요청 정보
     * @return 출제 결과 정보
     */
    fun assignPiece(request: AssignmentCreateRequest): AssignmentCreateResult
}

/**
 * 학습지 출제 요청 DTO
 */
data class AssignmentCreateRequest(
    val teacherId: Long,
    val pieceId: Long,
    val studentIds: List<Long>
) {
    init {
        require(teacherId > 0) { "선생님 ID는 0보다 커야 합니다." }
        require(pieceId > 0) { "학습지 ID는 0보다 커야 합니다." }
        require(studentIds.isNotEmpty()) { "학생 ID 리스트는 비어있을 수 없습니다." }
        require(studentIds.all { it > 0 }) { "모든 학생 ID는 0보다 커야 합니다." }
        require(studentIds.distinct().size == studentIds.size) { "중복된 학생 ID가 존재합니다." }
    }
}

/**
 * 학습지 출제 결과 DTO
 */
data class AssignmentCreateResult(
    val pieceId: Long,
    val totalRequestedStudents: Int,      // 요청된 전체 학생 수
    val newlyAssignedStudents: Int,       // 새로 출제된 학생 수
    val alreadyAssignedStudents: Int,     // 이미 출제되어 있던 학생 수
    val newlyAssignedStudentIds: List<Long>,  // 새로 출제된 학생 ID들
    val alreadyAssignedStudentIds: List<Long> // 이미 출제되어 있던 학생 ID들
) {
    val isAllStudentsProcessed: Boolean
        get() = newlyAssignedStudents + alreadyAssignedStudents == totalRequestedStudents
} 