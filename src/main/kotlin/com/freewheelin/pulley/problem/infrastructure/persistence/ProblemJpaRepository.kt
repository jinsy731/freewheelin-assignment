package com.freewheelin.pulley.problem.infrastructure.persistence

import com.freewheelin.pulley.problem.domain.model.ProblemType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Problem JPA Repository 인터페이스
 */
interface ProblemJpaRepository : JpaRepository<ProblemJpaEntity, Long> {

    /**
     * 유형코드와 문제 유형, 난이도 조건으로 문제 조회
     *
     * @param unitCodes 유형코드 리스트
     * @param problemType 문제 유형
     * @param levels 난이도 리스트
     * @param pageable 페이지 정보
     * @return 문제 리스트
     */
    @Query("""
        SELECT p FROM ProblemJpaEntity p 
        WHERE p.unitCode IN :unitCodes 
        AND (:problemType IS NULL OR p.problemType = :problemType)
        AND p.level IN :levels
        ORDER BY p.unitCode ASC, p.level ASC
    """)
    fun findByConditions(
        @Param("unitCodes") unitCodes: List<String>,
        @Param("problemType") problemType: ProblemType?,
        @Param("levels") levels: List<Int>,
        pageable: Pageable
    ): List<ProblemJpaEntity>

    /**
     * 유형코드와 문제 유형, 난이도 조건으로 문제 수 조회
     *
     * @param unitCodes 유형코드 리스트
     * @param problemType 문제 유형
     * @param levels 난이도 리스트
     * @return 문제 수
     */
    @Query("""
        SELECT COUNT(p) FROM ProblemJpaEntity p 
        WHERE p.unitCode IN :unitCodes 
        AND (:problemType IS NULL OR p.problemType = :problemType)
        AND p.level IN :levels
    """)
    fun countByConditions(
        @Param("unitCodes") unitCodes: List<String>,
        @Param("problemType") problemType: ProblemType?,
        @Param("levels") levels: List<Int>
    ): Long
}