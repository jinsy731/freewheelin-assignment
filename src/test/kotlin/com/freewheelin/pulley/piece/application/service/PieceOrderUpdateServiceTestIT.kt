package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.application.port.PieceOrderUpdateUseCase
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PieceOrderUpdateService 통합 테스트 (PieceProblem ID 기반)
 *
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PieceOrderUpdateServiceTestIT {

    @Autowired
    private lateinit var pieceOrderUpdateUseCase: PieceOrderUpdateUseCase

    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository

    @Autowired
    private lateinit var pieceProblemJpaRepository: PieceProblemJpaRepository

    private var testPieceId = 0L
    private val testPieceProblemIds = mutableListOf<Long>()

    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        pieceProblemJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        testPieceProblemIds.clear()

        // 테스트 학습지 생성
        val testPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "순서 테스트 학습지")
        val savedPiece = pieceJpaRepository.save(testPiece)
        testPieceId = savedPiece.id

        // 5개의 문제를 순서대로 생성 (position: 1.0, 2.0, 3.0, 4.0, 5.0)
        val testPieceProblems = listOf(
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = 101L, position = 1.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = 102L, position = 2.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = 103L, position = 3.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = 104L, position = 4.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = 105L, position = 5.0)
        )

        val savedPieceProblems = pieceProblemJpaRepository.saveAll(testPieceProblems)
        testPieceProblemIds.addAll(savedPieceProblems.map { it.id })
    }

    /**
     * PieceProblem ID를 가져오는 헬퍼 메소드
     */
    private fun getPieceProblemId(problemId: Long): Long {
        return pieceProblemJpaRepository.findByPieceIdAndProblemId(testPieceId, problemId)?.id
            ?: throw IllegalStateException("PieceProblem not found for problemId: $problemId")
    }

    @Test
    fun `정상적인 경우 - 중간 위치로 문제 이동 (PieceProblem ID 기반)`() {
        // Given: PieceProblem(problemId=102)를 PieceProblem(problemId=103)과 PieceProblem(problemId=104) 사이로 이동
        val targetPieceProblemId = getPieceProblemId(102L)
        val prevPieceProblemId = getPieceProblemId(103L)
        val nextPieceProblemId = getPieceProblemId(104L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = nextPieceProblemId
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertEquals(testPieceId, result.pieceId)
        assertEquals(targetPieceProblemId, result.pieceProblemId)
        assertEquals(2.0, result.previousPosition)
        assertTrue(result.success)

        // 새로운 위치가 103번과 104번 사이에 있는지 확인
        assertTrue(result.newPosition > 3.0 && result.newPosition < 4.0)

        // DB에서 실제 순서 확인
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val positions = updatedProblems.map { it.problemId to it.position }

        // 101 < 103 < 102 < 104 < 105 순서가 되어야 함
        val problem101Position = positions.find { it.first == 101L }!!.second
        val problem102Position = positions.find { it.first == 102L }!!.second
        val problem103Position = positions.find { it.first == 103L }!!.second
        val problem104Position = positions.find { it.first == 104L }!!.second
        val problem105Position = positions.find { it.first == 105L }!!.second

        assertTrue(problem101Position < problem103Position)
        assertTrue(problem103Position < problem102Position)
        assertTrue(problem102Position < problem104Position)
        assertTrue(problem104Position < problem105Position)
    }

    @Test
    fun `맨 앞으로 이동 - prevPieceProblemId null`() {
        // Given: PieceProblem(problemId=104)를 맨 앞으로 이동
        val targetPieceProblemId = getPieceProblemId(104L)
        val nextPieceProblemId = getPieceProblemId(101L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = null,
            nextPieceProblemId = nextPieceProblemId
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(4.0, result.previousPosition)

        // 새로운 위치가 101번 앞에 있는지 확인 (0.5)
        assertEquals(0.5, result.newPosition)

        // DB에서 실제 순서 확인 - 104가 맨 앞에 있어야 함
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        assertEquals(104L, updatedProblems.first().problemId)
    }

    @Test
    fun `맨 뒤로 이동 - nextPieceProblemId null`() {
        // Given: PieceProblem(problemId=102)를 맨 뒤로 이동
        val targetPieceProblemId = getPieceProblemId(102L)
        val prevPieceProblemId = getPieceProblemId(105L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = null
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(2.0, result.previousPosition)

        // 새로운 위치가 105번 뒤에 있는지 확인 (6.0)
        assertEquals(6.0, result.newPosition)

        // DB에서 실제 순서 확인 - 102가 맨 뒤에 있어야 함
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        assertEquals(102L, updatedProblems.last().problemId)
    }

    @Test
    fun `첫 번째와 두 번째 사이로 이동`() {
        // Given: PieceProblem(problemId=105)를 PieceProblem(problemId=101)과 PieceProblem(problemId=102) 사이로 이동
        val targetPieceProblemId = getPieceProblemId(105L)
        val prevPieceProblemId = getPieceProblemId(101L)
        val nextPieceProblemId = getPieceProblemId(102L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = nextPieceProblemId
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(5.0, result.previousPosition)

        // 새로운 위치가 1.0과 2.0 사이에 있는지 확인 (1.5)
        assertEquals(1.5, result.newPosition)

        // DB에서 순서 확인
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val orderedProblemIds = updatedProblems.map { it.problemId }

        // 101, 105, 102, 103, 104 순서가 되어야 함
        assertEquals(listOf(101L, 105L, 102L, 103L, 104L), orderedProblemIds)
    }

    @Test
    fun `권한이 없는 선생님이 순서 변경하려는 경우 - AuthorizationException 발생`() {
        // Given
        val targetPieceProblemId = getPieceProblemId(102L)
        val prevPieceProblemId = getPieceProblemId(103L)
        val nextPieceProblemId = getPieceProblemId(104L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 2L, // 다른 선생님 ID (학습지 소유자는 teacherId=1)
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = nextPieceProblemId
        )

        // When & Then
        assertThrows<AuthorizationException> {
            pieceOrderUpdateUseCase.updateProblemOrder(command)
        }

        // 원래 순서가 그대로 유지되었는지 확인
        val problems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val problemIds = problems.map { it.problemId }
        assertEquals(listOf(101L, 102L, 103L, 104L, 105L), problemIds)
    }

    @Test
    fun `존재하지 않는 학습지에서 순서 변경 - NotFoundException 발생`() {
        // Given
        val nonExistentPieceId = 999L
        val targetPieceProblemId = getPieceProblemId(102L)
        val prevPieceProblemId = getPieceProblemId(103L)
        val nextPieceProblemId = getPieceProblemId(104L)

        val command = ProblemOrderUpdateCommand(
            pieceId = nonExistentPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = nextPieceProblemId
        )

        // When & Then
        assertThrows<NotFoundException> {
            pieceOrderUpdateUseCase.updateProblemOrder(command)
        }
    }

    @Test
    fun `이전 PieceProblem만 지정하여 이동 (맨 뒤로)`() {
        // Given: PieceProblem(problemId=101)을 PieceProblem(problemId=105) 뒤로 이동
        val targetPieceProblemId = getPieceProblemId(101L)
        val prevPieceProblemId = getPieceProblemId(105L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = null
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(1.0, result.previousPosition)
        assertEquals(6.0, result.newPosition) // 105의 position(5.0) + 1.0

        // DB에서 순서 확인 - 101이 맨 뒤에 있어야 함
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val orderedProblemIds = updatedProblems.map { it.problemId }
        assertEquals(listOf(102L, 103L, 104L, 105L, 101L), orderedProblemIds)
    }

    @Test
    fun `다음 PieceProblem만 지정하여 이동 (맨 앞으로)`() {
        // Given: PieceProblem(problemId=105)를 PieceProblem(problemId=101) 앞으로 이동
        val targetPieceProblemId = getPieceProblemId(105L)
        val nextPieceProblemId = getPieceProblemId(101L)

        val command = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = targetPieceProblemId,
            prevPieceProblemId = null,
            nextPieceProblemId = nextPieceProblemId
        )

        // When
        val result = pieceOrderUpdateUseCase.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(5.0, result.previousPosition)
        assertEquals(0.5, result.newPosition) // 101의 position(1.0) / 2

        // DB에서 순서 확인 - 105가 맨 앞에 있어야 함
        val updatedProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val orderedProblemIds = updatedProblems.map { it.problemId }
        assertEquals(listOf(105L, 101L, 102L, 103L, 104L), orderedProblemIds)
    }

    @Test
    fun `복수 개의 순서 변경 - 연속적인 이동`() {
        // Given: 여러 문제를 연속으로 이동

        // 1단계: PieceProblem(problemId=105)를 PieceProblem(problemId=101)과 PieceProblem(problemId=102) 사이로 이동
        val command1 = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = getPieceProblemId(105L),
            prevPieceProblemId = getPieceProblemId(101L),
            nextPieceProblemId = getPieceProblemId(102L)
        )

        // 2단계: PieceProblem(problemId=104)를 맨 앞으로 이동
        val command2 = ProblemOrderUpdateCommand(
            pieceId = testPieceId,
            teacherId = 1L,
            pieceProblemId = getPieceProblemId(104L),
            prevPieceProblemId = null,
            nextPieceProblemId = getPieceProblemId(101L)
        )

        // When
        val result1 = pieceOrderUpdateUseCase.updateProblemOrder(command1)
        val result2 = pieceOrderUpdateUseCase.updateProblemOrder(command2)

        // Then
        assertTrue(result1.success)
        assertTrue(result2.success)

        // 최종 순서 확인: 104, 101, 105, 102, 103
        val finalProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(testPieceId)
        val finalOrder = finalProblems.map { it.problemId }
        assertEquals(listOf(104L, 101L, 105L, 102L, 103L), finalOrder)
    }
} 