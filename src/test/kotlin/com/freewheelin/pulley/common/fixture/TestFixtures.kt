package com.freewheelin.pulley.common.fixture

import com.freewheelin.pulley.assignment.domain.model.Assignment
import com.freewheelin.pulley.assignment.domain.model.Submission
import com.freewheelin.pulley.common.domain.*
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.model.UnitCode
import com.freewheelin.pulley.statistics.domain.model.PieceProblemStat
import com.freewheelin.pulley.statistics.domain.model.PieceStudentStat
import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.model.UserRole
import org.instancio.Instancio
import org.instancio.Select.field
import java.time.LocalDateTime

/**
 * 도메인 엔티티를 위한 테스트 픽스처 유틸리티
 * 
 * 기본적으로 Instancio가 랜덤한 값으로 객체를 생성하고,
 * 필요한 필드만 오버라이드할 수 있습니다.
 */

/**
 * User 도메인 픽스처
 */
object UserFixture {
    /**
     * 기본 User 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     * 
     * 사용 예시:
     * val user = UserFixture.create()
     * val customUser = UserFixture.create(name = "김테스트", role = UserRole.TEACHER)
     */
    fun create(
        id: Long? = null,
        username: String? = null,
        password: String? = null,
        name: String? = null,
        email: String? = null,
        role: UserRole? = null,
        isActive: Boolean? = null,
        createdAt: LocalDateTime? = null,
        lastLoginAt: LocalDateTime? = null
    ): User {
        return Instancio.of(User::class.java)
            .apply {
                if (id != null) set(field("id"), id)
                if (username != null) set(field("username"), username)
                if (password != null) set(field("password"), password)
                if (name != null) set(field("name"), name)
                if (email != null) set(field("email"), email)
                if (role != null) set(field("role"), role)
                if (isActive != null) set(field("isActive"), isActive)
                if (createdAt != null) set(field("createdAt"), createdAt)
                if (lastLoginAt != null) set(field("lastLoginAt"), lastLoginAt)
            }
            .create()
    }
}

/**
 * Problem 도메인 픽스처
 */
object ProblemFixture {
    /**
     * 기본 Problem 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     * 
     * 사용 예시:
     * val problem = ProblemFixture.create()
     * val customProblem = ProblemFixture.create(answer = "정답", level = 3)
     */
    fun create(
        id: Long? = null,
        answer: String? = null,
        unitCode: String? = null,
        level: Int? = null,
        problemType: ProblemType? = null
    ): Problem {
        return Instancio.of(Problem::class.java)
            .apply {
                if (id != null) set(field("id"), id)
                if (answer != null) set(field("answer"), answer)
                if (unitCode != null) set(field("unitCode"), unitCode)
                if (level != null) set(field("level"), level)
                if (problemType != null) set(field("problemType"), problemType)
            }
            .create()
    }

    fun createList(count: Int): List<Problem> {
        return (1..count).map { create() }
    }
}

/**
 * UnitCode 도메인 픽스처
 */
object UnitCodeFixture {
    /**
     * 기본 UnitCode 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     */
    fun create(
        id: Int? = null,
        unitCode: String? = null,
        name: String? = null
    ): UnitCode {
        return Instancio.of(UnitCode::class.java)
            .apply {
                if (id != null) set(field("id"), id)
                if (unitCode != null) set(field("unitCode"), unitCode)
                if (name != null) set(field("name"), name)
            }
            .create()
    }
}

/**
 * Piece 도메인 픽스처
 */
object PieceFixture {
    /**
     * 기본 Piece 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     */
    fun create(
        id: PieceId? = null,
        teacherId: TeacherId? = null,
        name: PieceName? = null
    ): Piece {
        return Piece(
            id = id ?: ValueObjectFixture.pieceId(),
            teacherId = teacherId ?: ValueObjectFixture.teacherId(),
            name = name ?: ValueObjectFixture.pieceName()
        )
    }
}

/**
 * PieceProblem 도메인 픽스처
 */
object PieceProblemFixture {
    /**
     * 기본 PieceProblem 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     */
    fun create(
        id: Long? = null,
        pieceId: PieceId? = null,
        problemId: ProblemId? = null,
        position: Position? = null
    ): PieceProblem {
        return PieceProblem(
            id = id ?: (1L..999999L).random(),
            pieceId = pieceId ?: ValueObjectFixture.pieceId(),
            problemId = problemId ?: ValueObjectFixture.problemId(),
            position = position ?: ValueObjectFixture.position()
        )
    }
}

/**
 * Assignment 도메인 픽스처
 */
object AssignmentFixture {
    /**
     * 기본 Assignment 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     * 
     * 사용 예시:
     * val assignment = AssignmentFixture.create()
     * val submittedAssignment = AssignmentFixture.create(
     *     submittedAt = LocalDateTime.now(),
     *     correctnessRate = CorrectnessRate(0.8)
     * )
     */
    fun create(
        id: AssignmentId? = null,
        pieceId: PieceId? = null,
        studentId: StudentId? = null,
        assignedAt: LocalDateTime? = null,
        submittedAt: LocalDateTime? = null,
        correctnessRate: CorrectnessRate? = null
    ): Assignment {
        return Assignment(
            id = id ?: ValueObjectFixture.assignmentId(),
            pieceId = pieceId ?: ValueObjectFixture.pieceId(),
            studentId = studentId ?: ValueObjectFixture.studentId(),
            assignedAt = assignedAt ?: LocalDateTime.now().minusDays((1..30).random().toLong()),
            submittedAt = submittedAt,
            correctnessRate = correctnessRate
        )
    }
}

/**
 * Submission 도메인 픽스처
 */
object SubmissionFixture {
    /**
     * 기본 Submission 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     * 
     * 사용 예시:
     * val submission = SubmissionFixture.create()
     * val correctSubmission = SubmissionFixture.create(answer = "정답", isCorrect = true)
     */
    fun create(
        id: Long? = null,
        assignmentId: Long? = null,
        problemId: Long? = null,
        answer: String? = null,
        isCorrect: Boolean? = null
    ): Submission {
        return Instancio.of(Submission::class.java)
            .apply {
                if (id != null) set(field("id"), id)
                if (assignmentId != null) set(field("assignmentId"), assignmentId)
                if (problemId != null) set(field("problemId"), problemId)
                if (answer != null) set(field("answer"), answer)
                if (isCorrect != null) set(field("isCorrect"), isCorrect)
            }
            .create()
    }
}

/**
 * PieceProblemStat 도메인 픽스처
 */
object PieceProblemStatFixture {
    /**
     * 기본 PieceProblemStat 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     */
    fun create(
        id: Long? = null,
        pieceId: PieceId? = null,
        problemId: ProblemId? = null,
        totalCount: Int? = null,
        correctCount: Int? = null,
        correctnessRate: CorrectnessRate? = null
    ): PieceProblemStat {
        val totalCnt = totalCount ?: (10..100).random()
        val correctCnt = correctCount ?: (0..totalCnt).random()
        
        return PieceProblemStat(
            id = id ?: (1L..999999L).random(),
            pieceId = pieceId ?: ValueObjectFixture.pieceId(),
            problemId = problemId ?: ValueObjectFixture.problemId(),
            totalCount = totalCnt,
            correctCount = correctCnt,
            correctnessRate = correctnessRate ?: ValueObjectFixture.correctnessRate(correctCnt.toDouble() / totalCnt)
        )
    }
}

/**
 * PieceStudentStat 도메인 픽스처
 */
object PieceStudentStatFixture {
    /**
     * 기본 PieceStudentStat 객체를 생성하되, 특정 값을 오버라이드할 수 있는 함수
     */
    fun create(
        id: Long? = null,
        assignmentId: AssignmentId? = null,
        pieceId: PieceId? = null,
        studentId: StudentId? = null,
        totalCount: Int? = null,
        correctCount: Int? = null,
        correctnessRate: CorrectnessRate? = null
    ): PieceStudentStat {
        val totalCnt = totalCount ?: (10..100).random()
        val correctCnt = correctCount ?: (0..totalCnt).random()
        
        return PieceStudentStat(
            id = id ?: (1L..999999L).random(),
            assignmentId = assignmentId ?: ValueObjectFixture.assignmentId(),
            pieceId = pieceId ?: ValueObjectFixture.pieceId(),
            studentId = studentId ?: ValueObjectFixture.studentId(),
            totalCount = totalCnt,
            correctCount = correctCnt,
            correctnessRate = correctnessRate ?: ValueObjectFixture.correctnessRate(correctCnt.toDouble() / totalCnt)
        )
    }
}

/**
 * 값 객체 픽스처들
 */
object ValueObjectFixture {
    fun assignmentId(value: Long? = null) = AssignmentId(value ?: (1L..999999L).random())
    fun pieceId(value: Long? = null) = PieceId(value ?: (1L..999999L).random())
    fun studentId(value: Long? = null) = StudentId(value ?: (1L..999999L).random())
    fun teacherId(value: Long? = null) = TeacherId(value ?: (1L..999999L).random())
    fun problemId(value: Long? = null) = ProblemId(value ?: (1L..999999L).random())
    fun correctnessRate(value: Double? = null) = CorrectnessRate(value ?: kotlin.random.Random.nextDouble(0.0, 1.0))
    fun position(value: Double? = null) = Position(value ?: kotlin.random.Random.nextDouble(1.0, 100.0))
    fun pieceName(value: String? = null) = PieceName(value ?: "테스트 학습지 ${(1000..9999).random()}")
}