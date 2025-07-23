package com.freewheelin.pulley.statistics.domain.model

import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.ProblemId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.assertj.core.api.Assertions.assertThat

@DisplayName("PieceProblemStat 도메인 모델 테스트")
class PieceProblemStatTest {

    @Nested
    @DisplayName("생성자 검증")
    inner class ConstructorValidation {

        @Test
        @DisplayName("정상적인 값으로 생성 시 성공한다")
        fun `should create successfully with valid values`() {
            // Given & When
            val stat = PieceProblemStat(
                id = 1L,
                pieceId = PieceId(1L),
                problemId = ProblemId(1L),
                totalCount = 10,
                correctCount = 8,
                correctnessRate = CorrectnessRate(0.8)
            )

            // Then
            assertThat(stat.id).isEqualTo(1L)
            assertThat(stat.pieceId.value).isEqualTo(1L)
            assertThat(stat.problemId.value).isEqualTo(1L)
            assertThat(stat.totalCount).isEqualTo(10)
            assertThat(stat.correctCount).isEqualTo(8)
            assertThat(stat.correctnessRate.value).isEqualTo(0.8)
        }

        @Test
        @DisplayName("전체 제출 수가 음수일 때 예외가 발생한다")
        fun `should throw exception when totalCount is negative`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> {
                PieceProblemStat(
                    pieceId = PieceId(1L),
                    problemId = ProblemId(1L),
                    totalCount = -1,
                    correctCount = 0,
                    correctnessRate = CorrectnessRate(0.0)
                )
            }
        }

        @Test
        @DisplayName("정답 수가 음수일 때 예외가 발생한다")
        fun `should throw exception when correctCount is negative`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> {
                PieceProblemStat(
                    pieceId = PieceId(1L),
                    problemId = ProblemId(1L),
                    totalCount = 5,
                    correctCount = -1,
                    correctnessRate = CorrectnessRate(0.0)
                )
            }
        }

        @Test
        @DisplayName("정답 수가 전체 제출 수를 초과할 때 예외가 발생한다")
        fun `should throw exception when correctCount exceeds totalCount`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> {
                PieceProblemStat(
                    pieceId = PieceId(1L),
                    problemId = ProblemId(1L),
                    totalCount = 5,
                    correctCount = 6,
                    correctnessRate = CorrectnessRate(1.0)
                )
            }
        }

        @Test
        @DisplayName("전체 제출 수와 정답 수가 0일 때 정상 생성된다")
        fun `should create successfully when totalCount and correctCount are zero`() {
            // Given & When
            val stat = PieceProblemStat(
                pieceId = PieceId(1L),
                problemId = ProblemId(1L),
                totalCount = 0,
                correctCount = 0,
                correctnessRate = CorrectnessRate(0.0)
            )

            // Then
            assertThat(stat.totalCount).isEqualTo(0)
            assertThat(stat.correctCount).isEqualTo(0)
            assertThat(stat.correctnessRate.value).isEqualTo(0.0)
        }
    }

    @Nested
    @DisplayName("create 팩토리 메소드")
    inner class CreateFactoryMethod {

        @Test
        @DisplayName("초기 통계를 기본값(0)으로 생성한다")
        fun `should create initial stat with default values`() {
            // Given & When
            val stat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L
            )

            // Then
            assertThat(stat.pieceId.value).isEqualTo(1L)
            assertThat(stat.problemId.value).isEqualTo(2L)
            assertThat(stat.totalCount).isEqualTo(0)
            assertThat(stat.correctCount).isEqualTo(0)
            assertThat(stat.correctnessRate.value).isEqualTo(0.0)
        }

        @Test
        @DisplayName("전체 제출 수가 0일 때 정답률을 0으로 설정한다")
        fun `should set correctness rate to zero when totalCount is zero`() {
            // Given & When
            val stat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 0,
                correctCount = 0
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(0.0)
        }

        @Test
        @DisplayName("전체 제출 수가 양수일 때 정답률을 올바르게 계산한다")
        fun `should calculate correctness rate correctly when totalCount is positive`() {
            // Given & When
            val stat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 10,
                correctCount = 7
            )

            // Then
            assertThat(stat.totalCount).isEqualTo(10)
            assertThat(stat.correctCount).isEqualTo(7)
            assertThat(stat.correctnessRate.value).isEqualTo(0.7)
        }

        @Test
        @DisplayName("모든 문제를 맞혔을 때 정답률이 1.0이 된다")
        fun `should set correctness rate to one when all answers are correct`() {
            // Given & When
            val stat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 5,
                correctCount = 5
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(1.0)
        }
    }

    @Nested
    @DisplayName("updateIncrement 메소드")
    inner class UpdateIncrementMethod {

        @Test
        @DisplayName("정답일 때 전체 수와 정답 수를 모두 증가시킨다")
        fun `should increment both totalCount and correctCount when answer is correct`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 10,
                correctCount = 7
            )

            // When
            val updatedStat = originalStat.updateIncrement(isCorrect = true)

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(11)
            assertThat(updatedStat.correctCount).isEqualTo(8)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(8.0 / 11.0)
        }

        @Test
        @DisplayName("오답일 때 전체 수만 증가시킨다")
        fun `should increment only totalCount when answer is incorrect`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 10,
                correctCount = 7
            )

            // When
            val updatedStat = originalStat.updateIncrement(isCorrect = false)

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(11)
            assertThat(updatedStat.correctCount).isEqualTo(7)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(7.0 / 11.0)
        }

        @Test
        @DisplayName("처음 제출일 때 정답이면 정답률이 1.0이 된다")
        fun `should set correctness rate to one when first submission is correct`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L
            )

            // When
            val updatedStat = originalStat.updateIncrement(isCorrect = true)

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(1)
            assertThat(updatedStat.correctCount).isEqualTo(1)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(1.0)
        }

        @Test
        @DisplayName("처음 제출일 때 오답이면 정답률이 0.0이 된다")
        fun `should set correctness rate to zero when first submission is incorrect`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L
            )

            // When
            val updatedStat = originalStat.updateIncrement(isCorrect = false)

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(1)
            assertThat(updatedStat.correctCount).isEqualTo(0)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.0)
        }
    }

    @Nested
    @DisplayName("update 메소드")
    inner class UpdateMethod {

        @Test
        @DisplayName("새로운 통계로 업데이트 시 정답률이 올바르게 계산된다")
        fun `should update with new statistics and calculate correctness rate correctly`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 5,
                correctCount = 3
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 20,
                newCorrectCount = 15
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(20)
            assertThat(updatedStat.correctCount).isEqualTo(15)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.75)
        }

        @Test
        @DisplayName("전체 수가 0일 때 정답률을 0으로 설정한다")
        fun `should set correctness rate to zero when total count is zero`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L,
                totalCount = 10,
                correctCount = 5
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 0,
                newCorrectCount = 0
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(0)
            assertThat(updatedStat.correctCount).isEqualTo(0)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.0)
        }

        @Test
        @DisplayName("모든 문제를 맞혔을 때 정답률이 1.0이 된다")
        fun `should set correctness rate to one when all problems are correct`() {
            // Given
            val originalStat = PieceProblemStat.create(
                pieceId = 1L,
                problemId = 2L
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 8,
                newCorrectCount = 8
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(8)
            assertThat(updatedStat.correctCount).isEqualTo(8)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(1.0)
        }
    }
} 