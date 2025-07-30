package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateResult
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.assignment.domain.service.AssignmentDuplicationInfo
import com.freewheelin.pulley.assignment.domain.service.AssignmentDuplicationPolicy
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import com.freewheelin.pulley.common.domain.validateOwnership
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 학습지 출제 Application Service
 */
@Service
@Transactional
class AssignmentCreateService(
    private val assignmentDuplicationPolicy: AssignmentDuplicationPolicy,
    private val assignmentRepository: AssignmentRepository,
    private val pieceRepository: PieceRepository
) : AssignmentCreateUseCase {
    
    override fun assignPiece(request: AssignmentCreateRequest): AssignmentCreateResult {
        logAssignmentStart(request)
        
        return try {
            validatePieceOwnership(request.pieceId, request.teacherId)
            val duplicateInfo = assignmentDuplicationPolicy.getDuplicationInfo(request.pieceId, request.studentIds)
            val savedAssignments = createAndSaveNewAssignments(request.pieceId, duplicateInfo)
            
            AssignmentCreateResult.from(
                request = request,
                alreadyAssignedStudentIds = duplicateInfo.alreadyAssignedStudentIds,
                newlyAssignedStudentIds = savedAssignments.map { it.studentId.value }
            ).also { result ->
                logAssignmentCompletion(result)
            }
        } catch (e: Exception) {
            logAssignmentFailure(request, e)
            throw e
        }
    }
    
    private fun validatePieceOwnership(pieceId: Long, teacherId: Long): Piece {
        val piece = pieceRepository.getById(pieceId)
        piece.validateOwnership(teacherId)
        logger.debug { "학습지 권한 확인 완료 - pieceId: $pieceId, teacherId: $teacherId" }
        return piece
    }
    
    private fun createAndSaveNewAssignments(pieceId: Long, duplicationInfo: AssignmentDuplicationInfo): List<Assignment> {
        if (!duplicationInfo.hasAssignableStudents()) {
            logger.info { "신규 출제할 학생 없음 - 모든 학생이 이미 출제됨" }
            return emptyList()
        }
        
        val newAssignments = duplicationInfo.notAssignedStudentIds.map { studentId ->
            Assignment.create(
                pieceId = PieceId(pieceId),
                studentId = StudentId(studentId)
            )
        }
        
        return assignmentRepository.saveAll(newAssignments).also { savedAssignments ->
            logger.info { 
                "신규 출제 완료 - pieceId: $pieceId, 출제수: ${savedAssignments.size}, " +
                "assignmentIds: ${savedAssignments.map { it.id.value }}" 
            }
        }
    }
    

    
    private fun logAssignmentStart(request: AssignmentCreateRequest) {
        logger.info { 
            "학습지 출제 시작 - pieceId: ${request.pieceId}, teacherId: ${request.teacherId}, " +
            "studentCount: ${request.studentIds.size}, students: ${request.studentIds}" 
        }
    }
    
    private fun logAssignmentCompletion(result: AssignmentCreateResult) {
        logger.info { 
            "학습지 출제 완료 - pieceId: ${result.pieceId}, " +
            "전체요청: ${result.totalRequestedStudents}명, " +
            "신규출제: ${result.newlyAssignedStudents}명, " +
            "기존출제: ${result.alreadyAssignedStudents}명" 
        }
    }
    
    private fun logAssignmentFailure(request: AssignmentCreateRequest, e: Exception) {
        logger.error(e) { 
            "학습지 출제 실패 - pieceId: ${request.pieceId}, teacherId: ${request.teacherId}, " +
            "error: ${e.message}" 
        }
    }
} 