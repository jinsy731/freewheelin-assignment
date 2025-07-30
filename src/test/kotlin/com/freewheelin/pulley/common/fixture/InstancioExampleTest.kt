package com.freewheelin.pulley.common.fixture

import com.freewheelin.pulley.user.domain.model.UserRole
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime

/**
 * 도메인 엔티티 TestFixtures 사용 예시 테스트
 */
class InstancioExampleTest {

    @Test
    fun `User 도메인 픽스처 테스트`() {
        assertDoesNotThrow {
            // 기본 랜덤 사용자 생성
            val user = UserFixture.create()
            println("Generated user: $user")

            // 특정 필드만 오버라이드해서 생성
            val customUser = UserFixture.create(
                name = "김테스트",
                role = UserRole.TEACHER,
                isActive = false
            )
            println("Custom user: $customUser")

            // 학생 사용자 생성
            val student = UserFixture.create(role = UserRole.STUDENT)
            println("Generated student: $student")
        }
    }

    @Test
    fun `Problem 도메인 픽스처 테스트`() {
        assertDoesNotThrow {
            // 기본 랜덤 문제 생성
            val problem = ProblemFixture.create()
            println("Generated problem: $problem")

            // 특정 필드만 오버라이드해서 생성
            val customProblem = ProblemFixture.create(
                answer = "정답",
                level = 3,
                unitCode = "MATH01"
            )
            println("Custom problem: $customProblem")

            // 여러 문제 생성
            val problems = ProblemFixture.createList(3)
            println("Generated problems: $problems")
        }
    }

    @Test
    fun `Assignment 도메인 픽스처 테스트`() {
        assertDoesNotThrow {
            // 기본 랜덤 출제 생성
            val assignment = AssignmentFixture.create()
            println("Generated assignment: $assignment")

            // 특정 필드만 오버라이드해서 생성
            val specificAssignment = AssignmentFixture.create(
                pieceId = ValueObjectFixture.pieceId(100L),
                studentId = ValueObjectFixture.studentId(200L)
            )
            println("Specific assignment: $specificAssignment")

            // 제출 완료된 출제 생성
            val submittedAssignment = AssignmentFixture.create(
                submittedAt = LocalDateTime.now(),
                correctnessRate = ValueObjectFixture.correctnessRate(0.8)
            )
            println("Submitted assignment: $submittedAssignment")
        }
    }

    @Test
    fun `Submission 도메인 픽스처 테스트`() {
        assertDoesNotThrow {
            // 기본 랜덤 제출 답안 생성
            val submission = SubmissionFixture.create()
            println("Generated submission: $submission")

            // 특정 필드만 오버라이드해서 생성
            val specificSubmission = SubmissionFixture.create(
                assignmentId = 100L,
                problemId = 200L,
                answer = "42",
                isCorrect = true
            )
            println("Specific submission: $specificSubmission")

            // 정답 제출 생성
            val correctSubmission = SubmissionFixture.create(
                answer = "정답",
                isCorrect = true
            )
            println("Correct submission: $correctSubmission")

            // 오답 제출 생성
            val incorrectSubmission = SubmissionFixture.create(
                answer = "오답",
                isCorrect = false
            )
            println("Incorrect submission: $incorrectSubmission")
        }
    }

    @Test
    fun `Statistics 도메인 픽스처 테스트`() {
        assertDoesNotThrow {
            // 기본 랜덤 문제별 통계 생성
            val problemStat = PieceProblemStatFixture.create()
            println("Generated problem stat: $problemStat")

            // 특정 필드만 오버라이드해서 생성
            val specificProblemStat = PieceProblemStatFixture.create(
                totalCount = 100,
                correctCount = 80
            )
            println("Specific problem stat: $specificProblemStat")

            // 학생별 통계 생성
            val studentStat = PieceStudentStatFixture.create()
            println("Generated student stat: $studentStat")
        }
    }

    @Test
    fun `값 객체 픽스처 테스트`() {
        assertDoesNotThrow {
            // 다양한 값 객체들 생성
            val assignmentId = ValueObjectFixture.assignmentId()
            val pieceId = ValueObjectFixture.pieceId(100L)  // 특정 값으로 생성
            val studentId = ValueObjectFixture.studentId()
            val correctnessRate = ValueObjectFixture.correctnessRate(0.8)  // 80% 정답률
            
            println("Generated value objects:")
            println("  AssignmentId: $assignmentId")
            println("  PieceId: $pieceId")
            println("  StudentId: $studentId")
            println("  CorrectnessRate: $correctnessRate")
        }
    }

    @Test
    fun `복잡한 테스트 시나리오 구성`() {
        assertDoesNotThrow {
            // 시나리오: 특정 학생이 특정 학습지에서 80% 정답률로 제출한 상황
            val studentId = 12345L
            val pieceId = 67890L
            val assignmentId = 999L
            
            // 학생 생성
            val student = UserFixture.create(
                id = studentId,
                username = "test_student",
                name = "김테스트",
                role = UserRole.STUDENT
            )
            
            // 출제 생성 (80% 정답률로 제출 완료)
            val assignment = AssignmentFixture.create(
                id = ValueObjectFixture.assignmentId(assignmentId),
                pieceId = ValueObjectFixture.pieceId(pieceId),
                studentId = ValueObjectFixture.studentId(studentId),
                submittedAt = LocalDateTime.now(),
                correctnessRate = ValueObjectFixture.correctnessRate(0.8)
            )
            
            // 정답 제출 4개
            val correctSubmissions = (1..4).map { problemId ->
                SubmissionFixture.create(
                    assignmentId = assignmentId,
                    problemId = problemId.toLong(),
                    answer = "정답$problemId",
                    isCorrect = true
                )
            }
            
            // 오답 제출 1개
            val incorrectSubmission = SubmissionFixture.create(
                assignmentId = assignmentId,
                problemId = 5L,
                answer = "오답",
                isCorrect = false
            )
            
            println("Test scenario created:")
            println("  Student: $student")
            println("  Assignment: $assignment")
            println("  Correct submissions: ${correctSubmissions.size}")
            println("  Incorrect submissions: 1")
            println("  Expected correctness rate: ${correctSubmissions.size.toDouble() / (correctSubmissions.size + 1)}")
        }
    }
}