package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceProblemsQuery
import com.freewheelin.pulley.piece.application.port.PieceProblemsQueryUseCase
import com.freewheelin.pulley.piece.application.port.PieceProblemsResult
import com.freewheelin.pulley.piece.application.port.ProblemDetail
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.associateBy
import kotlin.collections.map

/**
 * 학습지 문제 조회 Application Service
 * 
 * 학생이 출제받은 학습지의 문제들을 조회하는 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional(readOnly = true)
class PieceProblemsQueryService(
    private val assignmentRepository: AssignmentRepository,
    private val pieceProblemRepository: PieceProblemRepository,
    private val problemRepository: ProblemRepository
) : PieceProblemsQueryUseCase {
    
    override fun getProblemsInPiece(query: PieceProblemsQuery): PieceProblemsResult {
        // 1. 권한 검증: 해당 학생에게 출제된 학습지인지 확인
        assignmentRepository.getByPieceIdAndStudentId(
            query.pieceId,
            query.studentId
        )
        
        // 2. 학습지의 문제 매핑 정보 조회 (position 순서대로)
        val pieceProblems = pieceProblemRepository.findByPieceIdOrderByPosition(query.pieceId)
        
        // 3. 실제 문제 정보 조회
        val problemIds = pieceProblems.map { it.problemId.value }
        val problems = problemRepository.findByIds(problemIds)
        val problemMap = problems.associateBy { it.id }
        
        // 4. 응답 데이터 구성 (순서 유지)
        val problemDetails = pieceProblems.map { pieceProblem ->
            val problem = problemMap[pieceProblem.problemId.value]
                ?: throw NotFoundException(
                    ErrorCode.PROBLEM_NOT_FOUND,
                    pieceProblem.problemId
                )

            ProblemDetail(
                id = problem.id,
                unitCode = problem.unitCode,
                level = problem.level,
                type = problem.problemType
            )
        }
        
        return PieceProblemsResult(
            problems = problemDetails
        )
    }
} 