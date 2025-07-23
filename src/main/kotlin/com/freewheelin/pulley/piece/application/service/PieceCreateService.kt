package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceCreateRequest
import com.freewheelin.pulley.piece.application.port.PieceCreateResult
import com.freewheelin.pulley.piece.application.port.PieceCreateUseCase
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.piece.domain.service.PieceProblemFactory
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.map
import kotlin.collections.minus
import kotlin.collections.toSet

private val logger = KotlinLogging.logger {}

/**
 * 학습지 생성 Application Service
 * 
 * 새로운 학습지를 생성하고 도메인 Factory를 통해 문제를 추가하는 
 * 오케스트레이션 로직을 처리합니다.
 */
@Service
@Transactional
class PieceCreateService(
    private val pieceRepository: PieceRepository,
    private val pieceProblemRepository: PieceProblemRepository,
    private val problemRepository: ProblemRepository
) : PieceCreateUseCase {
    
    override fun createPiece(request: PieceCreateRequest): PieceCreateResult {
        logger.info { 
            "학습지 생성 시작 - teacherId: ${request.teacherId}, name: '${request.title}', " +
            "problemCount: ${request.problemIds.size}" 
        }
        
        try {
            // 1. 새로운 학습지 생성
            val piece = Piece.create(
                teacherId = TeacherId(request.teacherId),
                name = PieceName(request.title)
            )
            
            // 2. 학습지 저장
            val savedPiece = pieceRepository.save(piece)
            logger.debug { "학습지 저장 완료 - pieceId: ${savedPiece.id.value}" }
            
            // 3. 선택된 문제들 조회
            val problems = problemRepository.findByIds(request.problemIds)
            if (problems.size != request.problemIds.size) {
                val foundProblemIds = problems.map { it.id }.toSet()
                val missingProblemIds = request.problemIds.toSet() - foundProblemIds
                
                logger.warn { 
                    "존재하지 않는 문제 포함 - pieceId: ${savedPiece.id.value}, " +
                    "missing: $missingProblemIds, found: ${problems.size}/${request.problemIds.size}" 
                }
                
                throw NotFoundException(
                    ErrorCode.PROBLEM_NOT_FOUND,
                    "존재하지 않는 문제가 포함되어 있습니다. 누락된 문제 ID: $missingProblemIds"
                )
            }
            
            logger.debug { "문제 조회 완료 - problemCount: ${problems.size}" }
            
            // 4. Factory를 통해 정렬된 PieceProblem 생성
            val pieceProblems = PieceProblemFactory.createFromProblems(
                pieceId = PieceId(savedPiece.id.value),
                problems = problems
            )
            
            // 5. PieceProblem 저장
            pieceProblemRepository.saveAll(pieceProblems)
            logger.debug { "학습지 문제 매핑 저장 완료 - pieceProblemCount: ${pieceProblems.size}" }
            
            val result = PieceCreateResult(
                pieceId = savedPiece.id.value,
                name = savedPiece.name.value
            )
            
            logger.info { 
                "학습지 생성 완료 - pieceId: ${result.pieceId}, name: '${result.name}', " +
                "problemCount: ${pieceProblems.size}" 
            }
            
            return result
            
        } catch (e: Exception) {
            logger.error(e) { 
                "학습지 생성 실패 - teacherId: ${request.teacherId}, name: '${request.title}', " +
                "error: ${e.message}" 
            }
            throw e
        }
    }
} 