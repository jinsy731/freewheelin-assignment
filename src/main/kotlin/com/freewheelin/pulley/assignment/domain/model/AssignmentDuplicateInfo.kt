package com.freewheelin.pulley.assignment.domain.model

/**
 * 출제 중복 정보를 캡슐화하는 값 객체
 */
data class AssignmentDuplicateInfo(
    val alreadyAssignedStudentIds: List<String>,
    val newStudentIds: List<String>
) {
    val hasNewStudents: Boolean get() = newStudentIds.isNotEmpty()
    val hasExistingAssignments: Boolean get() = alreadyAssignedStudentIds.isNotEmpty()
    val newStudentCount: Int get() = newStudentIds.size
    val existingStudentCount: Int get() = alreadyAssignedStudentIds.size
    
    companion object {
        fun from(requestedStudentIds: List<String>, existingAssignments: List<Assignment>): AssignmentDuplicateInfo {
            val alreadyAssignedStudentIds = existingAssignments.map { it.studentId.value.toString() }
            val newStudentIds = requestedStudentIds.filterNot { studentId ->
                alreadyAssignedStudentIds.contains(studentId)
            }
            
            return AssignmentDuplicateInfo(
                alreadyAssignedStudentIds = alreadyAssignedStudentIds,
                newStudentIds = newStudentIds
            )
        }
    }
}