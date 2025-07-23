package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.piece.application.port.PieceProblemsQuery
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * PieceProblemsQueryService 단위 테스트
 */
class PieceProblemsQueryServiceTest {
    
    private val assignmentRepository = mockk<AssignmentRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    
    private val service = PieceProblemsQueryService(
        assignmentRepository,
        pieceProblemRepository,
        problemRepository
    )

    @Nested
    inner class `getProblemsInPiece - 학습지 문제 목록 조회` {

        @Test
        fun `정상적인 경우 - 학습지 문제 목록 조회 성공`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 10L)

            // Mock data
            val assignment = Assignment.create(
                pieceId = PieceId(100L),
                studentId = StudentId(10L)
            )

            val pieceProblems = listOf(
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1001L),
                    position = Position(0.500000)
                ),
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1002L),
                    position = Position(1.000000)
                )
            )

            val problems = listOf(
                Problem(
                    id = 1001L,
                    answer = "답1",
                    unitCode = "UC001",
                    level = 1,
                    problemType = ProblemType.SUBJECTIVE
                ),
                Problem(
                    id = 1002L,
                    answer = "답2",
                    unitCode = "UC002",
                    level = 2,
                    problemType = ProblemType.SELECTION
                )
            )

            // Mocking
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
            every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
            every { problemRepository.findByIds(listOf(1001L, 1002L)) } returns problems

            // When
            val result = service.getProblemsInPiece(query)

            // Then
            assertEquals(2, result.problems.size)
            assertEquals(1001L, result.problems[0].id)
            assertEquals("UC001", result.problems[0].unitCode)
            assertEquals(1, result.problems[0].level)
            assertEquals(ProblemType.SUBJECTIVE, result.problems[0].type)

            assertEquals(1002L, result.problems[1].id)
            assertEquals("UC002", result.problems[1].unitCode)
            assertEquals(2, result.problems[1].level)
            assertEquals(ProblemType.SELECTION, result.problems[1].type)
        }

        @Test
        fun `권한 없음 - 해당 학생에게 출제되지 않은 학습지`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 999L)

            // Mocking - NotFoundException 발생
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 999L) } throws NotFoundException(
                ErrorCode.ASSIGNMENT_NOT_FOUND,
                "출제되지 않은 학습지입니다."
            )

            // When & Then
            assertThrows<NotFoundException> {
                service.getProblemsInPiece(query)
            }
        }

        @Test
        fun `빈 결과 - 학습지에 문제가 없는 경우`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 10L)

            val assignment = Assignment.create(
                pieceId = PieceId(100L),
                studentId = StudentId(10L)
            )

            // Mocking - 빈 문제 리스트
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
            every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns emptyList()
            every { problemRepository.findByIds(emptyList()) } returns emptyList()

            // When
            val result = service.getProblemsInPiece(query)

            // Then
            assertEquals(0, result.problems.size)
        }

        @Test
        fun `단일 문제 - 학습지에 문제가 하나만 있는 경우`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 10L)

            val assignment = Assignment.create(
                pieceId = PieceId(100L),
                studentId = StudentId(10L)
            )

            val pieceProblems = listOf(
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1001L),
                    position = Position(0.500000)
                )
            )

            val problems = listOf(
                Problem(
                    id = 1001L,
                    answer = "답1",
                    unitCode = "UC001",
                    level = 3,
                    problemType = ProblemType.SUBJECTIVE
                )
            )

            // Mocking
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
            every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
            every { problemRepository.findByIds(listOf(1001L)) } returns problems

            // When
            val result = service.getProblemsInPiece(query)

            // Then
            assertEquals(1, result.problems.size)
            assertEquals(1001L, result.problems[0].id)
            assertEquals("UC001", result.problems[0].unitCode)
            assertEquals(3, result.problems[0].level)
            assertEquals(ProblemType.SUBJECTIVE, result.problems[0].type)
        }

        @Test
        fun `문제 누락 - PieceProblem에는 있지만 실제 Problem이 없는 경우`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 10L)

            val assignment = Assignment.create(
                pieceId = PieceId(100L),
                studentId = StudentId(10L)
            )

            val pieceProblems = listOf(
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1001L),
                    position = Position(0.500000)
                ),
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1002L),
                    position = Position(1.000000)
                )
            )

            // 1002번 문제만 실제로 존재 (1001번 문제는 누락)
            val problems = listOf(
                Problem(
                    id = 1002L,
                    answer = "답2",
                    unitCode = "UC002",
                    level = 3,
                    problemType = ProblemType.SELECTION
                )
            )

            // Mocking
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
            every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
            every { problemRepository.findByIds(listOf(1001L, 1002L)) } returns problems

            // When & Then
            assertThrows<NotFoundException> {
                service.getProblemsInPiece(query)
            }
        }

        @Test
        fun `순서 유지 확인 - position 순서대로 문제가 정렬되어 반환되는지 확인`() {
            // Given
            val query = PieceProblemsQuery(pieceId = 100L, studentId = 10L)

            val assignment = Assignment.create(
                pieceId = PieceId(100L),
                studentId = StudentId(10L)
            )

            // position 순서: 1003 (0.100000) -> 1001 (0.500000) -> 1002 (0.900000)
            val pieceProblems = listOf(
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1003L),
                    position = Position(0.100000)
                ),
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1001L),
                    position = Position(0.500000)
                ),
                PieceProblem.create(
                    pieceId = PieceId(100L),
                    problemId = ProblemId(1002L),
                    position = Position(0.900000)
                )
            )

            val problems = listOf(
                Problem(
                    id = 1001L,
                    answer = "답1",
                    unitCode = "UC001",
                    level = 1,
                    problemType = ProblemType.SUBJECTIVE
                ),
                Problem(
                    id = 1002L,
                    answer = "답2",
                    unitCode = "UC002",
                    level = 2,
                    problemType = ProblemType.SELECTION
                ),
                Problem(
                    id = 1003L,
                    answer = "답3",
                    unitCode = "UC003",
                    level = 4,
                    problemType = ProblemType.SUBJECTIVE
                )
            )

            // Mocking
            every { assignmentRepository.getByPieceIdAndStudentId(100L, 10L) } returns assignment
            every { pieceProblemRepository.findByPieceIdOrderByPosition(100L) } returns pieceProblems
            every { problemRepository.findByIds(listOf(1003L, 1001L, 1002L)) } returns problems

            // When
            val result = service.getProblemsInPiece(query)

            // Then
            assertEquals(3, result.problems.size)
            // position 순서대로 반환되어야 함
            assertEquals(1003L, result.problems[0].id)  // 0.100000
            assertEquals(1001L, result.problems[1].id)  // 0.500000
            assertEquals(1002L, result.problems[2].id)  // 0.900000
        }
    }
} 