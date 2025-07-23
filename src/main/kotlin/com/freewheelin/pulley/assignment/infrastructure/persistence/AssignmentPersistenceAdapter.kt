package com.freewheelin.pulley.assignment.infrastructure.persistence

import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.port.AssignmentRepository
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import org.springframework.stereotype.Repository

/**
 * Assignment Repository 구현체
 * 
 * JPA를 사용하여 Assignment 도메인 객체를 영속화합니다.
 */
@Repository
class AssignmentPersistenceAdapter(
    private val assignmentJpaRepository: AssignmentJpaRepository
) : AssignmentRepository {
    
    override fun save(assignment: Assignment): Assignment {
        val entity = AssignmentJpaEntity.fromDomain(assignment)
        val savedEntity = assignmentJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun saveAll(assignments: List<Assignment>): List<Assignment> {
        val entities = assignments.map { AssignmentJpaEntity.fromDomain(it) }
        val savedEntities = assignmentJpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }
    
    override fun findByPieceIdAndStudentId(pieceId: Long, studentId: Long): Assignment? {
        return assignmentJpaRepository.findByPieceIdAndStudentId(pieceId, studentId)
            ?.toDomain()
    }
    
    override fun getByPieceIdAndStudentId(pieceId: Long, studentId: Long): Assignment {
        return findByPieceIdAndStudentId(pieceId, studentId) ?: throw NotFoundException(
            ErrorCode.ASSIGNMENT_NOT_ASSIGNED,
            "해당 학생에게 출제되지 않은 학습지입니다. (학습지: $pieceId, 학생: $studentId)"
        )
    }
    
    override fun findByPieceIdAndStudentIdIn(pieceId: Long, studentIds: List<Long>): List<Assignment> {
        return assignmentJpaRepository.findByPieceIdAndStudentIdIn(pieceId, studentIds)
            .map { it.toDomain() }
    }
} 