package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateResult
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.map

private val logger = KotlinLogging.logger {}

/**
 * 학습지 출제 Application Service
 */
@Service
@Transactional
class AssignmentCreateService(
    private val assignmentRepository: AssignmentRepository,
    private val pieceRepository: PieceRepository
) : AssignmentCreateUseCase {
    
    override fun assignPiece(request: AssignmentCreateRequest): AssignmentCreateResult {
        logger.info { 
            "학습지 출제 시작 - pieceId: ${request.pieceId}, teacherId: ${request.teacherId}, " +
            "studentCount: ${request.studentIds.size}, students: ${request.studentIds}" 
        }
        
        try {
            // 1. 학습지 존재 및 권한 확인
            val piece = pieceRepository.getById(request.pieceId)
            piece.validateOwnership(request.teacherId)
            logger.debug { "학습지 권한 확인 완료 - pieceId: ${request.pieceId}, teacherId: ${request.teacherId}" }
            
            // 2. 중복 출제 확인
            val existingAssignments = assignmentRepository.findByPieceIdAndStudentIdIn(
                request.pieceId, 
                request.studentIds
            )
            
            val alreadyAssignedStudentIds = existingAssignments.map { it.studentId.value }
            val newStudentIds = request.studentIds.filter { studentId ->
                !alreadyAssignedStudentIds.contains(studentId)
            }
            
            logger.info { 
                "중복 출제 확인 완료 - pieceId: ${request.pieceId}, " +
                "기존출제: ${alreadyAssignedStudentIds.size}명, 신규출제: ${newStudentIds.size}명" 
            }
            
            if (alreadyAssignedStudentIds.isNotEmpty()) {
                logger.debug { "기존 출제 학생들: $alreadyAssignedStudentIds" }
            }
            
            // 3. 새로운 출제 생성
            val newAssignments = newStudentIds.map { studentId ->
                Assignment.create(
                    pieceId = PieceId(request.pieceId),
                    studentId = StudentId(studentId)
                )
            }
            
            // 4. 저장 (빈 리스트가 아닌 경우만)
            val savedAssignments = if (newAssignments.isNotEmpty()) {
                val saved = assignmentRepository.saveAll(newAssignments)
                logger.info { 
                    "신규 출제 완료 - pieceId: ${request.pieceId}, 출제수: ${saved.size}, " +
                    "assignmentIds: ${saved.map { it.id.value }}" 
                }
                saved
            } else {
                logger.info { "신규 출제할 학생 없음 - 모든 학생이 이미 출제됨" }
                emptyList()
            }
            
            // 5. 결과 반환
            val result = AssignmentCreateResult(
                pieceId = request.pieceId,
                totalRequestedStudents = request.studentIds.size,
                newlyAssignedStudents = savedAssignments.size,
                alreadyAssignedStudents = alreadyAssignedStudentIds.size,
                newlyAssignedStudentIds = savedAssignments.map { it.studentId.value },
                alreadyAssignedStudentIds = alreadyAssignedStudentIds
            )
            
            logger.info { 
                "학습지 출제 완료 - pieceId: ${request.pieceId}, " +
                "전체요청: ${result.totalRequestedStudents}명, " +
                "신규출제: ${result.newlyAssignedStudents}명, " +
                "기존출제: ${result.alreadyAssignedStudents}명" 
            }
            
            return result
            
        } catch (e: Exception) {
            logger.error(e) { 
                "학습지 출제 실패 - pieceId: ${request.pieceId}, teacherId: ${request.teacherId}, " +
                "error: ${e.message}" 
            }
            throw e
        }
    }
} 