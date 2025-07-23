package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaEntity
import com.freewheelin.pulley.piece.application.port.PieceProblemsQuery
import com.freewheelin.pulley.piece.application.port.PieceProblemsQueryUseCase
import com.freewheelin.pulley.assignment.infrastructure.persistence.AssignmentJpaRepository
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaEntity
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceJpaRepository
import com.freewheelin.pulley.piece.infrastructure.persistence.PieceProblemJpaEntity
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
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PieceProblemsQueryService 통합 테스트
 * 
 * 실제 DB와 연동하여 전체 플로우를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PieceProblemsQueryServiceTestIT {
    
    @Autowired
    private lateinit var pieceProblemsQueryUseCase: PieceProblemsQueryUseCase
    
    @Autowired
    private lateinit var assignmentJpaRepository: AssignmentJpaRepository
    
    @Autowired
    private lateinit var pieceJpaRepository: PieceJpaRepository
    
    @Autowired
    private lateinit var pieceProblemJpaRepository: PieceProblemJpaRepository
    
    @Autowired
    private lateinit var problemJpaRepository: ProblemJpaRepository
    
    private var testPieceId = 0L
    private val testProblemIds = mutableListOf<Long>()
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        assignmentJpaRepository.deleteAll()
        pieceProblemJpaRepository.deleteAll()
        pieceJpaRepository.deleteAll()
        problemJpaRepository.deleteAll()
        
        // 1. 테스트 문제들 생성
        val testProblems = listOf(
            ProblemJpaEntity(
                id = 0,
                answer = "답1",
                unitCode = "UC001",
                level = 1,
                problemType = ProblemType.SUBJECTIVE
            ),
            ProblemJpaEntity(id = 0, answer = "답2", unitCode = "UC001", level = 2, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(id = 0, answer = "답3", unitCode = "UC002", level = 3, problemType = ProblemType.SUBJECTIVE),
            ProblemJpaEntity(id = 0, answer = "답4", unitCode = "UC002", level = 4, problemType = ProblemType.SELECTION),
            ProblemJpaEntity(id = 0, answer = "답5", unitCode = "UC003", level = 5, problemType = ProblemType.SUBJECTIVE)
        )
        val savedProblems = problemJpaRepository.saveAll(testProblems)
        testProblemIds.addAll(savedProblems.map { it.id })
        
        // 2. 테스트 학습지 생성
        val testPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "문제 조회 테스트 학습지")
        val savedPiece = pieceJpaRepository.save(testPiece)
        testPieceId = savedPiece.id
        
        // 3. 학습지-문제 매핑 생성 (순서: problem1, problem3, problem2, problem5, problem4)
        val testPieceProblems = listOf(
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = testProblemIds[0], position = 1.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = testProblemIds[2], position = 2.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = testProblemIds[1], position = 3.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = testProblemIds[4], position = 4.0),
            PieceProblemJpaEntity(id = 0, pieceId = testPieceId, problemId = testProblemIds[3], position = 5.0)
        )
        pieceProblemJpaRepository.saveAll(testPieceProblems)
        
        // 4. 학생들에게 학습지 출제
        val assignments = listOf(
            AssignmentJpaEntity(
                id = 0,
                pieceId = testPieceId,
                studentId = 101L,
                assignedAt = LocalDateTime.now()
            ),
            AssignmentJpaEntity(
                id = 0,
                pieceId = testPieceId,
                studentId = 102L,
                assignedAt = LocalDateTime.now()
            )
        )
        assignmentJpaRepository.saveAll(assignments)
    }
    
    @Test
    fun `정상적인 경우 - 학습지 문제 조회 성공`() {
        // Given
        val query = PieceProblemsQuery(
            pieceId = testPieceId,
            studentId = 101L
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertEquals(5, result.problems.size)
        
        // 순서가 올바른지 확인 (position 순서대로: problem1, problem3, problem2, problem5, problem4)
        val expectedProblemIds = listOf(testProblemIds[0], testProblemIds[2], testProblemIds[1], testProblemIds[4], testProblemIds[3])
        val actualProblemIds = result.problems.map { it.id }
        assertEquals(expectedProblemIds, actualProblemIds)
        
        // 각 문제의 상세 정보가 올바른지 확인
        val problems = result.problems
        
        // 첫 번째 문제 (문제1)
        assertEquals("UC001", problems[0].unitCode)
        assertEquals(1, problems[0].level)
        assertEquals(ProblemType.SUBJECTIVE, problems[0].type)
        
        // 두 번째 문제 (문제3)
        assertEquals("UC002", problems[1].unitCode)
        assertEquals(3, problems[1].level)
        assertEquals(ProblemType.SUBJECTIVE, problems[1].type)
        
        // 세 번째 문제 (문제2)
        assertEquals("UC001", problems[2].unitCode)
        assertEquals(2, problems[2].level)
        assertEquals(ProblemType.SELECTION, problems[2].type)
        
        // 네 번째 문제 (문제5)
        assertEquals("UC003", problems[3].unitCode)
        assertEquals(5, problems[3].level)
        assertEquals(ProblemType.SUBJECTIVE, problems[3].type)
        
        // 다섯 번째 문제 (문제4)
        assertEquals("UC002", problems[4].unitCode)
        assertEquals(4, problems[4].level)
        assertEquals(ProblemType.SELECTION, problems[4].type)
    }
    
    @Test
    fun `다른 학생도 같은 학습지 조회 - 동일한 순서와 내용`() {
        // Given
        val query = PieceProblemsQuery(
            pieceId = testPieceId,
            studentId = 102L // 다른 학생 ID
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertEquals(5, result.problems.size)
        
        // 동일한 순서인지 확인
        val expectedProblemIds = listOf(testProblemIds[0], testProblemIds[2], testProblemIds[1], testProblemIds[4], testProblemIds[3])
        val actualProblemIds = result.problems.map { it.id }
        assertEquals(expectedProblemIds, actualProblemIds)
        
        // 동일한 내용인지 확인
        assertEquals("UC001", result.problems[0].unitCode)
        assertEquals(1, result.problems[0].level)
        assertEquals(ProblemType.SUBJECTIVE, result.problems[0].type)
    }
    
    @Test
    fun `출제받지 않은 학생이 학습지 조회 시도 - NotFoundException 발생`() {
        // Given
        val query = PieceProblemsQuery(
            pieceId = testPieceId,
            studentId = 999L // 출제받지 않은 학생 ID
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            pieceProblemsQueryUseCase.getProblemsInPiece(query)
        }
    }
    
    @Test
    fun `존재하지 않는 학습지 조회 - NotFoundException 발생`() {
        // Given
        val nonExistentPieceId = 999L
        val query = PieceProblemsQuery(
            pieceId = nonExistentPieceId,
            studentId = 101L
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            pieceProblemsQueryUseCase.getProblemsInPiece(query)
        }
    }
    
    @Test
    fun `문제가 없는 학습지 조회 - 빈 결과 반환`() {
        // Given: 문제가 없는 새로운 학습지 생성
        val emptyPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "빈 학습지")
        val savedEmptyPiece = pieceJpaRepository.save(emptyPiece)
        
        // 학생에게 빈 학습지 출제
        val emptyAssignment = AssignmentJpaEntity(
            id = 0,
            pieceId = savedEmptyPiece.id,
            studentId = 101L,
            assignedAt = LocalDateTime.now()
        )
        assignmentJpaRepository.save(emptyAssignment)
        
        val query = PieceProblemsQuery(
            pieceId = savedEmptyPiece.id,
            studentId = 101L
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertTrue(result.problems.isEmpty())
    }
    
    @Test
    fun `단일 문제만 있는 학습지 조회`() {
        // Given: 단일 문제만 있는 학습지 생성
        val singleProblemPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "단일 문제 학습지")
        val savedSinglePiece = pieceJpaRepository.save(singleProblemPiece)
        
        // 단일 문제 매핑
        val singlePieceProblem = PieceProblemJpaEntity(
            id = 0,
            pieceId = savedSinglePiece.id,
            problemId = testProblemIds[0],
            position = 1.0
        )
        pieceProblemJpaRepository.save(singlePieceProblem)
        
        // 학생에게 출제
        val singleAssignment = AssignmentJpaEntity(
            id = 0,
            pieceId = savedSinglePiece.id,
            studentId = 101L,
            assignedAt = LocalDateTime.now()
        )
        assignmentJpaRepository.save(singleAssignment)
        
        val query = PieceProblemsQuery(
            pieceId = savedSinglePiece.id,
            studentId = 101L
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertEquals(1, result.problems.size)
        assertEquals(testProblemIds[0], result.problems[0].id)
        assertEquals("UC001", result.problems[0].unitCode)
        assertEquals(1, result.problems[0].level)
        assertEquals(ProblemType.SUBJECTIVE, result.problems[0].type)
    }
    
    @Test
    fun `다양한 유형과 난이도의 문제들이 올바르게 조회되는지 확인`() {
        // Given
        val query = PieceProblemsQuery(
            pieceId = testPieceId,
            studentId = 101L
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertEquals(5, result.problems.size)
        
        // 유형별 개수 확인
        val subjectiveCount = result.problems.count { it.type == ProblemType.SUBJECTIVE }
        val selectionCount = result.problems.count { it.type == ProblemType.SELECTION }
        assertEquals(3, subjectiveCount) // 문제1, 문제3, 문제5
        assertEquals(2, selectionCount)  // 문제2, 문제4
        
        // 난이도별 개수 확인
        val levelCounts = result.problems.groupingBy { it.level }.eachCount()
        assertEquals(1, levelCounts[1]) // level 1
        assertEquals(1, levelCounts[2]) // level 2
        assertEquals(1, levelCounts[3]) // level 3
        assertEquals(1, levelCounts[4]) // level 4
        assertEquals(1, levelCounts[5]) // level 5
        
        // 유형코드별 개수 확인
        val unitCodeCounts = result.problems.groupingBy { it.unitCode }.eachCount()
        assertEquals(2, unitCodeCounts["UC001"]) // 문제1, 문제2
        assertEquals(2, unitCodeCounts["UC002"]) // 문제3, 문제4
        assertEquals(1, unitCodeCounts["UC003"]) // 문제5
    }
    
    @Test
    fun `순서가 복잡하게 섞여있어도 올바른 position 순서로 조회`() {
        // Given: 새로운 학습지에 복잡한 순서로 문제 추가
        val complexPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "복잡한 순서 학습지")
        val savedComplexPiece = pieceJpaRepository.save(complexPiece)
        
        // position 순서: 0.5, 1.2, 1.8, 2.5, 3.1
        val complexPieceProblems = listOf(
            PieceProblemJpaEntity(id = 0, pieceId = savedComplexPiece.id, problemId = testProblemIds[3], position = 0.5),
            PieceProblemJpaEntity(id = 0, pieceId = savedComplexPiece.id, problemId = testProblemIds[1], position = 1.2),
            PieceProblemJpaEntity(id = 0, pieceId = savedComplexPiece.id, problemId = testProblemIds[4], position = 1.8),
            PieceProblemJpaEntity(id = 0, pieceId = savedComplexPiece.id, problemId = testProblemIds[0], position = 2.5),
            PieceProblemJpaEntity(id = 0, pieceId = savedComplexPiece.id, problemId = testProblemIds[2], position = 3.1)
        )
        pieceProblemJpaRepository.saveAll(complexPieceProblems)
        
        // 학생에게 출제
        val complexAssignment = AssignmentJpaEntity(
            id = 0,
            pieceId = savedComplexPiece.id,
            studentId = 101L,
            assignedAt = LocalDateTime.now()
        )
        assignmentJpaRepository.save(complexAssignment)
        
        val query = PieceProblemsQuery(
            pieceId = savedComplexPiece.id,
            studentId = 101L
        )
        
        // When
        val result = pieceProblemsQueryUseCase.getProblemsInPiece(query)
        
        // Then
        assertEquals(5, result.problems.size)
        
        // position 순서에 따른 올바른 순서인지 확인
        val expectedOrder = listOf(testProblemIds[3], testProblemIds[1], testProblemIds[4], testProblemIds[0], testProblemIds[2])
        val actualOrder = result.problems.map { it.id }
        assertEquals(expectedOrder, actualOrder)
    }
    
    @Test
    fun `여러 학습지가 있을 때 올바른 학습지의 문제만 조회`() {
        // Given: 추가 학습지 생성
        val anotherPiece = PieceJpaEntity(id = 0, teacherId = 1L, name = "다른 학습지")
        val savedAnotherPiece = pieceJpaRepository.save(anotherPiece)
        
        // 다른 학습지에 다른 문제들 추가
        val anotherPieceProblem = PieceProblemJpaEntity(
            id = 0,
            pieceId = savedAnotherPiece.id,
            problemId = testProblemIds[0], // 같은 문제이지만 다른 학습지
            position = 1.0
        )
        pieceProblemJpaRepository.save(anotherPieceProblem)
        
        // 학생에게 두 번째 학습지도 출제
        val anotherAssignment = AssignmentJpaEntity(
            id = 0,
            pieceId = savedAnotherPiece.id,
            studentId = 101L,
            assignedAt = LocalDateTime.now()
        )
        assignmentJpaRepository.save(anotherAssignment)
        
        // 원래 학습지 조회
        val originalQuery = PieceProblemsQuery(
            pieceId = testPieceId,
            studentId = 101L
        )
        
        // 새로운 학습지 조회
        val anotherQuery = PieceProblemsQuery(
            pieceId = savedAnotherPiece.id,
            studentId = 101L
        )
        
        // When
        val originalResult = pieceProblemsQueryUseCase.getProblemsInPiece(originalQuery)
        val anotherResult = pieceProblemsQueryUseCase.getProblemsInPiece(anotherQuery)
        
        // Then
        // 원래 학습지는 5개 문제
        assertEquals(5, originalResult.problems.size)
        
        // 새로운 학습지는 1개 문제
        assertEquals(1, anotherResult.problems.size)
        assertEquals(testProblemIds[0], anotherResult.problems[0].id)
        
        // 서로 다른 결과인지 확인
        assertTrue(originalResult.problems.size != anotherResult.problems.size)
    }
} 