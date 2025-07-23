package com.freewheelin.pulley.assignment.domain.model

import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.ProblemCount
import com.freewheelin.pulley.common.domain.StudentId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

/**
 * Assignment 도메인 모델 단위 테스트
 */
@DisplayName("Assignment 도메인 모델")
class AssignmentTest {

    @Nested
    @DisplayName("create 팩토리 메서드")
    inner class CreateTest {

        @Test
        fun `새로운 출제 생성 - 성공`() {
            // given
            val pieceId = PieceId(1L)
            val studentId = StudentId(100L)
            val assignedAt = LocalDateTime.of(2024, 1, 15, 10, 0)

            // when
            val assignment = Assignment.create(
                pieceId = pieceId,
                studentId = studentId,
                assignedAt = assignedAt
            )

            // then
            assertEquals(AssignmentId(0), assignment.id) // JPA 자동 생성 임시 ID
            assertEquals(pieceId, assignment.pieceId)
            assertEquals(studentId, assignment.studentId)
            assertEquals(assignedAt, assignment.assignedAt)
            assertEquals(null, assignment.submittedAt)
            assertEquals(null, assignment.correctnessRate)
        }

        @Test
        fun `새로운 출제 생성 - 기본 시간으로 생성`() {
            // given
            val pieceId = PieceId(1L)
            val studentId = StudentId(100L)
            val beforeCreate = LocalDateTime.now()

            // when
            val assignment = Assignment.create(
                pieceId = pieceId,
                studentId = studentId
            )

            // then
            val afterCreate = LocalDateTime.now()
            assertTrue(assignment.assignedAt >= beforeCreate)
            assertTrue(assignment.assignedAt <= afterCreate)
            assertEquals(null, assignment.submittedAt)
            assertEquals(null, assignment.correctnessRate)
        }
    }

    @Nested
    @DisplayName("submit 메서드")
    inner class SubmitTest {

        @Test
        fun `제출 완료 처리 - 성공`() {
            // given
            val assignment = createSampleAssignment()
            val submissions = listOf(
                Submission(1L, 1L, 1L, "답안1", true),
                Submission(2L, 1L, 2L, "답안2", false),
                Submission(3L, 1L, 3L, "답안3", true)
            )
            val totalProblems = ProblemCount(3)
            val beforeSubmit = LocalDateTime.now()

            // when
            val submittedAssignment = assignment.submit(submissions, totalProblems)

            // then
            val afterSubmit = LocalDateTime.now()
            assertTrue(submittedAssignment.submittedAt!! >= beforeSubmit)
            assertTrue(submittedAssignment.submittedAt!! <= afterSubmit)
            assertEquals(CorrectnessRate(2.0 / 3.0), submittedAssignment.correctnessRate)
            
            // 원본 객체는 변경되지 않음 (불변성 확인)
            assertEquals(null, assignment.submittedAt)
            assertEquals(null, assignment.correctnessRate)
        }

        @Test
        fun `제출 완료 처리 - 빈 제출 리스트로 실패`() {
            // given
            val assignment = createSampleAssignment()
            val emptySubmissions = emptyList<Submission>()
            val totalProblems = ProblemCount(5)

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                assignment.submit(emptySubmissions, totalProblems)
            }
            assertEquals("제출할 답안이 없습니다.", exception.message)
        }
    }

    @Nested
    @DisplayName("isSubmitted 메서드")
    inner class IsSubmittedTest {

        @Test
        fun `제출 전 상태 확인`() {
            // given
            val assignment = createSampleAssignment()

            // when & then
            assertFalse(assignment.isSubmitted())
        }

        @Test
        fun `제출 후 상태 확인`() {
            // given
            val assignment = createSampleAssignment()
            val submissions = listOf(
                Submission(1L, 1L, 1L, "답안1", true)
            )
            val submittedAssignment = assignment.submit(submissions, ProblemCount(1))

            // when & then
            assertTrue(submittedAssignment.isSubmitted())
        }

        @Test
        fun `제출 시간이 null이 아니면 제출된 것으로 판단`() {
            // given
            val assignment = Assignment(
                id = AssignmentId(1L),
                pieceId = PieceId(1L),
                studentId = StudentId(100L),
                assignedAt = LocalDateTime.now(),
                submittedAt = LocalDateTime.now(),
                correctnessRate = CorrectnessRate(0.8)
            )

            // when & then
            assertTrue(assignment.isSubmitted())
        }
    }
    
    private fun createSampleAssignment(
        id: AssignmentId = AssignmentId(1L),
        pieceId: PieceId = PieceId(1L),
        studentId: StudentId = StudentId(100L),
        assignedAt: LocalDateTime = LocalDateTime.of(2024, 1, 15, 10, 0)
    ): Assignment {
        return Assignment(
            id = id,
            pieceId = pieceId,
            studentId = studentId,
            assignedAt = assignedAt,
            submittedAt = null,
            correctnessRate = null
        )
    }
} 