package com.freewheelin.pulley.piece.domain.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.problem.domain.model.Problem
import kotlin.collections.isNotEmpty
import kotlin.collections.mapIndexed
import kotlin.collections.sortedWith
import kotlin.comparisons.thenBy

/**
 * PieceProblem 생성 Factory
 * 
 * 문제들을 정렬하고 초기 위치를 설정하여 PieceProblem 리스트를 생성하는 
 * 비즈니스 로직을 캡슐화합니다.
 */
object PieceProblemFactory {

    const val MAX_PROBLEMS = 50 // 학습지에 포함될 수 있는 최대 문제 수
    
    /**
     * 문제들로부터 PieceProblem 리스트 생성
     * 
     * 비즈니스 규칙:
     * 1. 문제들을 유형코드(unitCode) 순으로 정렬
     * 2. 같은 유형코드 내에서는 난이도(level) 순으로 정렬  
     * 3. 초기 위치 값들을 순차적으로 생성 (1.0, 2.0, 3.0, ...)
     * 
     * @param pieceId 학습지 ID
     * @param problems 포함할 문제들
     * @return 정렬되고 위치가 설정된 PieceProblem 리스트
     */
    fun createFromProblems(
        pieceId: PieceId,
        problems: List<Problem>
    ): List<PieceProblem> {
        require(problems.isNotEmpty()) { "문제 리스트는 비어있을 수 없습니다." }
        require(problems.size <= MAX_PROBLEMS) { "학습지에 포함될 수 있는 최대 문제 수는 ${MAX_PROBLEMS}개입니다." }

        // 1. 문제들을 유형코드와 난이도 순으로 정렬
        val sortedProblems = problems.sortedWith(
            compareBy<Problem> { it.unitCode }
                .thenBy { it.level }
        )
        
        // 2. 초기 위치 값들 생성
        val initialPositions = Position.generateInitialPositions(sortedProblems.size)
        
        // 3. PieceProblem 엔티티들 생성
        return sortedProblems.mapIndexed { index, problem ->
            PieceProblem.create(
                pieceId = pieceId,
                problemId = ProblemId(problem.id),
                position = initialPositions[index]
            )
        }
    }
} 