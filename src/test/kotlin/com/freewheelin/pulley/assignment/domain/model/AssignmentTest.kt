package com.freewheelin.pulley.assignment.domain.model

import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.common.domain.validateOwnership
import com.freewheelin.pulley.common.exception.AuthorizationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

class AssignmentTest {

    @Test
    fun `유효한 정보로 새로운 출제를 생성할 수 있다`() {
        // Given
        val pieceId = PieceId(1L)
        val studentId = StudentId(1L)

        // When
        val assignment = Assignment.create(pieceId, studentId)

        // Then
        assertThat(assignment.pieceId).isEqualTo(pieceId)
        assertThat(assignment.studentId).isEqualTo(studentId)
        assertThat(assignment.id.value).isEqualTo(0L) // JPA가 자동 생성할 임시 ID
        assertThat(assignment.submittedAt).isNull()
        assertThat(assignment.correctnessRate).isNull()
    }

    @Test
    fun `학생 ID로 소유자 확인이 정확하게 동작한다`() {
        // Given
        val ownerId = 1L
        val otherId = 2L
        val assignment = createTestAssignment(studentId = StudentId(ownerId))

        // When & Then
        assertThat(assignment.isOwnedBy(ownerId)).isTrue()
        assertThat(assignment.isOwnedBy(otherId)).isFalse()
    }

    @Test
    fun `소유자일 때 소유권 검증이 성공한다`() {
        // Given
        val studentId = 1L
        val assignment = createTestAssignment(studentId = StudentId(studentId))

        // When & Then (예외가 발생하지 않아야 함)
        assignment.validateOwnership(studentId)
    }

    @Test
    fun `소유자가 아닐 때 소유권 검증이 실패한다`() {
        // Given
        val ownerId = 1L
        val requesterId = 2L
        val assignment = createTestAssignment(studentId = StudentId(ownerId))

        // When & Then
        val exception = assertThrows<AuthorizationException> {
            assignment.validateOwnership(requesterId)
        }
        
        assertThat(exception.message).contains("Assignment")
        assertThat(exception.message).contains(requesterId.toString())
    }

    @Test
    fun `제출 여부를 정확하게 확인할 수 있다`() {
        // Given
        val assignment = createTestAssignment()
        val submittedAssignment = assignment.copy(submittedAt = LocalDateTime.now())

        // When & Then
        assertThat(assignment.isSubmitted()).isFalse()
        assertThat(submittedAssignment.isSubmitted()).isTrue()
    }

    private fun createTestAssignment(
        assignmentId: AssignmentId = AssignmentId(1L),
        pieceId: PieceId = PieceId(1L),
        studentId: StudentId = StudentId(1L),
        assignedAt: LocalDateTime = LocalDateTime.now()
    ): Assignment {
        return Assignment(
            id = assignmentId,
            pieceId = pieceId,
            studentId = studentId,
            assignedAt = assignedAt,
            submittedAt = null,
            correctnessRate = null
        )
    }
}