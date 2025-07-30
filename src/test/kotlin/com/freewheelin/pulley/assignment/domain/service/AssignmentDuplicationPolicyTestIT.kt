package com.freewheelin.pulley.assignment.domain.service

import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.common.fixture.AssignmentFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AssignmentDuplicationPolicyTestIT {

    @Autowired
    private lateinit var sut: AssignmentDuplicationPolicy

    @Autowired
    private lateinit var assignmentRepository: AssignmentRepository

    @Test
    @DisplayName("이미 출제된 학생과 아직 출제되지 않은 학생 ID를 반환한다")
    fun `getDuplicationInfo - return studentIds of already assigned and not assigned yet`() {
        // arrange
        val assignment1 = AssignmentFixture.create(
            pieceId = PieceId(1L),
            studentId = StudentId(1L)
        )

        val assignment2 = AssignmentFixture.create(
            pieceId = PieceId(1L),
            studentId = StudentId(2L)
        )

        assignmentRepository.saveAll(listOf(assignment1, assignment2))

        val pieceId = 1L
        val studentIds = listOf(1L, 2L, 3L, 4L, 5L)

        // act
        val duplicationInfo = sut.getDuplicationInfo(pieceId, studentIds)

        // assert
        duplicationInfo.notAssignedStudentIds shouldBe listOf(3L, 4L, 5L)
        duplicationInfo.alreadyAssignedStudentIds shouldBe listOf(1L, 2L)
    }
}