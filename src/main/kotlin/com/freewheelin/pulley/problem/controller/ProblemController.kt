package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemSearchUseCase
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 문제 조회 API Controller
 */
@RestController
@RequestMapping("/problems")
@Validated
class ProblemController(
    private val problemSearchUseCase: ProblemSearchUseCase,
    private val securityService: SecurityService
) : ProblemApiSpec {
    
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER')")
    override fun getProblems(
        @RequestParam(name = "totalCount", required = true) @NotNull @Min(1) totalCount: Int,
        @RequestParam(name = "unitCodeList", required = true) @NotBlank unitCodeList: String,
        @RequestParam(name = "level", required = true) @NotNull level: Level,
        @RequestParam(name = "problemType", required = true) @NotNull problemType: ProblemTypeFilter
    ): ResponseEntity<ProblemSearchResponseDto> {
        
        // 쉼표로 구분된 유형코드 리스트 파싱
        val unitCodes = unitCodeList.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        // 파싱된 unitCodes가 비어있으면 예외 발생
        if (unitCodes.isEmpty()) {
            throw IllegalArgumentException("유효한 유형코드가 하나 이상 필요합니다.")
        }
        
        val query = ProblemSearchQuery(
            totalCount = totalCount,
            unitCodeList = unitCodes,
            level = level,
            problemType = problemType
        )
        
        val result = problemSearchUseCase.searchProblems(query)
        val isTeacher = securityService.isCurrentUserTeacher()
        val response = ProblemSearchResponseDto.from(result, isTeacher)
        
        return ResponseEntity.ok(response)
    }
} 