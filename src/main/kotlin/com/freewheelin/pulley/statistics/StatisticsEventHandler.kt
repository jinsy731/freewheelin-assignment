package com.freewheelin.pulley.statistics

import com.freewheelin.pulley.assignment.domain.model.SubmissionGradedEvent
import com.freewheelin.pulley.statistics.application.service.StatisticsUpdateService
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 통계 이벤트 핸들러
 * 
 * 도메인 이벤트를 받아서 적절한 Application Service로 위임하는 역할을 합니다.
 */
@Component
class StatisticsEventHandler(
    private val statisticsUpdateService: StatisticsUpdateService
) {
    
    /**
     * 채점 완료 이벤트 처리
     */
    @EventListener
    fun handleSubmissionGraded(event: SubmissionGradedEvent) {
        logger.info { 
            "채점 완료 이벤트 수신 - assignmentId: ${event.assignmentId}, " +
            "pieceId: ${event.pieceId}, studentId: ${event.studentId}" 
        }
        
        try {
            statisticsUpdateService.updateStatistics(event)
            
            logger.debug { 
                "이벤트 처리 완료 - assignmentId: ${event.assignmentId}, " +
                "pieceId: ${event.pieceId}, studentId: ${event.studentId}" 
            }
        } catch (e: Exception) {
            logger.error(e) { 
                "이벤트 처리 실패 - assignmentId: ${event.assignmentId}, " +
                "pieceId: ${event.pieceId}, studentId: ${event.studentId}, error: ${e.message}" 
            }
            throw e
        }
    }
} 