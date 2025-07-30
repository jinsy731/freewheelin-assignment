package com.freewheelin.pulley.piece.domain.model

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.domain.validateOwnership
import com.freewheelin.pulley.common.exception.AuthorizationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

class PieceTest {

    @Test
    fun `유효한 정보로 새로운 학습지를 생성할 수 있다`() {
        // Given
        val teacherId = TeacherId(1L)
        val pieceName = PieceName("수학 기초 학습지")

        // When
        val piece = Piece.create(teacherId, pieceName)

        // Then
        assertThat(piece.teacherId).isEqualTo(teacherId)
        assertThat(piece.name).isEqualTo(pieceName)
        assertThat(piece.id.value).isEqualTo(0L) // JPA가 자동 생성할 임시 ID
    }

    @Test
    fun `TeacherId로 소유자 확인이 정확하게 동작한다`() {
        // Given
        val ownerId = TeacherId(1L)
        val otherId = TeacherId(2L)
        val piece = createTestPiece(teacherId = ownerId)

        // When & Then
        assertThat(piece.isOwnedBy(ownerId)).isTrue()
        assertThat(piece.isOwnedBy(otherId)).isFalse()
    }

    @Test
    fun `Long 타입으로 소유자 확인이 정확하게 동작한다`() {
        // Given
        val ownerId = 1L
        val otherId = 2L
        val piece = createTestPiece(teacherId = TeacherId(ownerId))

        // When & Then
        assertThat(piece.isOwnedBy(ownerId)).isTrue()
        assertThat(piece.isOwnedBy(otherId)).isFalse()
    }

    @Test
    fun `소유자일 때 소유권 검증이 성공한다`() {
        // Given
        val teacherId = 1L
        val piece = createTestPiece(teacherId = TeacherId(teacherId))

        // When & Then (예외가 발생하지 않아야 함)
        piece.validateOwnership(teacherId)
    }

    @Test
    fun `소유자가 아닐 때 소유권 검증이 실패한다`() {
        // Given
        val ownerId = 1L
        val requesterId = 2L
        val piece = createTestPiece(teacherId = TeacherId(ownerId))

        // When & Then
        val exception = assertThrows<AuthorizationException> {
            piece.validateOwnership(requesterId)
        }
        
        assertThat(exception.message).contains("Piece")
        assertThat(exception.message).contains(requesterId.toString())
    }

    @Test
    fun `같은 선생님 ID로 여러 학습지를 생성할 수 있다`() {
        // Given
        val teacherId = TeacherId(1L)
        val pieceName1 = PieceName("수학 학습지")
        val pieceName2 = PieceName("영어 학습지")

        // When
        val piece1 = Piece.create(teacherId, pieceName1)
        val piece2 = Piece.create(teacherId, pieceName2)

        // Then
        assertThat(piece1.teacherId).isEqualTo(teacherId)
        assertThat(piece2.teacherId).isEqualTo(teacherId)
        assertThat(piece1.name).isEqualTo(pieceName1)
        assertThat(piece2.name).isEqualTo(pieceName2)
        assertThat(piece1.name).isNotEqualTo(piece2.name)
    }

    @Test
    fun `최대 길이의 학습지 이름으로 생성할 수 있다`() {
        // Given
        val teacherId = TeacherId(1L)
        val maxLengthName = PieceName("a".repeat(100)) // MAX_LENGTH = 100

        // When
        val piece = Piece.create(teacherId, maxLengthName)

        // Then
        assertThat(piece.name).isEqualTo(maxLengthName)
    }

    @Test
    fun `기본 학습지 이름으로 생성할 수 있다`() {
        // Given
        val teacherId = TeacherId(1L)
        val defaultName = PieceName.default()

        // When
        val piece = Piece.create(teacherId, defaultName)

        // Then
        assertThat(piece.name).isEqualTo(defaultName)
        assertThat(piece.name.value).isEqualTo("새 학습지")
    }

    @Test
    fun `경계값 선생님 ID로 학습지를 생성할 수 있다`() {
        // Given
        val boundaryTeacherId = TeacherId(0L) // 최소값
        val pieceName = PieceName("경계값 테스트 학습지")

        // When
        val piece = Piece.create(boundaryTeacherId, pieceName)

        // Then
        assertThat(piece.teacherId).isEqualTo(boundaryTeacherId)
        assertThat(piece.isOwnedBy(0L)).isTrue()
    }

    @Test
    fun `큰 선생님 ID 값으로도 정상 동작한다`() {
        // Given
        val largeTeacherId = TeacherId(Long.MAX_VALUE)
        val pieceName = PieceName("대용량 ID 테스트")

        // When
        val piece = Piece.create(largeTeacherId, pieceName)

        // Then
        assertThat(piece.teacherId).isEqualTo(largeTeacherId)
        assertThat(piece.isOwnedBy(Long.MAX_VALUE)).isTrue()
    }

    private fun createTestPiece(
        pieceId: PieceId = PieceId(1L),
        teacherId: TeacherId = TeacherId(1L),
        name: PieceName = PieceName("테스트 학습지")
    ): Piece {
        return Piece(
            id = pieceId,
            teacherId = teacherId,
            name = name
        )
    }
} 