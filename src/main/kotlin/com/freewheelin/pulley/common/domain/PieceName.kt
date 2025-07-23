package com.freewheelin.pulley.common.domain

/**
 * 학습지 이름 값객체
 */
data class PieceName(val value: String) {
    init {
        require(value.isNotBlank()) { "학습지 이름은 비어있을 수 없습니다." }
        require(value.length <= MAX_LENGTH) { "학습지 이름은 ${MAX_LENGTH}자를 초과할 수 없습니다." }
        require(value.trim() == value) { "학습지 이름의 앞뒤 공백은 허용되지 않습니다." }
    }
    
    /**
     * 유효한 이름인지 확인
     */
    fun isValid(): Boolean = value.isNotBlank() && value.length <= MAX_LENGTH
    
    /**
     * 특정 키워드 포함 여부
     */
    fun contains(keyword: String): Boolean = 
        value.contains(keyword, ignoreCase = true)
    
    override fun toString(): String = value
    
    companion object {
        const val MAX_LENGTH = 100
        
        /**
         * 기본 학습지 이름 생성
         */
        fun default(): PieceName = PieceName("새 학습지")
    }
} 