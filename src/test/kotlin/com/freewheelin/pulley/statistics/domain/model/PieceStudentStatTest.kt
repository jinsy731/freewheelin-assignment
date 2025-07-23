package com.freewheelin.pulley.statistics.domain.model

import com.freewheelin.pulley.common.domain.AssignmentId
import com.freewheelin.pulley.common.domain.CorrectnessRate
import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.StudentId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.assertj.core.api.Assertions.assertThat

@DisplayName("PieceStudentStat 도메인 모델 테스트")
class PieceStudentStatTest {

    @Nested
    @DisplayName("생성자 검증")
    inner class ConstructorValidation {

        @Test
        @DisplayName("정상적인 값으로 생성 시 성공한다")
        fun `should create successfully with valid values`() {
            // Given & When
            val stat = PieceStudentStat(
                id = 1L,
                assignmentId = AssignmentId(1L),
                pieceId = PieceId(2L),
                studentId = StudentId(3L),
                totalCount = 10,
                correctCount = 8,
                correctnessRate = CorrectnessRate(0.8)
            )

            // Then
            assertThat(stat.id).isEqualTo(1L)
            assertThat(stat.assignmentId.value).isEqualTo(1L)
            assertThat(stat.pieceId.value).isEqualTo(2L)
            assertThat(stat.studentId.value).isEqualTo(3L)
            assertThat(stat.totalCount).isEqualTo(10)
            assertThat(stat.correctCount).isEqualTo(8)
            assertThat(stat.correctnessRate.value).isEqualTo(0.8)
        }

        @Test
        @DisplayName("전체 문제 수가 음수일 때 예외가 발생한다")
        fun `should throw exception when totalCount is negative`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> {
                PieceStudentStat(
                    assignmentId = AssignmentId(1L),
                    pieceId = PieceId(2L),
                    studentId = StudentId(3L),
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
                PieceStudentStat(
                    assignmentId = AssignmentId(1L),
                    pieceId = PieceId(2L),
                    studentId = StudentId(3L),
                    totalCount = 5,
                    correctCount = -1,
                    correctnessRate = CorrectnessRate(0.0)
                )
            }
        }

        @Test
        @DisplayName("정답 수가 전체 문제 수를 초과할 때 예외가 발생한다")
        fun `should throw exception when correctCount exceeds totalCount`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> {
                PieceStudentStat(
                    assignmentId = AssignmentId(1L),
                    pieceId = PieceId(2L),
                    studentId = StudentId(3L),
                    totalCount = 5,
                    correctCount = 6,
                    correctnessRate = CorrectnessRate(1.0)
                )
            }
        }

        @Test
        @DisplayName("전체 문제 수와 정답 수가 0일 때 정상 생성된다")
        fun `should create successfully when totalCount and correctCount are zero`() {
            // Given & When
            val stat = PieceStudentStat(
                assignmentId = AssignmentId(1L),
                pieceId = PieceId(2L),
                studentId = StudentId(3L),
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
        @DisplayName("초기 통계를 올바르게 생성한다")
        fun `should create initial stat correctly`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 10,
                correctCount = 7
            )

            // Then
            assertThat(stat.assignmentId.value).isEqualTo(1L)
            assertThat(stat.pieceId.value).isEqualTo(2L)
            assertThat(stat.studentId.value).isEqualTo(3L)
            assertThat(stat.totalCount).isEqualTo(10)
            assertThat(stat.correctCount).isEqualTo(7)
            assertThat(stat.correctnessRate.value).isEqualTo(0.7)
        }

        @Test
        @DisplayName("전체 문제 수가 0일 때 정답률을 0으로 설정한다")
        fun `should set correctness rate to zero when totalCount is zero`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 0,
                correctCount = 0
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(0.0)
        }

        @Test
        @DisplayName("전체 문제 수가 양수일 때 정답률을 올바르게 계산한다")
        fun `should calculate correctness rate correctly when totalCount is positive`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 8,
                correctCount = 6
            )

            // Then
            assertThat(stat.totalCount).isEqualTo(8)
            assertThat(stat.correctCount).isEqualTo(6)
            assertThat(stat.correctnessRate.value).isEqualTo(0.75)
        }

        @Test
        @DisplayName("모든 문제를 맞혔을 때 정답률이 1.0이 된다")
        fun `should set correctness rate to one when all problems are correct`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 5,
                correctCount = 5
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(1.0)
        }

        @Test
        @DisplayName("모든 문제를 틀렸을 때 정답률이 0.0이 된다")
        fun `should set correctness rate to zero when all problems are incorrect`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 5,
                correctCount = 0
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(0.0)
        }
    }

    @Nested
    @DisplayName("update 메소드")
    inner class UpdateMethod {

        @Test
        @DisplayName("새로운 통계로 업데이트 시 정답률이 올바르게 계산된다")
        fun `should update with new statistics and calculate correctness rate correctly`() {
            // Given
            val originalStat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 10,
                correctCount = 5
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 20,
                newCorrectCount = 16
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(20)
            assertThat(updatedStat.correctCount).isEqualTo(16)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.8)
        }

        @Test
        @DisplayName("전체 문제 수가 0일 때 정답률을 0으로 설정한다")
        fun `should set correctness rate to zero when total count is zero`() {
            // Given
            val originalStat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
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
            val originalStat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 5,
                correctCount = 3
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

        @Test
        @DisplayName("정답률이 향상되는 경우를 올바르게 처리한다")
        fun `should handle correctness rate improvement correctly`() {
            // Given
            val originalStat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 10,
                correctCount = 4
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 10,
                newCorrectCount = 9
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(10)
            assertThat(updatedStat.correctCount).isEqualTo(9)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.9)
        }

        @Test
        @DisplayName("정답률이 하락하는 경우를 올바르게 처리한다")
        fun `should handle correctness rate decline correctly`() {
            // Given
            val originalStat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 10,
                correctCount = 8
            )

            // When
            val updatedStat = originalStat.update(
                newTotalCount = 15,
                newCorrectCount = 9
            )

            // Then
            assertThat(updatedStat.totalCount).isEqualTo(15)
            assertThat(updatedStat.correctCount).isEqualTo(9)
            assertThat(updatedStat.correctnessRate.value).isEqualTo(0.6)
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    inner class EdgeCases {

        @Test
        @DisplayName("대용량 데이터에서도 정확한 정답률을 계산한다")
        fun `should calculate accurate correctness rate with large numbers`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 1000000,
                correctCount = 750000
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(0.75)
        }

        @Test
        @DisplayName("소수점 정답률을 정확하게 계산한다")
        fun `should calculate decimal correctness rate accurately`() {
            // Given & When
            val stat = PieceStudentStat.create(
                assignmentId = 1L,
                pieceId = 2L,
                studentId = 3L,
                totalCount = 3,
                correctCount = 1
            )

            // Then
            assertThat(stat.correctnessRate.value).isEqualTo(1.0 / 3.0)
        }
    }
} 