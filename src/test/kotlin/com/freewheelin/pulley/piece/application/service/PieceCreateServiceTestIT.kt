package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceCreateRequest
import com.freewheelin.pulley.piece.application.port.PieceCreateUseCase
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaRepository
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaEntity
import com.freewheelin.pulley.problem.infrastructure.persistence.ProblemJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.first
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.sorted
import kotlin.collections.take
import kotlin.ranges.until
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.text.contains

/**
 * PieceCreateService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PieceCreateServiceTestIT {
    
    @Autowired
    private lateinit var pieceCreateUseCase: PieceCreateUseCase
    
    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository
    
    @Autowired
    private lateinit var pieceProblemJpaRepository: PieceProblemJpaRepository
    
    @Autowired
    private lateinit var problemJpaRepository: ProblemJpaRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        pieceProblemJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        problemJpaRepository.deleteAll()
        
        // 테스트 문제 데이터 삽입
        val testProblems = listOf(
            ProblemJpaEntity(
                id = 0,
                answer = "답1",
                unitCode = "UC001",
                level = 1,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답2", unitCode = "UC001", level = 2, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(
                id = 0,
                answer = "답3",
                unitCode = "UC001",
                level = 3,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답4", unitCode = "UC002", level = 1, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(
                id = 0,
                answer = "답5",
                unitCode = "UC002",
                level = 2,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답6", unitCode = "UC003", level = 3, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(
                id = 0,
                answer = "답7",
                unitCode = "UC003",
                level = 4,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답8", unitCode = "UC003", level = 5, problemType = ProblemType.SELECTION)
        )
        
        problemJpaRepository.saveAll(testProblems)
    }
    
    @Test
    fun `createPiece - 정상 생성 테스트`() {
        // Given
        val problems = problemJpaRepository.findAll()
        val selectedProblemIds = problems.take(5).map { it.id }
        
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "수학 기초 학습지",
            problemIds = selectedProblemIds
        )
        
        // When
        val result = pieceCreateUseCase.createPiece(request)
        
        // Then
        assertEquals("수학 기초 학습지", result.name)
        assertTrue(result.pieceId > 0)
        
        // DB에 학습지가 저장되었는지 확인
        val savedPiece = pieceJpaRepository.findById(result.pieceId).orElse(null)
        assertEquals(1L, savedPiece.teacherId)
        assertEquals("수학 기초 학습지", savedPiece.name)
        
        // PieceProblem 매핑이 올바르게 생성되었는지 확인
        val pieceProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(result.pieceId)
        assertEquals(5, pieceProblems.size)
        
        // 문제 순서가 올바른지 확인 (PieceProblemFactory가 ID 순서대로 position을 부여하는지 확인)
        val savedProblemIds = pieceProblems.map { it.problemId }
        assertEquals(selectedProblemIds, savedProblemIds)
        
        // position 값이 순서대로 증가하는지 확인
        for (i in 0 until pieceProblems.size - 1) {
            assertTrue(pieceProblems[i].position < pieceProblems[i + 1].position)
        }
    }
    
    @Test
    fun `단일 문제로 학습지 생성 - 정상 처리`() {
        // Given
        val problems = problemJpaRepository.findAll()
        val selectedProblemId = listOf(problems.first().id)
        
        val request = PieceCreateRequest(
            teacherId = 2L,
            title = "단일 문제 학습지",
            problemIds = selectedProblemId
        )
        
        // When
        val result = pieceCreateUseCase.createPiece(request)
        
        // Then
        assertEquals("단일 문제 학습지", result.name)
        
        // PieceProblem 매핑 확인
        val pieceProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(result.pieceId)
        assertEquals(1, pieceProblems.size)
        assertEquals(selectedProblemId[0], pieceProblems[0].problemId)
    }

    @Test
    fun `존재하지 않는 문제 ID가 포함된 경우 - NotFoundException 발생`() {
        // Given
        val problems = problemJpaRepository.findAll()
        val existingProblemIds = problems.take(2).map { it.id }
        val nonExistentProblemIds = listOf(999L, 1000L)
        val allProblemIds = existingProblemIds + nonExistentProblemIds
        
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "문제없는 학습지",
            problemIds = allProblemIds
        )
        
        // When & Then
        val exception = assertThrows<NotFoundException> {
            pieceCreateUseCase.createPiece(request)
        }
        
        // 예외 메시지에 누락된 문제 ID들이 포함되어 있는지 확인
        assertTrue(exception.message!!.contains("999"))
        assertTrue(exception.message!!.contains("1000"))
    }
    
    @Test
    fun `createPiece - 존재하지 않는 문제 ID 포함시 NotFoundException 발생`() {
        // Given
        val problems = problemJpaRepository.findAll()
        val existingProblemIds = problems.take(3).map { it.id }
        val problemIdsWithMissing = existingProblemIds + listOf(999L) // 하나만 없음
        
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "부분적으로 누락된 학습지",
            problemIds = problemIdsWithMissing
        )
        
        // When & Then
        val exception = assertThrows<NotFoundException> {
            pieceCreateUseCase.createPiece(request)
        }

        assertTrue(exception.message!!.contains("999"))
    }
    
    @Test
    fun `모든 문제로 학습지 생성 - 정상 처리`() {
        // Given
        val problems = problemJpaRepository.findAll()
        val allProblemIds = problems.map { it.id }
        
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "모든 문제 학습지",
            problemIds = allProblemIds
        )
        
        // When
        val result = pieceCreateUseCase.createPiece(request)
        
        // Then
        assertEquals("모든 문제 학습지", result.name)
        
        // 모든 문제가 매핑되었는지 확인
        val pieceProblems = pieceProblemJpaRepository.findByPieceIdOrderByPosition(result.pieceId)
        assertEquals(problems.size, pieceProblems.size)
        assertEquals(allProblemIds.sorted(), pieceProblems.map { it.problemId }.sorted())
    }
} 