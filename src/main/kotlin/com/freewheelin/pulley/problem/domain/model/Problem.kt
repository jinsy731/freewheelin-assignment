package com.freewheelin.pulley.problem.domain.model

import com.freewheelin.pulley.common.domain.Level

/**
 * 문제 도메인 모델
 *
 * - 문제 ID
 * - 정답
 * - 유형 코드 (단원 코드)
 * - 난이도 (1-5)
 * - 문제 유형 (주관식/객관식)
 */
data class Problem(
    val id: Long,
    val answer: String,
    val unitCode: String,
    val level: Int,
    val problemType: ProblemType
) {
    init {
        require(level in Level.getAllLevels()) { "문제 난이도는 1~5 사이여야 합니다." }
        require(answer.isNotBlank()) { "정답은 비어있을 수 없습니다." }
        require(unitCode.isNotBlank()) { "유형코드는 비어있을 수 없습니다." }
    }

    /**
     * 이 문제의 난이도 레벨 반환
     */
    fun getLevel(): Level = Level.fromLevel(level)

    /**
     * 제출된 답안이 정답인지 확인
     */
    fun isCorrectAnswer(submittedAnswer: String): Boolean {
        return answer.trim().equals(submittedAnswer.trim(), ignoreCase = true)
    }
}

/**
 * 문제 유형
 */
enum class ProblemType {
    SUBJECTIVE,  // 주관식
    SELECTION    // 객관식
}