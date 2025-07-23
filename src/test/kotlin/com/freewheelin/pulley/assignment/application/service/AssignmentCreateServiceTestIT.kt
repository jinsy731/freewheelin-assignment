package com.freewheelin.pulley.assignment.application.service

import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaEntity
import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaRepository
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AssignmentCreateService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssignmentCreateServiceTestIT {
    
    @Autowired
    private lateinit var assignmentCreateUseCase: AssignmentCreateUseCase
    
    @Autowired
    private lateinit var assignmentJpaRepository: AssignmentJpaRepository
    
    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        assignmentJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        
        // 테스트 학습지 데이터 삽입
        val testPieces = listOf(
            PieceJpaEntity(id = 0, teacherId = 1L, name = "수학 기초"),
            PieceJpaEntity(id = 0, teacherId = 1L, name = "영어 문법"),
            PieceJpaEntity(id = 0, teacherId = 2L, name = "과학 실험"),
            PieceJpaEntity(id = 0, teacherId = 3L, name = "국어 독해")
        )
        
        pieceJpaRepository.saveAll(testPieces)
    }
    
    @Test
    fun `assignPiece - 정상 출제 테스트 (모든 학생이 새로 출제)`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = piece.id,
            studentIds = listOf(101L, 102L, 103L)
        )
        
        // When
        val result = assignmentCreateUseCase.assignPiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals(3, result.totalRequestedStudents)
        assertEquals(3, result.newlyAssignedStudents)
        assertEquals(0, result.alreadyAssignedStudents)
        assertEquals(listOf(101L, 102L, 103L), result.newlyAssignedStudentIds.sorted())
        assertTrue(result.alreadyAssignedStudentIds.isEmpty())
        assertTrue(result.isAllStudentsProcessed)
        
        // DB에 정말로 저장되었는지 확인
        val savedAssignments = assignmentJpaRepository.findByPieceId(piece.id)
        assertEquals(3, savedAssignments.size)
        assertEquals(setOf(101L, 102L, 103L), savedAssignments.map { it.studentId }.toSet())
    }
    
    @Test
    fun `assignPiece - 일부 학생이 이미 출제되어 있는 경우 출제되지 않은 학샘에게만 출제된다`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 기존 출제 데이터 생성 (학생 101, 102는 이미 출제됨)
        val existingAssignments = listOf(
            AssignmentJpaEntity(
                id = 0,
                pieceId = piece.id,
                studentId = 101L,
                assignedAt = LocalDateTime.now()
            ),
            AssignmentJpaEntity(
                id = 0,
                pieceId = piece.id,
                studentId = 102L,
                assignedAt = LocalDateTime.now()
            )
        )
        assignmentJpaRepository.saveAll(existingAssignments)
        
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = piece.id,
            studentIds = listOf(101L, 102L, 103L, 104L) // 101, 102는 이미 있고, 103, 104는 새로움
        )
        
        // When
        val result = assignmentCreateUseCase.assignPiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals(4, result.totalRequestedStudents)
        assertEquals(2, result.newlyAssignedStudents)
        assertEquals(2, result.alreadyAssignedStudents)
        assertEquals(setOf(103L, 104L), result.newlyAssignedStudentIds.toSet())
        assertEquals(setOf(101L, 102L), result.alreadyAssignedStudentIds.toSet())
        assertTrue(result.isAllStudentsProcessed)
        
        // DB에 총 4개의 출제가 있는지 확인
        val allAssignments = assignmentJpaRepository.findByPieceId(piece.id)
        assertEquals(4, allAssignments.size)
        assertEquals(setOf(101L, 102L, 103L, 104L), allAssignments.map { it.studentId }.toSet())
    }
    
    @Test
    fun `assignPiece - 모든 학생이 이미 출제되어 있는 경우 새로 출제되는 학습지는 없다`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        
        // 모든 학생이 이미 출제된 상태
        val existingAssignments = listOf(
            AssignmentJpaEntity(
                id = 0,
                pieceId = piece.id,
                studentId = 101L,
                assignedAt = LocalDateTime.now()
            ),
            AssignmentJpaEntity(
                id = 0,
                pieceId = piece.id,
                studentId = 102L,
                assignedAt = LocalDateTime.now()
            )
        )
        assignmentJpaRepository.saveAll(existingAssignments)
        
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = piece.id,
            studentIds = listOf(101L, 102L) // 모든 학생이 이미 출제됨
        )
        
        // When
        val result = assignmentCreateUseCase.assignPiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals(2, result.totalRequestedStudents)
        assertEquals(0, result.newlyAssignedStudents)
        assertEquals(2, result.alreadyAssignedStudents)
        assertTrue(result.newlyAssignedStudentIds.isEmpty())
        assertEquals(setOf(101L, 102L), result.alreadyAssignedStudentIds.toSet())
        assertTrue(result.isAllStudentsProcessed)
        
        // DB에 여전히 2개의 출제만 있는지 확인
        val allAssignments = assignmentJpaRepository.findByPieceId(piece.id)
        assertEquals(2, allAssignments.size)
    }
    
    @Test
    fun `assignPiece - 다른 선생님의 학습지로 출제 시도시 AuthorizationException 발생`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0] // teacherId = 1L인 학습지
        val request = AssignmentCreateRequest(
            teacherId = 2L, // 다른 선생님 ID
            pieceId = piece.id,
            studentIds = listOf(101L, 102L)
        )
        
        // When & Then
        assertThrows<AuthorizationException> {
            assignmentCreateUseCase.assignPiece(request)
        }
        
        // DB에 출제가 생성되지 않았는지 확인
        val assignments = assignmentJpaRepository.findByPieceId(piece.id)
        assertTrue(assignments.isEmpty())
    }
    
    @Test
    fun `assignPiece - 존재하지 않는 학습지로 출제 시도시 NotFoundException 발생`() {
        // Given
        val nonExistentPieceId = 999L
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = nonExistentPieceId,
            studentIds = listOf(101L, 102L)
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            assignmentCreateUseCase.assignPiece(request)
        }
        
        // DB에 출제가 생성되지 않았는지 확인
        val assignments = assignmentJpaRepository.findByPieceId(nonExistentPieceId)
        assertTrue(assignments.isEmpty())
    }
    
    @Test
    fun `assignPiece - 단일 학생 출제 테스트`() {
        // Given
        val piece = pieceJpaRepository.findByTeacherId(1L)[0]
        val request = AssignmentCreateRequest(
            teacherId = 1L,
            pieceId = piece.id,
            studentIds = listOf(101L)
        )
        
        // When
        val result = assignmentCreateUseCase.assignPiece(request)
        
        // Then
        assertEquals(piece.id, result.pieceId)
        assertEquals(1, result.totalRequestedStudents)
        assertEquals(1, result.newlyAssignedStudents)
        assertEquals(0, result.alreadyAssignedStudents)
        assertEquals(listOf(101L), result.newlyAssignedStudentIds)
        assertTrue(result.alreadyAssignedStudentIds.isEmpty())
        assertTrue(result.isAllStudentsProcessed)
        
        // DB 확인
        val savedAssignments = assignmentJpaRepository.findByPieceId(piece.id)
        assertEquals(1, savedAssignments.size)
        assertEquals(101L, savedAssignments[0].studentId)
    }
} 