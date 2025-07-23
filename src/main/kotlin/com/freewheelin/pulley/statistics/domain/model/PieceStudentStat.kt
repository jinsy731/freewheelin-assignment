package com.freewheelin.pulley.statistics.domain.model

import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId

/**
 * 학생별 학습지 통계 도메인 모델
 */
data class PieceStudentStat(
    val id: Long = 0L,
    val assignmentId: AssignmentId,
    val pieceId: PieceId,
    val studentId: StudentId,
    val totalCount: Int,
    val correctCount: Int,
    val correctnessRate: CorrectnessRate
) {
    init {
        require(totalCount >= MIN_COUNT) { "전체 문제 수는 $MIN_COUNT 이상이어야 합니다." }
        require(correctCount >= MIN_COUNT) { "정답 수는 $MIN_COUNT 이상이어야 합니다." }
        require(correctCount <= totalCount) { "정답 수는 전체 문제 수를 초과할 수 없습니다." }
    }
    
    /**
     * 통계 업데이트
     */
    fun update(newTotalCount: Int, newCorrectCount: Int): PieceStudentStat {
        val newRate = calculateCorrectnessRate(newTotalCount, newCorrectCount)
        
        return copy(
            totalCount = newTotalCount,
            correctCount = newCorrectCount,
            correctnessRate = newRate
        )
    }
    
    /**
     * 정답률 계산
     */
    private fun calculateCorrectnessRate(totalCount: Int, correctCount: Int): CorrectnessRate {
        return if (hasValidSubmissions(totalCount)) {
            CorrectnessRate(correctCount.toDouble() / totalCount)
        } else {
            CorrectnessRate.zero()
        }
    }
    
    /**
     * 유효한 제출이 있는지 확인
     */
    private fun hasValidSubmissions(totalCount: Int): Boolean {
        return totalCount > MIN_COUNT
    }
    
    companion object {
        private const val MIN_COUNT = 0
        
        /**
         * 초기 통계 생성
         */
        fun create(
            assignmentId: Long,
            pieceId: Long,
            studentId: Long,
            totalCount: Int,
            correctCount: Int
        ): PieceStudentStat {
            val rate = if (totalCount > MIN_COUNT) {
                CorrectnessRate(correctCount.toDouble() / totalCount)
            } else {
                CorrectnessRate.zero()
            }
            
            return PieceStudentStat(
                assignmentId = AssignmentId(assignmentId),
                pieceId = PieceId(pieceId),
                studentId = StudentId(studentId),
                totalCount = totalCount,
                correctCount = correctCount,
                correctnessRate = rate
            )
        }
    }
} 