package com.freewheelin.pulley.assignment.domain.service

import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.infrastructure.logging.logger
import org.springframework.stereotype.Service

@Service
class AssignmentDuplicationPolicy(private val assignmentRepository: AssignmentRepository) {

    /**
     * 학습지 조각에 대해 중복 출제 여부를 확인하고, 이미 출제된 학생 ID와 아직 출제되지 않은 학생 ID를 반환합니다.
     *
     * @param pieceId 학습지 조각 ID
     * @param studentIds 학생 ID 목록
     * @return AssignmentDuplicationInfo 이미 출제된 학생 ID와 아직 출제되지 않은 학생 ID를 포함하는 객체
     */
    fun getDuplicationInfo(pieceId: Long, studentIds: List<Long>): AssignmentDuplicationInfo {
        val alreadyAssignedStudentIds = assignmentRepository.findByPieceIdAndStudentIdIn(pieceId, studentIds)
            .map { assignment -> assignment.studentId.value }
        val notAssignedStudentIds = studentIds - alreadyAssignedStudentIds

        val duplicationInfo = AssignmentDuplicationInfo(
            alreadyAssignedStudentIds = studentIds - notAssignedStudentIds,
            notAssignedStudentIds = notAssignedStudentIds
        )

        logger.info {
            """
            중복 출제 확인 완료 - pieceId: $pieceId,
            기존출제: ${duplicationInfo.alreadyAssignedStudentIds.size}명, 
            신규출제: ${duplicationInfo.notAssignedStudentIds.size}명
            """.trimIndent()
        }

        return duplicationInfo
    }
}

data class AssignmentDuplicationInfo(
    val alreadyAssignedStudentIds: List<Long>,
    val notAssignedStudentIds: List<Long>
) {
    fun hasAssignableStudents(): Boolean {
        return notAssignedStudentIds.isNotEmpty()
    }
}