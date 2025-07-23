package com.freewheelin.pulley.assignment.infrastructure.persistence

import com.freewheelin.pulley.assignment.domain.model.Submission
import com.freewheelin.pulley.assignment.domain.port.SubmissionRepository
import org.springframework.stereotype.Repository
import kotlin.collections.map

/**
 * Submission Repository 구현체
 * 
 * JPA를 사용하여 Submission 도메인 객체를 영속화합니다.
 */
@Repository
class SubmissionPersistenceAdapter(
    private val submissionJpaRepository: SubmissionJpaRepository
) : SubmissionRepository {
    
    override fun save(submission: Submission): Submission {
        val entity = submissionJpaRepository.save(
            SubmissionJpaEntity.fromDomain(submission)
        )
        return entity.toDomain()
    }
    
    override fun saveAll(submissions: List<Submission>): List<Submission> {
        val entities = submissions.map {
            SubmissionJpaEntity.fromDomain(it)
        }
        val savedEntities = submissionJpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByAssignmentId(assignmentId: Long): List<Submission> {
        return submissionJpaRepository.findByAssignmentId(assignmentId).map { it.toDomain() }
    }
    
    override fun findByAssignmentIds(assignmentIds: List<Long>): List<Submission> {
        return submissionJpaRepository.findByAssignmentIdIn(assignmentIds).map { it.toDomain() }
    }
    
    override fun findByProblemId(problemId: Long): List<Submission> {
        return submissionJpaRepository.findByProblemId(problemId).map { it.toDomain() }
    }
    
    override fun findByAssignmentIdAndProblemId(assignmentId: Long, problemId: Long): Submission? {
        return submissionJpaRepository.findByAssignmentIdAndProblemId(assignmentId, problemId)?.toDomain()
    }
    
    override fun findByAssignmentIdAndProblemIdIn(assignmentId: Long, problemIds: List<Long>): List<Submission> {
        return submissionJpaRepository.findByAssignmentIdAndProblemIdIn(assignmentId, problemIds).map { it.toDomain() }
    }
    
    override fun findByPieceIdAndProblemId(pieceId: Long, problemId: Long): List<Submission> {
        return submissionJpaRepository.findByPieceIdAndProblemId(pieceId, problemId).map { it.toDomain() }
    }
    
    override fun countByAssignmentId(assignmentId: Long): Long {
        return submissionJpaRepository.countByAssignmentId(assignmentId)
    }
} 