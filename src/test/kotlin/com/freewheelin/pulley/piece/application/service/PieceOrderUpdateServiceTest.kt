package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.common.exception.ValidationException
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PieceOrderUpdateService 단위 테스트 (PieceProblem ID 기반)
 */
class PieceOrderUpdateServiceTest {

    private val pieceRepository = mockk<PieceRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()

    private val service = PieceOrderUpdateService(
        pieceRepository,
        pieceProblemRepository
    )

    private fun createTestPiece(teacherId: Long = 1L): Piece {
        return Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
    }

    private fun createTestProblem(id: Long, pieceId: Long, problemId: Long, position: Double): PieceProblem {
        return PieceProblem(
            id = id,
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(position)
        )
    }

    @Test
    fun `정상적인 경우 - 연속된 두 문제 사이로 이동`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 4L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 2L,
            nextPieceProblemId = 3L
        )

        val piece = createTestPiece(teacherId)

        // 필요한 문제들: 이동할 문제(4), 이전 문제(2), 다음 문제(3)
        val requiredProblems = listOf(
            createTestProblem(2L, pieceId, 102L, 2.0),
            createTestProblem(3L, pieceId, 103L, 3.0),
            createTestProblem(4L, pieceId, 104L, 4.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 2L, 3L) } returns requiredProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0

        // When
        val result = service.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(pieceId, result.pieceId)
        assertEquals(pieceProblemId, result.pieceProblemId)
        assertEquals(4.0, result.previousPosition)
        assertEquals(2.5, result.newPosition) // (2.0 + 3.0) / 2

        // 저장이 호출되었는지 확인
        verify { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `정상적인 경우 - 맨 앞으로 이동`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 3L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = null,
            nextPieceProblemId = 1L
        )

        val piece = createTestPiece(teacherId)
        val requiredProblems = listOf(
            createTestProblem(1L, pieceId, 101L, 1.0),
            createTestProblem(3L, pieceId, 103L, 3.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, null, 1L) } returns requiredProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0

        // When
        val result = service.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(3.0, result.previousPosition)
        assertEquals(0.5, result.newPosition) // 1.0 / 2

        verify { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `정상적인 경우 - 맨 뒤로 이동`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 1L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 3L,
            nextPieceProblemId = null
        )

        val piece = createTestPiece(teacherId)
        val requiredProblems = listOf(
            createTestProblem(1L, pieceId, 101L, 1.0),
            createTestProblem(3L, pieceId, 103L, 3.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 3L, null) } returns requiredProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0

        // When
        val result = service.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(1.0, result.previousPosition)
        assertEquals(4.0, result.newPosition) // 3.0 + 1.0

        verify { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `이동 불필요 - 이미 올바른 위치에 있는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 2L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 1L,
            nextPieceProblemId = 3L
        )

        val piece = createTestPiece(teacherId)
        val requiredProblems = listOf(
            createTestProblem(1L, pieceId, 101L, 1.0),
            createTestProblem(2L, pieceId, 102L, 2.0), // 이미 올바른 위치
            createTestProblem(3L, pieceId, 103L, 3.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 1L, 3L) } returns requiredProblems

        // When
        val result = service.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(2.0, result.previousPosition)
        assertEquals(2.0, result.newPosition) // 변경 없음

        // 저장이 호출되지 않았는지 확인 (이동 불필요)
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `도메인 예외 전파 - 연속성 검증 실패`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 4L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 1L, // 1과 3은 연속되지 않음 (2가 사이에 있음)
            nextPieceProblemId = 3L
        )

        val piece = createTestPiece(teacherId)
        val requiredProblems = listOf(
            createTestProblem(1L, pieceId, 101L, 1.0),
            createTestProblem(2L, pieceId, 102L, 2.0), // 사이에 있는 문제
            createTestProblem(3L, pieceId, 103L, 3.0),
            createTestProblem(4L, pieceId, 104L, 4.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 1L, 3L) } returns requiredProblems

        // When & Then
        val exception = assertThrows<ValidationException> {
            service.updateProblemOrder(command)
        }

        assertTrue(exception.message!!.contains("연속되어 있지 않습니다"))

        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `권한 없음 - 다른 선생님의 학습지 수정 시도`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val requestingTeacherId = 999L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = requestingTeacherId,
            pieceProblemId = 1L,
            prevPieceProblemId = null,
            nextPieceProblemId = null
        )

        val piece = createTestPiece(teacherId) // 실제 소유자는 teacherId=1

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece

        // When & Then
        assertThrows<AuthorizationException> {
            service.updateProblemOrder(command)
        }

        // Repository 조회도 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.findPieceProblemsForOrderUpdate(any(), any(), any(), any()) }
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `빈 결과 - 학습지에 문제가 없거나 지정된 PieceProblem을 찾을 수 없는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 999L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = null,
            nextPieceProblemId = null
        )

        val piece = createTestPiece(teacherId)

        // Mocking - 빈 리스트 반환
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, null, null) } returns emptyList()

        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }

        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `학습지 없음 - 존재하지 않는 학습지 ID`() {
        // Given
        val pieceId = 9999L
        val teacherId = 1L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = 1L,
            prevPieceProblemId = null,
            nextPieceProblemId = null
        )

        // Mocking - 학습지가 없음
        every { pieceRepository.getById(pieceId) } throws NotFoundException(
            ErrorCode.PIECE_NOT_FOUND,
            "학습지를 찾을 수 없습니다: $pieceId"
        )

        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }

        verify(exactly = 0) { pieceProblemRepository.findPieceProblemsForOrderUpdate(any(), any(), any(), any()) }
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `이동할 PieceProblem 없음 - Repository에서 찾았지만 일급 컬렉션에서 없는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 999L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 1L,
            nextPieceProblemId = 2L
        )

        val piece = createTestPiece(teacherId)
        // 이동할 PieceProblem(999)는 없고 다른 문제들만 있음
        val requiredProblems = listOf(
            createTestProblem(1L, pieceId, 101L, 1.0),
            createTestProblem(2L, pieceId, 102L, 2.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 1L, 2L) } returns requiredProblems

        // When & Then
        val exception = assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }

        assertTrue(exception.message!!.contains("PieceProblem을 찾을 수 없습니다: $pieceProblemId"))

        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }

    @Test
    fun `복잡한 시나리오 - 여러 문제가 포함된 연속성 검증`() {
        // Given: 1(1.0), 2(2.0), 3(3.0), 4(4.0), 5(5.0)
        // 5를 2와 3 사이로 이동 (연속됨)
        val pieceId = 100L
        val teacherId = 1L
        val pieceProblemId = 5L

        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            pieceProblemId = pieceProblemId,
            prevPieceProblemId = 2L,
            nextPieceProblemId = 3L
        )

        val piece = createTestPiece(teacherId)
        val requiredProblems = listOf(
            createTestProblem(2L, pieceId, 102L, 2.0),
            createTestProblem(3L, pieceId, 103L, 3.0),
            createTestProblem(5L, pieceId, 105L, 5.0)
        )

        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findPieceProblemsForOrderUpdate(pieceId, pieceProblemId, 2L, 3L) } returns requiredProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0

        // When
        val result = service.updateProblemOrder(command)

        // Then
        assertTrue(result.success)
        assertEquals(5.0, result.previousPosition)
        assertEquals(2.5, result.newPosition)

        verify { pieceProblemRepository.save(any()) }
    }
} 