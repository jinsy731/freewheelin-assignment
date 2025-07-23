package com.freewheelin.pulley.problem.infrastructure.persistence

import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

/**
 * Problem Repository 구현체
 * */
@Repository
class ProblemPersistenceAdapter(
    private val problemJpaRepository: ProblemJpaRepository
) : ProblemRepository {

    override fun findByIds(ids: List<Long>): List<Problem> {
        return problemJpaRepository.findAllById(ids)
            .map { it.toDomain() }
    }

    override fun countByConditions(
        unitCodes: List<String>,
        problemType: ProblemType?,
        levels: List<Int>
    ): Long {
        return problemJpaRepository.countByConditions(unitCodes, problemType, levels)
    }

    override fun findByConditions(
        unitCodes: List<String>,
        problemType: ProblemType?,
        levels: List<Int>,
        limit: Int
    ): List<Problem> {
        val pageable = PageRequest.of(0, limit)
        return problemJpaRepository.findByConditions(unitCodes, problemType, levels, pageable)
            .map { it.toDomain() }
    }
}