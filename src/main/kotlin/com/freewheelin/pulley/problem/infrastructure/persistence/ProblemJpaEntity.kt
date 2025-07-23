package com.freewheelin.pulley.problem.infrastructure.persistence

import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import jakarta.persistence.*

/**
 * 문제 JPA 엔티티
 */
@Entity
@Table(
    name = "problems",
    indexes = [
        Index(name = "idx_unit_code", columnList = "unit_code"),
        Index(name = "idx_level", columnList = "problem_level"),
        Index(name = "idx_problem_type", columnList = "problem_type"),
        Index(name = "idx_unit_code_level_type", columnList = "unit_code, problem_level, problem_type")
    ]
)
class ProblemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "answer", nullable = false)
    val answer: String,

    @Column(name = "unit_code", nullable = false, length = 10)
    val unitCode: String,

    @Column(name = "problem_level", nullable = false)
    val level: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false)
    val problemType: ProblemType
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): Problem {
        return Problem(
            id = id,
            answer = answer,
            unitCode = unitCode,
            level = level,
            problemType = problemType
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(problem: Problem): ProblemJpaEntity {
            return ProblemJpaEntity(
                id = problem.id,
                answer = problem.answer,
                unitCode = problem.unitCode,
                level = problem.level,
                problemType = problem.problemType
            )
        }
    }
} 