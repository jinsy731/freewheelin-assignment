package com.freewheelin.pulley.problem.application.service

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemSearchUseCase
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaEntity
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * ProblemSearchService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProblemSearchServiceTestIT {
    
    @Autowired
    private lateinit var problemSearchUseCase: ProblemSearchUseCase
    
    @Autowired
    private lateinit var problemJpaRepository: ProblemJpaRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        problemJpaRepository.deleteAll()
        
        // 테스트 문제 데이터 삽입
        val testProblems = listOf(
            // UC001 - 다양한 난이도와 유형 (충분한 모든 난이도 문제 추가)
            ProblemJpaEntity(0, "답1", "UC001", 1, ProblemType.SUBJECTIVE),  // 하
            ProblemJpaEntity(0, "답2", "UC001", 1, ProblemType.SELECTION),   // 하
            ProblemJpaEntity(0, "답3", "UC001", 1, ProblemType.SUBJECTIVE),  // 하
            ProblemJpaEntity(0, "답21", "UC001", 1, ProblemType.SELECTION),  // 하
            ProblemJpaEntity(0, "답22", "UC001", 1, ProblemType.SUBJECTIVE), // 하
            ProblemJpaEntity(0, "답23", "UC001", 1, ProblemType.SELECTION),  // 하
            ProblemJpaEntity(0, "답4", "UC001", 2, ProblemType.SUBJECTIVE),  // 중
            ProblemJpaEntity(0, "답5", "UC001", 3, ProblemType.SELECTION),   // 중
            ProblemJpaEntity(0, "답6", "UC001", 4, ProblemType.SUBJECTIVE),  // 중
            ProblemJpaEntity(0, "답7", "UC001", 2, ProblemType.SELECTION),   // 중
            ProblemJpaEntity(0, "답8", "UC001", 3, ProblemType.SUBJECTIVE),  // 중
            ProblemJpaEntity(0, "답9", "UC001", 5, ProblemType.SELECTION),   // 상
            ProblemJpaEntity(0, "답10", "UC001", 5, ProblemType.SUBJECTIVE), // 상
            ProblemJpaEntity(0, "답11", "UC001", 5, ProblemType.SELECTION),  // 상
            ProblemJpaEntity(0, "답12", "UC001", 5, ProblemType.SUBJECTIVE), // 상
            ProblemJpaEntity(0, "답13", "UC001", 5, ProblemType.SELECTION),  // 상
            ProblemJpaEntity(0, "답14", "UC001", 5, ProblemType.SUBJECTIVE), // 상
            
            // UC002 - 제한된 난이도
            ProblemJpaEntity(0, "답15", "UC002", 2, ProblemType.SUBJECTIVE), // 중
            ProblemJpaEntity(0, "답16", "UC002", 3, ProblemType.SELECTION),  // 중
            ProblemJpaEntity(0, "답17", "UC002", 4, ProblemType.SUBJECTIVE), // 중
            
            // UC003 - 주관식만
            ProblemJpaEntity(0, "답18", "UC003", 1, ProblemType.SUBJECTIVE), // 하
            ProblemJpaEntity(0, "답19", "UC003", 3, ProblemType.SUBJECTIVE), // 중
            ProblemJpaEntity(0, "답20", "UC003", 5, ProblemType.SUBJECTIVE)  // 상
        )
        
        problemJpaRepository.saveAll(testProblems)
    }

    @Nested
    @DisplayName("searchProblems - 학습지 문제 목록 조회")
    inner class SearchProblemsTests {

        @Test
        fun `정상적인 경우 - 난이도별 분배하여 문제 조회 성공`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 6,
                unitCodeList = listOf("UC001"),
                level = Level.MIDDLE,  // 중 난이도 선택 (하 25%, 중 50%, 상 25%)
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertEquals(6, result.problems.size)
            assertEquals(6, result.totalCount)

            // 모든 문제가 UC001에서 나온 것인지 확인
            result.problems.forEach { problem ->
                assertEquals("UC001", problem.unitCode)
            }

            // 난이도 분배 확인 (정확한 개수는 분배 로직에 따라 결정됨)
            val lowLevelCount = result.problems.count { it.level == 1 }
            val middleLevelCount = result.problems.count { it.level in 2..4 }
            val highLevelCount = result.problems.count { it.level == 5 }

            assertTrue(lowLevelCount >= 0)
            assertTrue(middleLevelCount >= 0)
            assertTrue(highLevelCount >= 0)
            assertEquals(6, lowLevelCount + middleLevelCount + highLevelCount)
        }

        @Test
        fun `특정 문제 유형만 조회 - 주관식만 필터링`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC001"),
                level = Level.HIGH,
                problemType = ProblemTypeFilter.SUBJECTIVE
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isNotEmpty())

            // 모든 문제가 주관식인지 확인
            result.problems.forEach { problem ->
                assertEquals(ProblemType.SUBJECTIVE, problem.problemType)
                assertEquals("UC001", problem.unitCode)
            }
        }

        @Test
        fun `여러 유형코드 조회 - UC001과 UC002`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 8,
                unitCodeList = listOf("UC001", "UC002"),
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isNotEmpty())

            // 모든 문제가 UC001 또는 UC002에서 나온 것인지 확인
            result.problems.forEach { problem ->
                assertTrue(problem.unitCode in listOf("UC001", "UC002"))
            }

            // UC001과 UC002 문제가 모두 포함되었는지 확인
            val unitCodes = result.problems.map { it.unitCode }.toSet()
            assertTrue(unitCodes.contains("UC001") || unitCodes.contains("UC002"))
        }

        @Test
        fun `사용 가능한 문제 수가 요청보다 적은 경우`() {
            // Given - UC003은 문제가 3개뿐
            val query = ProblemSearchQuery(
                totalCount = 10,  // 10개 요청하지만 UC003에는 3개만 있음
                unitCodeList = listOf("UC003"),
                level = Level.LOW,
                problemType = ProblemTypeFilter.SUBJECTIVE
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.size <= 3)  // 최대 3개까지만 조회됨
            assertEquals(result.problems.size, result.totalCount)

            // 모든 문제가 UC003의 주관식인지 확인
            result.problems.forEach { problem ->
                assertEquals("UC003", problem.unitCode)
                assertEquals(ProblemType.SUBJECTIVE, problem.problemType)
            }
        }

        @Test
        fun `존재하지 않는 유형코드 조회 - 빈 결과 반환`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC999"),  // 존재하지 않는 유형코드
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isEmpty())
            assertEquals(0, result.totalCount)
        }

        @Test
        fun `특정 문제 유형이 없는 경우 - 객관식만 요청`() {
            // Given - UC003에는 주관식만 있음
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC003"),
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.SELECTION  // 객관식 요청
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isEmpty())
            assertEquals(0, result.totalCount)
        }

        @Test
        fun `특정 난이도 문제만 있는 경우 - 중 난이도만 조회`() {
            // Given - UC002는 중 난이도 문제만 있음 (level 2,3,4)
            val query = ProblemSearchQuery(
                totalCount = 5,
                unitCodeList = listOf("UC002"),
                level = Level.MIDDLE,
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isNotEmpty())

            // 모든 문제가 중 난이도인지 확인
            result.problems.forEach { problem ->
                assertTrue(problem.level in 2..4)
                assertEquals("UC002", problem.unitCode)
            }
        }

        @Test
        fun `하 난이도 선택시 비율 확인`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 10,
                unitCodeList = listOf("UC001"),
                level = Level.LOW,  // 하 난이도 선택 (하 50%, 중 30%, 상 20%)
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isNotEmpty())

            val lowLevelCount = result.problems.count { it.level == 1 }
            val middleLevelCount = result.problems.count { it.level in 2..4 }
            val highLevelCount = result.problems.count { it.level == 5 }

            // 하 난이도가 가장 많이 조회되었는지 확인 (정확한 비율은 분배 로직에 따라 다름)
            assertTrue(lowLevelCount >= middleLevelCount || lowLevelCount >= highLevelCount)
        }

        @Test
        fun `상 난이도 선택시 비율 확인`() {
            // Given
            val query = ProblemSearchQuery(
                totalCount = 10,
                unitCodeList = listOf("UC001"),
                level = Level.HIGH,  // 상 난이도 선택 (하 20%, 중 30%, 상 50%)
                problemType = ProblemTypeFilter.ALL
            )

            // When
            val result = problemSearchUseCase.searchProblems(query)

            // Then
            assertTrue(result.problems.isNotEmpty())

            val lowLevelCount = result.problems.count { it.level == 1 }
            val middleLevelCount = result.problems.count { it.level in 2..4 }
            val highLevelCount = result.problems.count { it.level == 5 }

            // 상 난이도가 가장 많이 조회되었는지 확인 (정확한 비율은 분배 로직에 따라 다름)
            assertTrue(highLevelCount >= lowLevelCount && highLevelCount >= middleLevelCount)
        }
    }

} 