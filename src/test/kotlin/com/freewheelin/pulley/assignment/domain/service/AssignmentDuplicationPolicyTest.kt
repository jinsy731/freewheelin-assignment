package com.freewheelin.pulley.assignment.domain.service

import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AssignmentDuplicationPolicyTest {

    private val assignmentRepository = mockk<AssignmentRepository>()
    private val sut = AssignmentDuplicationPolicy(assignmentRepository)

    @Test
    @DisplayName("이미 출제된 학생과 아직 출제되지 않은 학생 ID를 반환한다")
    fun `getDuplicationInfo - return studentIds of already assigned and not assigned yet`() {
        // arrange
        val pieceId = 1L
        val studentIds = listOf(1L, 2L, 3L, 4L, 5L)

        every { assignmentRepository.findByPieceIdAndStudentIdIn(pieceId, studentIds) }
            .returns(listOf(
                Assignment(AssignmentId(1L), pieceId = PieceId(pieceId), studentId = StudentId(1L), assignedAt = java.time.LocalDateTime.now()),
                Assignment(AssignmentId(2L), pieceId = PieceId(pieceId), studentId = StudentId(2L), assignedAt = java.time.LocalDateTime.now()),
            ))

        // act
        val duplicationInfo = sut.getDuplicationInfo(pieceId, studentIds)

        // assert
        duplicationInfo.notAssignedStudentIds shouldBe listOf(3L, 4L, 5L)
        duplicationInfo.alreadyAssignedStudentIds shouldBe listOf(1L, 2L)
    }
}