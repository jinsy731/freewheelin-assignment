package com.freewheelin.pulley.problem.domain.port

import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType

/**
 * Problem Repository 인터페이스 (Port)
 */
interface ProblemRepository {

    /**
     * ID 리스트로 문제 조회
     */
    fun findByIds(ids: List<Long>): List<Problem>

    /**
     * 조건에 맞는 문제 수 조회
     *
     * @param unitCodes 유형코드 리스트
     * @param problemType 문제 유형 (선택사항)
     * @param levels 난이도 레벨 리스트
     * @return 조건에 맞는 문제 수
     */
    fun countByConditions(
        unitCodes: List<String>,
        problemType: ProblemType?,
        levels: List<Int>
    ): Long

    /**
     * 조건에 맞는 문제 조회 (제한 개수 포함)
     *
     * @param unitCodes 유형코드 리스트
     * @param problemType 문제 유형 (선택사항)
     * @param levels 난이도 레벨 리스트
     * @param limit 조회할 최대 개수
     * @return 조건에 맞는 문제 리스트
     */
    fun findByConditions(
        unitCodes: List<String>,
        problemType: ProblemType?,
        levels: List<Int>,
        limit: Int
    ): List<Problem>
}