package com.freewheelin.pulley.problem.application.service

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemSearchResult
import com.freewheelin.pulley.problem.application.port.ProblemSearchUseCase
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import com.freewheelin.pulley.problem.domain.service.AvailableProblemCounts
import com.freewheelin.pulley.problem.domain.service.ProblemDistributionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 문제 조회 Application Service
 * 
 * 문제 조회와 관련된 유스케이스를 오케스트레이션합니다.
 * 1. 각 난이도별 실제 문제 개수 조회
 * 2. 도메인 서비스에서 비율 계산
 * 3. Repository에서 각 난이도별 문제 조회
 */
@Service
@Transactional(readOnly = true)
class ProblemSearchService(
    private val problemRepository: ProblemRepository,
    private val problemDistributionService: ProblemDistributionService
) : ProblemSearchUseCase {
    
    override fun searchProblems(query: ProblemSearchQuery): ProblemSearchResult {
        val domainProblemType = query.getDomainProblemType()
        
        // 1. 조건에 맞는 각 난이도별 문제 개수를 먼저 조회
        val availableCounts = getAvailableProblemCounts(query.unitCodeList, domainProblemType)
        
        // 전체 사용 가능한 문제 수 계산
        val totalAvailable = availableCounts.lowCount + availableCounts.middleCount + availableCounts.highCount
        
        // 조건에 맞는 문제가 없는 경우 빈 결과 반환
        if (totalAvailable == 0) {
            return ProblemSearchResult(problems = emptyList(), totalCount = 0)
        }
        
        // 요청한 문제 수가 사용 가능한 문제 수보다 많으면 사용 가능한 수만큼만 조회
        val actualTotalCount = minOf(query.totalCount, totalAvailable)
        
        // 2. 도메인 서비스에서 각 난이도별 비율에 맞는 문제수를 계산
        val distributionPlan = problemDistributionService.calculateDistribution(
            problemLevel = query.level,
            totalCount = actualTotalCount,
            availableCounts = availableCounts
        )
        
        val allProblems = mutableListOf<Problem>()
        
        // 3. Repository에서 각 난이도별로 문제를 조회
        
        // 하 난이도 문제 조회 (level 1)
        if (distributionPlan.lowCount > 0) {
            val lowProblems = problemRepository.findByConditions(
                unitCodes = query.unitCodeList,
                problemType = domainProblemType,
                levels = Level.LOW.levels,
                limit = distributionPlan.lowCount
            )
            allProblems.addAll(lowProblems)
        }
        
        // 중 난이도 문제 조회 (level 2,3,4)
        if (distributionPlan.middleCount > 0) {
            val middleProblems = problemRepository.findByConditions(
                unitCodes = query.unitCodeList,
                problemType = domainProblemType,
                levels = Level.MIDDLE.levels,
                limit = distributionPlan.middleCount
            )
            allProblems.addAll(middleProblems)
        }
        
        // 상 난이도 문제 조회 (level 5)
        if (distributionPlan.highCount > 0) {
            val highProblems = problemRepository.findByConditions(
                unitCodes = query.unitCodeList,
                problemType = domainProblemType,
                levels = Level.HIGH.levels,
                limit = distributionPlan.highCount
            )
            allProblems.addAll(highProblems)
        }
        
        // 4. 결과 DTO로 변환하여 반환
        return ProblemSearchResult.from(allProblems)
    }
    
    /**
     * 각 난이도별 실제 존재하는 문제 개수 조회
     */
    private fun getAvailableProblemCounts(
        unitCodes: List<String>,
        problemType: ProblemType?
    ): AvailableProblemCounts {
        val lowCount = problemRepository.countByConditions(
            unitCodes = unitCodes,
            problemType = problemType,
            levels = Level.LOW.levels
        ).toInt()
        
        val middleCount = problemRepository.countByConditions(
            unitCodes = unitCodes,
            problemType = problemType,
            levels = Level.MIDDLE.levels
        ).toInt()
        
        val highCount = problemRepository.countByConditions(
            unitCodes = unitCodes,
            problemType = problemType,
            levels = Level.HIGH.levels
        ).toInt()
        
        return AvailableProblemCounts(
            lowCount = lowCount,
            middleCount = middleCount,
            highCount = highCount
        )
    }
} 