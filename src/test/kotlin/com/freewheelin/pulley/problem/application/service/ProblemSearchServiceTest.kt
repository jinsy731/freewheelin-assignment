package com.freewheelin.pulley.problem.application.service

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import com.freewheelin.pulley.problem.domain.service.AvailableProblemCounts
import com.freewheelin.pulley.problem.domain.service.ProblemDistributionPlan
import com.freewheelin.pulley.problem.domain.service.ProblemDistributionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ProblemSearchService 단위 테스트
 */
class ProblemSearchServiceTest {
    
    private val problemRepository = mockk<ProblemRepository>()
    private val problemDistributionService = mockk<ProblemDistributionService>()
    private val service = ProblemSearchService(problemRepository, problemDistributionService)

    @Nested
    @DisplayName("searchProblems - 문제 목록 검색")
    inner class SearchProblemsTests {

        @Test
        fun `정상적인 경우 - 요청한 문제 수만큼 난이도별 분배하여 조회 성공`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 10,
                unitCodeList = listOf("UC001", "UC002"),
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.SUBJECTIVE
            )

            val availableCounts = AvailableProblemCounts(
                lowCount = 5,
                middleCount = 8,
                highCount = 4
            )

            val distributionPlan = ProblemDistributionPlan(
                lowCount = 2,
                middleCount = 5,
                highCount = 3,
                totalCount = 10
            )

            val lowProblems = listOf(
                Problem(1001L, "답1", "UC001", 1, ProblemType.SUBJECTIVE),
                Problem(1002L, "답2", "UC002", 1, ProblemType.SUBJECTIVE)
            )

            val middleProblems = listOf(
                Problem(1003L, "답3", "UC001", 2, ProblemType.SUBJECTIVE),
                Problem(1004L, "답4", "UC001", 3, ProblemType.SUBJECTIVE),
                Problem(1005L, "답5", "UC002", 4, ProblemType.SUBJECTIVE),
                Problem(1006L, "답6", "UC002", 2, ProblemType.SUBJECTIVE),
                Problem(1007L, "답7", "UC001", 3, ProblemType.SUBJECTIVE)
            )

            val highProblems = listOf(
                Problem(1008L, "답8", "UC001", 5, ProblemType.SUBJECTIVE),
                Problem(1009L, "답9", "UC002", 5, ProblemType.SUBJECTIVE),
                Problem(1010L, "답10", "UC001", 5, ProblemType.SUBJECTIVE)
            )

            // Mocking - 각 난이도별 사용 가능한 문제 수 조회
            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(1)
                )
            } returns 5L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(2, 3, 4)
                )
            } returns 8L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(5)
                )
            } returns 4L

            // Mocking - 분배 계획 계산
            every {
                problemDistributionService.calculateDistribution(
                    problemLevel = Level.MIDDLE,
                    totalCount = 10,
                    availableCounts = availableCounts
                )
            } returns distributionPlan

            // Mocking - 각 난이도별 문제 조회
            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(1),
                    limit = 2
                )
            } returns lowProblems

            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(2, 3, 4),
                    limit = 5
                )
            } returns middleProblems

            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(5),
                    limit = 3
                )
            } returns highProblems

            // When
            val result = service.searchProblems(query)

            // Then
            assertEquals(10, result.problems.size)
            assertEquals(10, result.totalCount)

            // 첫 번째 문제 검증
            val firstProblem = result.problems[0]
            assertEquals(1001L, firstProblem.id)
            assertEquals("답1", firstProblem.answer)
            assertEquals("UC001", firstProblem.unitCode)
            assertEquals(1, firstProblem.level)
            assertEquals(ProblemType.SUBJECTIVE, firstProblem.problemType)

            // 모든 호출 검증
            verify {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(1)
                )
            }

            verify {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(2, 3, 4)
                )
            }

            verify {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(5)
                )
            }

            verify {
                problemDistributionService.calculateDistribution(
                    problemLevel = Level.MIDDLE,
                    totalCount = 10,
                    availableCounts = availableCounts
                )
            }

            verify {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(1),
                    limit = 2
                )
            }

            verify {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(2, 3, 4),
                    limit = 5
                )
            }

            verify {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001", "UC002"),
                    problemType = ProblemType.SUBJECTIVE,
                    levels = listOf(5),
                    limit = 3
                )
            }
        }

        @Test
        fun `사용 가능한 문제 수가 요청보다 적은 경우 - 사용 가능한 수만큼만 조회`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 10,  // 10개 요청
                unitCodeList = listOf("UC001"),
                level = Level.HIGH,
                problemType = ProblemTypeFilter.ALL
            )

            val availableCounts = AvailableProblemCounts(
                lowCount = 1,
                middleCount = 1,
                highCount = 1
            )

            val distributionPlan = ProblemDistributionPlan(
                lowCount = 1,
                middleCount = 1,
                highCount = 1,
                totalCount = 3  // 실제로는 3개만 가능
            )

            val lowProblems = listOf(
                Problem(1001L, "답1", "UC001", 1, ProblemType.SUBJECTIVE)
            )

            val middleProblems = listOf(
                Problem(1002L, "답2", "UC001", 2, ProblemType.SELECTION)
            )

            val highProblems = listOf(
                Problem(1003L, "답3", "UC001", 5, ProblemType.SUBJECTIVE)
            )

            // Mocking - 각 난이도별 사용 가능한 문제 수 조회 (적은 수)
            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(1)
                )
            } returns 1L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(2, 3, 4)
                )
            } returns 1L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(5)
                )
            } returns 1L

            // Mocking - 분배 계획 계산 (실제 가능한 수로 조정됨)
            every {
                problemDistributionService.calculateDistribution(
                    problemLevel = Level.HIGH,
                    totalCount = 3,  // 요청한 10개가 아닌 실제 가능한 3개
                    availableCounts = availableCounts
                )
            } returns distributionPlan

            // Mocking - 각 난이도별 문제 조회
            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(1),
                    limit = 1
                )
            } returns lowProblems

            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(2, 3, 4),
                    limit = 1
                )
            } returns middleProblems

            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(5),
                    limit = 1
                )
            } returns highProblems

            // When
            val result = service.searchProblems(query)

            // Then
            assertEquals(3, result.problems.size)  // 요청한 10개가 아닌 사용 가능한 3개
            assertEquals(3, result.totalCount)

            // 분배 계획이 실제 가능한 수(3)로 호출되었는지 확인
            verify {
                problemDistributionService.calculateDistribution(
                    problemLevel = Level.HIGH,
                    totalCount = 3,
                    availableCounts = availableCounts
                )
            }
        }

        @Test
        fun `사용 가능한 문제가 없는 경우 - 빈 결과 반환`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC999"),  // 존재하지 않는 unitCode
                level = Level.LOW,
                problemType = ProblemTypeFilter.SELECTION
            )

            // Mocking - 모든 난이도에서 사용 가능한 문제가 0개
            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC999"),
                    problemType = ProblemType.SELECTION,
                    levels = listOf(1)
                )
            } returns 0L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC999"),
                    problemType = ProblemType.SELECTION,
                    levels = listOf(2, 3, 4)
                )
            } returns 0L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC999"),
                    problemType = ProblemType.SELECTION,
                    levels = listOf(5)
                )
            } returns 0L

            // When
            val result = service.searchProblems(query)

            // Then
            assertTrue(result.problems.isEmpty())
            assertEquals(0, result.totalCount)

            // findByConditions와 problemDistributionService는 호출되지 않아야 함
            verify(exactly = 0) {
                problemRepository.findByConditions(any(), any(), any(), any())
            }

            verify(exactly = 0) {
                problemDistributionService.calculateDistribution(any(), any(), any())
            }
        }

        @Test
        fun `특정 난이도만 문제가 있는 경우 - 해당 난이도만 조회`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC001"),
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.ALL
            )

            val availableCounts = AvailableProblemCounts(
                lowCount = 0,     // 하 난이도 문제 없음
                middleCount = 10, // 중 난이도만 있음
                highCount = 0     // 상 난이도 문제 없음
            )

            val distributionPlan = ProblemDistributionPlan(
                lowCount = 0,
                middleCount = 5,  // 모두 중 난이도에서
                highCount = 0,
                totalCount = 5
            )

            val middleProblems = listOf(
                Problem(1001L, "답1", "UC001", 2, ProblemType.SUBJECTIVE),
                Problem(1002L, "답2", "UC001", 3, ProblemType.SELECTION),
                Problem(1003L, "답3", "UC001", 4, ProblemType.SUBJECTIVE),
                Problem(1004L, "답4", "UC001", 2, ProblemType.SELECTION),
                Problem(1005L, "답5", "UC001", 3, ProblemType.SUBJECTIVE)
            )

            // Mocking
            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(1)
                )
            } returns 0L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(2, 3, 4)
                )
            } returns 10L

            every {
                problemRepository.countByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(5)
                )
            } returns 0L

            every {
                problemDistributionService.calculateDistribution(
                    problemLevel = Level.MIDDLE,
                    totalCount = 5,
                    availableCounts = availableCounts
                )
            } returns distributionPlan

            every {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(2, 3, 4),
                    limit = 5
                )
            } returns middleProblems

            // When
            val result = service.searchProblems(query)

            // Then
            assertEquals(5, result.problems.size)
            assertEquals(5, result.totalCount)

            // 모든 문제가 중 난이도인지 확인
            result.problems.forEach { problem ->
                assertTrue(problem.level in 2..4)
            }

            // 하/상 난이도 조회는 호출되지 않았는지 확인
            verify(exactly = 0) {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(1),
                    limit = any()
                )
            }

            verify(exactly = 0) {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(5),
                    limit = any()
                )
            }

            verify {
                problemRepository.findByConditions(
                    unitCodes = listOf("UC001"),
                    problemType = null,
                    levels = listOf(2, 3, 4),
                    limit = 5
                )
            }
        }
    }
} 