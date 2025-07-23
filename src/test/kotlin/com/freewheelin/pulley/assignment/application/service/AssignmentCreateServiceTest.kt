package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.common.exception.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignmentCreateServiceTest {
    
    private val assignmentRepository = mockk<AssignmentRepository>()
    private val pieceRepository = mockk<PieceRepository>()
    private val assignmentCreateService = AssignmentCreateService(
        assignmentRepository,
        pieceRepository
    )
    
    @Test
    fun `assignPiece - 정상 출제 테스트 (모든 학생이 새로 출제)`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = 100L,
            studentIds = listOf(10L, 20L, 30L)
        )
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        val savedAssignments = listOf(
            Assignment(
                id = AssignmentId(1L),
                pieceId = PieceId(100L),
                studentId = StudentId(10L),
                assignedAt = LocalDateTime.now()
            ),
            Assignment(
                id = AssignmentId(2L),
                pieceId = PieceId(100L),
                studentId = StudentId(20L),
                assignedAt = LocalDateTime.now()
            ),
            Assignment(
                id = AssignmentId(3L),
                pieceId = PieceId(100L),
                studentId = StudentId(30L),
                assignedAt = LocalDateTime.now()
            )
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { 
            assignmentRepository.findByPieceIdAndStudentIdIn(100L, listOf(10L, 20L, 30L)) 
        } returns emptyList()
        every { assignmentRepository.saveAll(any()) } returns savedAssignments
        
        // When
        val result = assignmentCreateService.assignPiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals(3, result.totalRequestedStudents)
        assertEquals(3, result.newlyAssignedStudents)
        assertEquals(0, result.alreadyAssignedStudents)
        assertEquals(listOf(10L, 20L, 30L), result.newlyAssignedStudentIds)
        assertEquals(emptyList(), result.alreadyAssignedStudentIds)
        assertTrue(result.isAllStudentsProcessed)
        
        // saveAll이 3개의 Assignment와 함께 호출되었는지 확인
        val assignmentsSlot = slot<List<Assignment>>()
        verify { assignmentRepository.saveAll(capture(assignmentsSlot)) }
        
        val capturedAssignments = assignmentsSlot.captured
        assertEquals(3, capturedAssignments.size)
        assertEquals(listOf(10L, 20L, 30L), capturedAssignments.map { it.studentId.value })
        assertTrue(capturedAssignments.all { it.pieceId.value == 100L })
        assertTrue(capturedAssignments.all { it.submittedAt == null })
        assertTrue(capturedAssignments.all { it.correctnessRate == null })
    }
    
    @Test
    fun `assignPiece - 일부 학생이 이미 출제되어 있는 경우 출제되지 않은 학샘에게만 출제된다`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = 100L,
            studentIds = listOf(10L, 20L, 30L)
        )
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        val existingAssignment = Assignment(
            id = AssignmentId(1L),
            pieceId = PieceId(100L),
            studentId = StudentId(20L),  // 20L은 이미 출제됨
            assignedAt = LocalDateTime.now().minusDays(1),
            submittedAt = null,
            correctnessRate = null
        )
        
        val savedAssignments = listOf(
            Assignment(
                id = AssignmentId(2L),
                pieceId = PieceId(100L),
                studentId = StudentId(10L),
                assignedAt = LocalDateTime.now()
            ),
            Assignment(
                id = AssignmentId(3L),
                pieceId = PieceId(100L),
                studentId = StudentId(30L),
                assignedAt = LocalDateTime.now()
            )
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { 
            assignmentRepository.findByPieceIdAndStudentIdIn(100L, listOf(10L, 20L, 30L)) 
        } returns listOf(existingAssignment)
        every { assignmentRepository.saveAll(any()) } returns savedAssignments
        
        // When
        val result = assignmentCreateService.assignPiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals(3, result.totalRequestedStudents)
        assertEquals(2, result.newlyAssignedStudents)  // 10L, 30L만 새로 출제
        assertEquals(1, result.alreadyAssignedStudents)  // 20L은 이미 출제됨
        assertEquals(listOf(10L, 30L), result.newlyAssignedStudentIds)
        assertEquals(listOf(20L), result.alreadyAssignedStudentIds)
        assertTrue(result.isAllStudentsProcessed)
        
        // saveAll이 2개의 Assignment와 함께 호출되었는지 확인
        val assignmentsSlot = slot<List<Assignment>>()
        verify { assignmentRepository.saveAll(capture(assignmentsSlot)) }
        
        val capturedAssignments = assignmentsSlot.captured
        assertEquals(2, capturedAssignments.size)
        assertEquals(listOf(10L, 30L), capturedAssignments.map { it.studentId.value })
    }
    
    @Test
    fun `assignPiece - 모든 학생이 이미 출제되어 있는 경우 새로 출제되는 학습지는 없다`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = 100L,
            studentIds = listOf(10L, 20L)
        )
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        val existingAssignments = listOf(
            Assignment(
                id = AssignmentId(1L),
                pieceId = PieceId(100L),
                studentId = StudentId(10L),
                assignedAt = LocalDateTime.now().minusDays(1),
                submittedAt = null,
                correctnessRate = null
            ),
            Assignment(
                id = AssignmentId(2L),
                pieceId = PieceId(100L),
                studentId = StudentId(20L),
                assignedAt = LocalDateTime.now().minusDays(1),
                submittedAt = null,
                correctnessRate = null
            )
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { 
            assignmentRepository.findByPieceIdAndStudentIdIn(100L, listOf(10L, 20L)) 
        } returns existingAssignments
        
        // When
        val result = assignmentCreateService.assignPiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals(2, result.totalRequestedStudents)
        assertEquals(0, result.newlyAssignedStudents)
        assertEquals(2, result.alreadyAssignedStudents)
        assertEquals(emptyList(), result.newlyAssignedStudentIds)
        assertEquals(listOf(10L, 20L), result.alreadyAssignedStudentIds)
        assertTrue(result.isAllStudentsProcessed)
        
        // saveAll이 호출되지 않아야 함 (새로 출제할 학생이 없음)
        verify(exactly = 0) { assignmentRepository.saveAll(any()) }
    }
    
    @Test
    fun `assignPiece - 존재하지 않는 학습지로 출제 시도시 NotFoundException 발생`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = 999L,  // 존재하지 않는 학습지
            studentIds = listOf(10L, 20L)
        )
        
        every { pieceRepository.getById(999L) } throws NotFoundException(
            ErrorCode.PIECE_NOT_FOUND,
            999L
        )
        
        // When & Then
        val exception = assertThrows<NotFoundException> {
            assignmentCreateService.assignPiece(request)
        }
        
        assertEquals(ErrorCode.PIECE_NOT_FOUND, exception.errorCode)
    }
    
    @Test
    fun `assignPiece - 다른 선생님의 학습지로 출제 시도시 AuthorizationException 발생`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 999L,  // 다른 선생님
            pieceId = 100L,
            studentIds = listOf(10L, 20L)
        )
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),  // 선생님 ID 1L
            name = PieceName("수학 학습지")
        )
        
        every { pieceRepository.getById(100L) } returns piece
        
        // When & Then
        val exception = assertThrows<AuthorizationException> {
            assignmentCreateService.assignPiece(request)
        }
        
        assertEquals(ErrorCode.PIECE_UNAUTHORIZED, exception.errorCode)
    }
    
    @Test
    fun `assignPiece - 단일 학생 출제 테스트`() {
        // Given
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = 100L,
            studentIds = listOf(10L)  // 단일 학생
        )
        
        val piece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        val savedAssignment = Assignment(
            id = AssignmentId(1L),
            pieceId = PieceId(100L),
            studentId = StudentId(10L),
            assignedAt = LocalDateTime.now()
        )
        
        every { pieceRepository.getById(100L) } returns piece
        every { 
            assignmentRepository.findByPieceIdAndStudentIdIn(100L, listOf(10L)) 
        } returns emptyList()
        every { assignmentRepository.saveAll(any()) } returns listOf(savedAssignment)
        
        // When
        val result = assignmentCreateService.assignPiece(request)
        
        // Then
        assertEquals(1, result.totalRequestedStudents)
        assertEquals(1, result.newlyAssignedStudents)
        assertEquals(0, result.alreadyAssignedStudents)
        assertEquals(listOf(10L), result.newlyAssignedStudentIds)
    }
} 