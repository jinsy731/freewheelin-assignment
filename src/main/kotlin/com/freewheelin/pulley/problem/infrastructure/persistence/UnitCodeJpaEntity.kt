package com.freewheelin.pulley.problem.infrastructure.persistence

import com.freewheelin.pulley.problem.domain.model.UnitCode
import jakarta.persistence.*

/**
 * 유닛 코드 JPA 엔티티
 */
@Entity
@Table(
    name = "unit_codes",
    indexes = [
        Index(name = "idx_unit_code_code", columnList = "unit_code", unique = true)
    ]
)
class UnitCodeJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "unit_code", nullable = false, unique = true, length = 10)
    val unitCode: String,
    
    @Column(name = "name", nullable = false)
    val name: String
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): UnitCode {
        return UnitCode(
            id = id,
            unitCode = unitCode,
            name = name
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(unitCode: UnitCode): UnitCodeJpaEntity {
            return UnitCodeJpaEntity(
                id = unitCode.id,
                unitCode = unitCode.unitCode,
                name = unitCode.name
            )
        }
    }
}