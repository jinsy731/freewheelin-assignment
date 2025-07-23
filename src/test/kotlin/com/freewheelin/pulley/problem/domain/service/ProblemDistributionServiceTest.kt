package com.freewheelin.pulley.problem.domain.service

import com.freewheelin.pulley.common.domain.Level
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("ProblemDistributionService 테스트")
class ProblemDistributionServiceTest {

    private lateinit var problemDistributionService: ProblemDistributionService

    @BeforeEach
    fun setUp() {
        problemDistributionService = ProblemDistributionService()
    }

    companion object {
        @JvmStatic
        fun normalDistributionProvider() = listOf(
            // HIGH 선택: 하 20%, 중 30%, 상 50%
            Arguments.of(
                Level.HIGH, 10,
                AvailableProblemCounts(10, 10, 10),
                2, 3, 5  // 10 * (0.2, 0.3, 0.5) = (2, 3, 5)
            ),
            Arguments.of(
                Level.HIGH, 20,
                AvailableProblemCounts(20, 20, 20),
                4, 6, 10  // 20 * (0.2, 0.3, 0.5) = (4, 6, 10)
            ),
            
            // MIDDLE 선택: 하 25%, 중 50%, 상 25%
            Arguments.of(
                Level.MIDDLE, 12,
                AvailableProblemCounts(15, 15, 15),
                3, 6, 3  // 12 * (0.25, 0.5, 0.25) = (3, 6, 3)
            ),
            Arguments.of(
                Level.MIDDLE, 20,
                AvailableProblemCounts(20, 20, 20),
                5, 10, 5  // 20 * (0.25, 0.5, 0.25) = (5, 10, 5)
            ),
            
            // LOW 선택: 하 50%, 중 30%, 상 20%
            Arguments.of(
                Level.LOW, 10,
                AvailableProblemCounts(10, 10, 10),
                5, 3, 2  // 10 * (0.5, 0.3, 0.2) = (5, 3, 2)
            ),
            Arguments.of(
                Level.LOW, 20,
                AvailableProblemCounts(20, 20, 20),
                10, 6, 4  // 20 * (0.5, 0.3, 0.2) = (10, 6, 4)
            )
        )
    }

    @Nested
    @DisplayName("정상적인 분배 테스트")
    inner class NormalDistributionTest {

        @ParameterizedTest
        @MethodSource("com.freewheelin.pulley.problem.domain.service.ProblemDistributionServiceTest#normalDistributionProvider")
        fun `각 난이도별 충분한 문제가 있을 때 올바른 비율로 분배`(
            level: Level,
            totalCount: Int,
            availableCounts: AvailableProblemCounts,
            expectedLow: Int,
            expectedMiddle: Int,
            expectedHigh: Int
        ) {
            // when
            val result = problemDistributionService.calculateDistribution(
                level, totalCount, availableCounts
            )

            // then
            assertEquals(expectedLow, result.lowCount)
            assertEquals(expectedMiddle, result.middleCount)
            assertEquals(expectedHigh, result.highCount)
            assertEquals(expectedLow + expectedMiddle + expectedHigh, result.totalCount)
        }
    }

    @Nested
    @DisplayName("나누어떨어지지 않는 경우 테스트")
    inner class IndivisibleDistributionTest {

        @Test
        fun `HIGH 선택시 나머지는 상 난이도에 할당`() {
            // given
            val totalCount = 11  // 11 * (0.2, 0.3, 0.5) = (2.2, 3.3, 5.5) -> (2, 3, 6)
            val availableCounts = AvailableProblemCounts(10, 10, 10)

            // when
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(2, result.lowCount)     // (int)(11 * 0.2) = 2
            assertEquals(3, result.middleCount)  // (int)(11 * 0.3) = 3  
            assertEquals(6, result.highCount)    // 11 - 2 - 3 = 6 (나머지)
            assertEquals(11, result.totalCount)
        }

        @Test
        fun `MIDDLE 선택시 나머지는 상 난이도에 할당`() {
            // given
            val totalCount = 13  // 13 * (0.25, 0.5, 0.25) = (3.25, 6.5, 3.25) -> (3, 6, 4)
            val availableCounts = AvailableProblemCounts(10, 10, 10)

            // when
            val result = problemDistributionService.calculateDistribution(
                Level.MIDDLE, totalCount, availableCounts
            )

            // then
            assertEquals(3, result.lowCount)     // (int)(13 * 0.25) = 3
            assertEquals(6, result.middleCount)  // (int)(13 * 0.5) = 6
            assertEquals(4, result.highCount)    // 13 - 3 - 6 = 4 (나머지)
            assertEquals(13, result.totalCount)
        }
    }

    @Nested
    @DisplayName("문제 부족으로 인한 재분배 테스트")
    inner class RedistributionTest {

        @Test
        fun `상 난이도 문제 부족시 다른 난이도로 재분배`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(10, 10, 2) // 상 난이도 부족
            
            // when (HIGH 선택: 이상적으로는 하 2, 중 3, 상 5이지만 상이 2개만 있음)
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(5, result.lowCount)     // 2 + 3(부족분 재분배)
            assertEquals(3, result.middleCount)  // 3 (변경 없음)
            assertEquals(2, result.highCount)    // 2 (가용한 최대)
            assertEquals(10, result.totalCount)
        }

        @Test
        fun `중 난이도 문제 부족시 다른 난이도로 재분배`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(10, 1, 10) // 중 난이도 부족
            
            // when (HIGH 선택: 이상적으로는 하 2, 중 3, 상 5이지만 중이 1개만 있음)
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(4, result.lowCount)     // 2 + 2(부족분 재분배)
            assertEquals(1, result.middleCount)  // 1 (가용한 최대)
            assertEquals(5, result.highCount)    // 5 (변경 없음)
            assertEquals(10, result.totalCount)
        }

        @Test
        fun `하 난이도 문제 부족시 다른 난이도로 재분배`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(1, 10, 10) // 하 난이도 부족
            
            // when (HIGH 선택: 이상적으로는 하 2, 중 3, 상 5이지만 하가 1개만 있음)
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(1, result.lowCount)     // 1 (가용한 최대)
            assertEquals(4, result.middleCount)  // 3 + 1(부족분 재분배)
            assertEquals(5, result.highCount)    // 5 (변경 없음)
            assertEquals(10, result.totalCount)
        }

        @Test
        fun `여러 난이도 문제가 동시에 부족할 때 재분배`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(1, 2, 3) // 모든 난이도 부족
            
            // when (HIGH 선택: 이상적으로는 하 2, 중 3, 상 5)
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(1, result.lowCount)     // 1 (가용한 최대)
            assertEquals(2, result.middleCount)  // 2 (가용한 최대)
            assertEquals(3, result.highCount)    // 3 (가용한 최대)
            assertEquals(6, result.totalCount)   // 요청한 10개보다 적은 6개만 가능
        }

        @Test
        fun `재분배시 우선순위 하에서 중에서 상 순으로 보충`() {
            // given
            val totalCount = 12
            val availableCounts = AvailableProblemCounts(8, 5, 2) // 중,상 난이도 부족
            
            // when (MIDDLE 선택: 이상적으로는 하 3, 중 6, 상 3)
            // 제약 적용: 하 3, 중 5, 상 2 = 10개, 부족분 2개
            // 재분배: 하에서 2개 추가 보충 (3 + 2 = 5)
            val result = problemDistributionService.calculateDistribution(
                Level.MIDDLE, totalCount, availableCounts
            )

            // then
            assertEquals(5, result.lowCount)     // 3 + 2(부족분 재분배) = 5
            assertEquals(5, result.middleCount)  // 5 (가용한 최대)
            assertEquals(2, result.highCount)    // 2 (가용한 최대)
            assertEquals(12, result.totalCount)
        }

        @Test
        fun `재분배시 하 난이도에서 모두 보충하지 못할 때 중 난이도에서 추가 보충`() {
            // given
            val totalCount = 15
            val availableCounts = AvailableProblemCounts(4, 10, 1) // 상 난이도 크게 부족
            
            // when (HIGH 선택: 이상적으로는 하 3, 중 4, 상 8)
            // 제약 적용: 하 3, 중 4, 상 1 = 8개, 부족분 7개
            // 재분배: 하에서 1개 추가(3+1=4), 중에서 6개 추가(4+6=10)
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(4, result.lowCount)     // 3 + 1(추가 가능한 최대)
            assertEquals(10, result.middleCount) // 4 + 6(추가 가능한 최대)
            assertEquals(1, result.highCount)    // 1 (가용한 최대)
            assertEquals(15, result.totalCount)
        }

        @Test
        fun `재분배시 하중에서 모두 보충하지 못할 때 상 난이도에서 추가 보충`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(2, 3, 8) // 하,중 난이도 부족
            
            // when (LOW 선택: 이상적으로는 하 5, 중 3, 상 2)
            // 제약 적용: 하 2, 중 3, 상 2 = 7개, 부족분 3개
            // 재분배: 하에서 0개 추가(이미 최대), 중에서 0개 추가(이미 최대), 상에서 3개 추가(2+3=5)
            val result = problemDistributionService.calculateDistribution(
                Level.LOW, totalCount, availableCounts
            )

            // then
            assertEquals(2, result.lowCount)     // 2 (가용한 최대)
            assertEquals(3, result.middleCount)  // 3 (가용한 최대)
            assertEquals(5, result.highCount)    // 2 + 3(부족분 재분배)
            assertEquals(10, result.totalCount)
        }
    }

    @Nested
    @DisplayName("경계 케이스 테스트")
    inner class EdgeCaseTest {

        @Test
        fun `총 문제 수가 0일 때`() {
            // given
            val totalCount = 0
            val availableCounts = AvailableProblemCounts(10, 10, 10)

            // when
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(0, result.lowCount)
            assertEquals(0, result.middleCount)
            assertEquals(0, result.highCount)
            assertEquals(0, result.totalCount)
        }

        @Test
        fun `모든 난이도 실제 문제 수가 0일 때`() {
            // given
            val totalCount = 10
            val availableCounts = AvailableProblemCounts(0, 0, 0)

            // when
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(0, result.lowCount)
            assertEquals(0, result.middleCount)
            assertEquals(0, result.highCount)
            assertEquals(0, result.totalCount)
        }

        @Test
        fun `총 문제 수가 1일 때 정확한 분배`() {
            // given
            val totalCount = 1
            val availableCounts = AvailableProblemCounts(10, 10, 10)

            // when (HIGH 선택: 1 * (0.2, 0.3, 0.5) = (0.2, 0.3, 0.5) -> (0, 0, 1))
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(0, result.lowCount)     // (int)(1 * 0.2) = 0
            assertEquals(0, result.middleCount)  // (int)(1 * 0.3) = 0
            assertEquals(1, result.highCount)    // 1 - 0 - 0 = 1 (나머지)
            assertEquals(1, result.totalCount)
        }

        @Test
        fun `요청한 문제 수보다 실제 문제가 적을 때`() {
            // given
            val totalCount = 100  // 많이 요청
            val availableCounts = AvailableProblemCounts(2, 3, 4) // 실제로는 9개만 있음

            // when
            val result = problemDistributionService.calculateDistribution(
                Level.HIGH, totalCount, availableCounts
            )

            // then
            assertEquals(2, result.lowCount)     // 가용한 최대
            assertEquals(3, result.middleCount)  // 가용한 최대
            assertEquals(4, result.highCount)    // 가용한 최대
            assertEquals(9, result.totalCount)   // 요청한 100개가 아닌 가용한 9개
        }
    }

    @Nested
    @DisplayName("데이터 클래스 유효성 검증 테스트")
    inner class ValidationTest {

        @Nested
        @DisplayName("AvailableProblemCounts 검증")
        inner class AvailableProblemCountsValidationTest {

            @Test
            fun `음수 하 난이도 문제 수는 예외 발생`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    AvailableProblemCounts(-1, 10, 10)
                }
                assertEquals("하 난이도 실제 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `음수 중 난이도 문제 수는 예외 발생`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    AvailableProblemCounts(10, -1, 10)
                }
                assertEquals("중 난이도 실제 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `음수 상 난이도 문제 수는 예외 발생`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    AvailableProblemCounts(10, 10, -1)
                }
                assertEquals("상 난이도 실제 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `모든 값이 0인 경우는 유효`() {
                // when & then (예외 발생하지 않아야 함)
                val availableCounts = AvailableProblemCounts(0, 0, 0)
                assertEquals(0, availableCounts.lowCount)
                assertEquals(0, availableCounts.middleCount)
                assertEquals(0, availableCounts.highCount)
            }
        }

        @Nested
        @DisplayName("ProblemDistributionPlan 검증")
        inner class ProblemDistributionPlanValidationTest {

            @Test
            fun `음수 하 난이도 문제 수는 예외 발생 in ProblemDistributionPlan`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    ProblemDistributionPlan(-1, 5, 5, 9)
                }
                assertEquals("하 난이도 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `음수 중 난이도 문제 수는 예외 발생 in ProblemDistributionPlan`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    ProblemDistributionPlan(5, -1, 5, 9)
                }
                assertEquals("중 난이도 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `음수 상 난이도 문제 수는 예외 발생 in ProblemDistributionPlan`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    ProblemDistributionPlan(5, 5, -1, 9)
                }
                assertEquals("상 난이도 문제 수는 0 이상이어야 합니다.", exception.message)
            }

            @Test
            fun `총합이 맞지 않으면 예외 발생`() {
                // when & then
                val exception = assertThrows<IllegalArgumentException> {
                    ProblemDistributionPlan(2, 3, 5, 11) // 2+3+5=10 != 11
                }
                assertEquals("각 난이도별 문제 수의 합은 총 문제 수와 일치해야 합니다.", exception.message)
            }

            @Test
            fun `모든 값이 0이고 총합도 0인 경우는 유효`() {
                // when & then (예외 발생하지 않아야 함)
                val plan = ProblemDistributionPlan(0, 0, 0, 0)
                assertEquals(0, plan.lowCount)
                assertEquals(0, plan.middleCount)
                assertEquals(0, plan.highCount)
                assertEquals(0, plan.totalCount)
            }

            @Test
            fun `정상적인 값들은 유효`() {
                // when & then (예외 발생하지 않아야 함)
                val plan = ProblemDistributionPlan(2, 3, 5, 10)
                assertEquals(2, plan.lowCount)
                assertEquals(3, plan.middleCount)
                assertEquals(5, plan.highCount)
                assertEquals(10, plan.totalCount)
            }
        }
    }
} 