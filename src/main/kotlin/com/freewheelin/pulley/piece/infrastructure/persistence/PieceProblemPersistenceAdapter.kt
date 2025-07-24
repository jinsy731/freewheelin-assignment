package com.freewheelin.pulley.piece.infrastructure.persistence

import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import org.springframework.stereotype.Repository

/**
 * PieceProblem Repository Port 구현체 (Adapter)
 */
@Repository
class PieceProblemPersistenceAdapter(
    private val pieceProblemJpaRepository: PieceProblemJpaRepository
) : PieceProblemRepository {
    
    override fun save(pieceProblem: PieceProblem): PieceProblem {
        val entity = PieceProblemJpaEntity.fromDomain(pieceProblem)
        val savedEntity = pieceProblemJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun saveAll(pieceProblems: List<PieceProblem>): List<PieceProblem> {
        val entities = pieceProblems.map { PieceProblemJpaEntity.fromDomain(it) }
        val savedEntities = pieceProblemJpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByPieceIdOrderByPosition(pieceId: Long): List<PieceProblem> {
        return pieceProblemJpaRepository.findByPieceIdOrderByPosition(pieceId)
            .map { it.toDomain() }
    }
    
    override fun findByPieceIdAndProblemIdIn(pieceId: Long, problemIds: List<Long>): List<PieceProblem> {
        return pieceProblemJpaRepository.findByPieceIdAndProblemIdIn(pieceId, problemIds)
            .map { it.toDomain() }
    }

    override fun findProblemsForOrderUpdate(
        pieceId: Long,
        problemId: Long,
        prevProblemId: Long?,
        nextProblemId: Long?
    ): List<PieceProblem> {
        // 1단계: 지정된 문제들(target, prev, next)을 먼저 조회하여 position 값을 얻음
        val targetProblemIds = listOfNotNull(problemId, prevProblemId, nextProblemId)
        val specifiedProblems = pieceProblemJpaRepository.findByPieceIdAndProblemIdIn(pieceId, targetProblemIds)

        // position 값 추출
        val prevPosition = prevProblemId?.let { id ->
            specifiedProblems.find { it.problemId == id }?.position
        }
        val nextPosition = nextProblemId?.let { id ->
            specifiedProblems.find { it.problemId == id }?.position
        }

        // 2단계: 최적화된 쿼리로 필요한 모든 문제들 조회
        return pieceProblemJpaRepository.findProblemsForOrderUpdate(
            pieceId = pieceId,
            targetProblemId = problemId,
            prevProblemId = prevProblemId,
            nextProblemId = nextProblemId,
            prevPosition = prevPosition,
            nextPosition = nextPosition
        ).map { it.toDomain() }
    }

    override fun findPieceProblemsForOrderUpdate(
        pieceId: Long,
        pieceProblemId: Long,
        prevPieceProblemId: Long?,
        nextPieceProblemId: Long?
    ): List<PieceProblem> {
        // 1단계: 지정된 PieceProblem들(target, prev, next)을 먼저 조회하여 position 값을 얻음
        val targetPieceProblemIds = listOfNotNull(pieceProblemId, prevPieceProblemId, nextPieceProblemId)
        val specifiedProblems = if (targetPieceProblemIds.isNotEmpty()) {
            pieceProblemJpaRepository.findAllById(targetPieceProblemIds)
        } else {
            emptyList()
        }

        // position 값 추출
        val prevPosition = prevPieceProblemId?.let { id ->
            specifiedProblems.find { it.id == id }?.position
        }
        val nextPosition = nextPieceProblemId?.let { id ->
            specifiedProblems.find { it.id == id }?.position
        }

        // 2단계: 최적화된 쿼리로 필요한 모든 PieceProblem들 조회
        return pieceProblemJpaRepository.findPieceProblemsForOrderUpdate(
            pieceId = pieceId,
            targetPieceProblemId = pieceProblemId,
            prevPieceProblemId = prevPieceProblemId,
            nextPieceProblemId = nextPieceProblemId,
            prevPosition = prevPosition,
            nextPosition = nextPosition
        ).map { it.toDomain() }
    }
} 