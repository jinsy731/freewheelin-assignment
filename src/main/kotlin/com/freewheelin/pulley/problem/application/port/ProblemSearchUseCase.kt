package com.freewheelin.pulley.problem.application.port

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import kotlin.collections.map

/**
 * 문제 조회 Use Case 포트 인터페이스
 * 
 * Presentation 레이어에서 호출하는 문제 조회 관련 기능을 정의합니다.
 */
interface ProblemSearchUseCase {
    
    /**
     * 조건에 따른 문제 조회
     * 
     * @param query 문제 조회 쿼리 정보
     * @return 조회된 문제 리스트
     */
    fun searchProblems(query: ProblemSearchQuery): ProblemSearchResult
}

/**
 * 문제 조회 쿼리 DTO
 */
data class ProblemSearchQuery(
    val totalCount: Int,
    val unitCodeList: List<String>,
    val level: Level,
    val problemType: ProblemTypeFilter
) {
    init {
        require(totalCount > 0) { "총 문제 수는 0보다 커야 합니다." }
        require(unitCodeList.isNotEmpty()) { "유형코드 리스트는 비어있을 수 없습니다." }
    }
    
    /**
     * Repository에서 사용할 도메인 ProblemType으로 변환
     */
    fun getDomainProblemType() = problemType.toDomainType()
}

/**
 * 문제 조회 결과 DTO
 */
data class ProblemSearchResult(
    val problems: List<ProblemInfo>,
    val totalCount: Int
) {
    companion object {
        /**
         * 도메인 객체 리스트를 결과 DTO로 변환
         */
        fun from(problems: List<Problem>): ProblemSearchResult {
            return ProblemSearchResult(
                problems = problems.map { ProblemInfo.from(it) },
                totalCount = problems.size
            )
        }
    }
}

/**
 * 문제 정보 DTO
 */
data class ProblemInfo(
    val id: Long,
    val answer: String,
    val unitCode: String,
    val level: Int,
    val problemType: ProblemType
) {
    companion object {
        /**
         * 도메인 객체를 DTO로 변환
         */
        fun from(problem: Problem): ProblemInfo {
            return ProblemInfo(
                id = problem.id,
                answer = problem.answer,
                unitCode = problem.unitCode,
                level = problem.level,
                problemType = problem.problemType
            )
        }
    }
}

/**
 * 문제 조회 시 사용하는 문제 유형 필터
 */
enum class ProblemTypeFilter {
    ALL,         // 전체 (주관식 + 객관식)
    SUBJECTIVE,  // 주관식
    SELECTION;   // 객관식

    /**
     * 도메인의 ProblemType으로 변환
     * ALL인 경우 null을 반환하여 Repository에서 모든 타입을 조회하도록 함
     */
    fun toDomainType(): ProblemType? = when (this) {
        ALL -> null  // null이면 Repository에서 모든 타입 조회
        SUBJECTIVE -> ProblemType.SUBJECTIVE
        SELECTION -> ProblemType.SELECTION
    }
}