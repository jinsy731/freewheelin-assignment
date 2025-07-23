package com.freewheelin.pulley.statistics.application.service

import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import com.freewheelin.pulley.statistics.domain.port.PieceProblemStatRepository
import com.freewheelin.pulley.statistics.domain.port.PieceStudentStatRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.forEach

private val logger = KotlinLogging.logger {}

/**
 * 통계 업데이트 Application Service
 * 
 * 채점 완료 이벤트의 데이터를 활용하여 효율적으로 통계 테이블을 업데이트합니다.
 */
@Service
@Transactional
class StatisticsUpdateService(
    private val pieceStudentStatRepository: PieceStudentStatRepository,
    private val pieceProblemStatRepository: PieceProblemStatRepository
) {
    
    /**
     * 채점 완료 이벤트 처리
     * 
     * 이벤트에 포함된 채점 결과를 활용하여 학생별/문제별 통계를 업데이트합니다.
     */
    fun updateStatistics(event: SubmissionGradedEvent) {
        logger.info { 
            "통계 업데이트 시작 - assignmentId: ${event.assignmentId}, " +
            "pieceId: ${event.pieceId}, studentId: ${event.studentId}, " +
            "submissionCount: ${event.submissionResults.size}" 
        }
        
        try {
            updateStudentStatistics(event)
            updateProblemStatistics(event)
            
            logger.info { 
                "통계 업데이트 완료 - assignmentId: ${event.assignmentId}, " +
                "pieceId: ${event.pieceId}, studentId: ${event.studentId}" 
            }
        } catch (e: Exception) {
            logger.error(e) { 
                "통계 업데이트 실패 - assignmentId: ${event.assignmentId}, " +
                "pieceId: ${event.pieceId}, studentId: ${event.studentId}, error: ${e.message}" 
            }
            throw e
        }
    }
    
    /**
     * 학생별 통계 업데이트
     */
    private fun updateStudentStatistics(event: SubmissionGradedEvent) {
        logger.debug { "학생별 통계 업데이트 시작 - assignmentId: ${event.assignmentId}" }
        
        val studentStats = event.calculateStudentStats()
        
        val existingStat = pieceStudentStatRepository.findByAssignmentId(event.assignmentId)
        
        val updatedStat = if (existingStat != null) {
            logger.debug { 
                "기존 학생 통계 업데이트 - assignmentId: ${event.assignmentId}, " +
                "기존: ${existingStat.totalCount}/${existingStat.correctCount}, " +
                "신규: ${studentStats.totalCount}/${studentStats.correctCount}" 
            }
            // 기존 통계 업데이트
            existingStat.update(studentStats.totalCount, studentStats.correctCount)
        } else {
            logger.debug { 
                "신규 학생 통계 생성 - assignmentId: ${event.assignmentId}, " +
                "통계: ${studentStats.totalCount}/${studentStats.correctCount}" 
            }
            // 새로운 통계 생성
            PieceStudentStat.create(
                assignmentId = event.assignmentId,
                pieceId = event.pieceId,
                studentId = event.studentId,
                totalCount = studentStats.totalCount,
                correctCount = studentStats.correctCount
            )
        }
        
        pieceStudentStatRepository.save(updatedStat)
        logger.debug { "학생별 통계 저장 완료 - assignmentId: ${event.assignmentId}" }
    }
    
    /**
     * 문제별 통계 업데이트
     */
    private fun updateProblemStatistics(event: SubmissionGradedEvent) {
        logger.debug { 
            "문제별 통계 업데이트 시작 - pieceId: ${event.pieceId}, " +
            "problemCount: ${event.calculateProblemStatsMap().size}" 
        }
        
        val problemStatsMap = event.calculateProblemStatsMap()
        
        problemStatsMap.forEach { (problemId, problemStats) ->
            val existingStat = pieceProblemStatRepository.findByPieceIdAndProblemId(
                event.pieceId, 
                problemId
            )
            
            val updatedStat = if (existingStat != null) {
                logger.debug { 
                    "기존 문제 통계 업데이트 - problemId: $problemId, " +
                    "기존: ${existingStat.totalCount}/${existingStat.correctCount}, " +
                    "증분: ${problemStats.totalCount}/${problemStats.correctCount}" 
                }
                // 기존 통계에 증분 추가
                existingStat.update(
                    existingStat.totalCount + problemStats.totalCount,
                    existingStat.correctCount + problemStats.correctCount
                )
            } else {
                logger.debug { 
                    "신규 문제 통계 생성 - problemId: $problemId, " +
                    "통계: ${problemStats.totalCount}/${problemStats.correctCount}" 
                }
                // 새로운 통계 생성
                PieceProblemStat.create(
                    pieceId = event.pieceId,
                    problemId = problemId,
                    totalCount = problemStats.totalCount,
                    correctCount = problemStats.correctCount
                )
            }
            
            pieceProblemStatRepository.save(updatedStat)
        }
        
        logger.debug { "문제별 통계 저장 완료 - pieceId: ${event.pieceId}" }
    }
} 