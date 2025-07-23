package com.freewheelin.pulley.problem.domain.model

/**
 * 유닛 코드 도메인 모델
 * 
 * 문제의 유형 코드와 이름을 나타냅니다.
 */
data class UnitCode(
    val id: Int,
    val unitCode: String,
    val name: String
) {
    init {
        require(unitCode.isNotBlank()) { "유닛 코드는 비어있을 수 없습니다." }
        require(name.isNotBlank()) { "유닛 이름은 비어있을 수 없습니다." }
    }
}