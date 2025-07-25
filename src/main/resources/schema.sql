-- 사용자 테이블
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TEACHER', 'STUDENT')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_users_username ON users (username);
CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_active ON users (is_active);

DROP TABLE IF EXISTS assignments;
CREATE TABLE assignments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    piece_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    correctness_rate NUMERIC(5, 2),
    CONSTRAINT uk_assignments_piece_student UNIQUE (piece_id, student_id)
);

DROP TABLE IF EXISTS pieces;
CREATE TABLE pieces (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL
);
CREATE INDEX idx_teacher_id ON pieces (teacher_id);

DROP TABLE IF EXISTS piece_problems;
CREATE TABLE piece_problems (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    piece_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    position DOUBLE NOT NULL,
    CONSTRAINT uk_piece_problem UNIQUE (piece_id, problem_id)
);
CREATE INDEX idx_piece_id ON piece_problems (piece_id);
CREATE INDEX idx_piece_position ON piece_problems (piece_id, position);

DROP TABLE IF EXISTS problems;
CREATE TABLE problems (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    answer TEXT NOT NULL,
    unit_code VARCHAR(10) NOT NULL,
    problem_level INT NOT NULL,
    problem_type VARCHAR(255) NOT NULL
);
CREATE INDEX idx_unit_code ON problems (unit_code);
CREATE INDEX idx_level ON problems (problem_level);
CREATE INDEX idx_problem_type ON problems (problem_type);
CREATE INDEX idx_unit_code_level_type ON problems (unit_code, problem_level, problem_type);

DROP TABLE IF EXISTS piece_problem_stats;
CREATE TABLE piece_problem_stats (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    piece_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    total_count INT NOT NULL,
    correct_count INT NOT NULL,
    correctness_rate NUMERIC(5, 2) NOT NULL,
    CONSTRAINT uk_piece_problem_stats_piece_problem UNIQUE (piece_id, problem_id)
);
CREATE INDEX idx_piece_id_piece_problem_stats ON piece_problem_stats (piece_id);
CREATE INDEX idx_problem_id_piece_problem_stats ON piece_problem_stats (problem_id);
CREATE INDEX idx_piece_problem_piece_problem_stats ON piece_problem_stats (piece_id, problem_id);

DROP TABLE IF EXISTS piece_student_stats;
CREATE TABLE piece_student_stats (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    assignment_id BIGINT NOT NULL UNIQUE,
    piece_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    total_count INT NOT NULL,
    correct_count INT NOT NULL,
    correctness_rate NUMERIC(5, 2) NOT NULL,
    CONSTRAINT uk_piece_student_stats_piece_student UNIQUE (piece_id, student_id)
);
CREATE INDEX idx_piece_id_piece_student_stats ON piece_student_stats (piece_id);
CREATE INDEX idx_student_id_piece_student_stats ON piece_student_stats (student_id);
CREATE INDEX idx_piece_student_piece_student_stats ON piece_student_stats (piece_id, student_id);

DROP TABLE IF EXISTS submissions;
CREATE TABLE submissions (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    CONSTRAINT uk_submissions_assignment_problem UNIQUE (assignment_id, problem_id)
);
CREATE INDEX idx_assignment_id ON submissions (assignment_id);
CREATE INDEX idx_problem_id ON submissions (problem_id);
CREATE INDEX idx_assignment_problem ON submissions (assignment_id, problem_id);

DROP TABLE IF EXISTS unit_codes;
CREATE TABLE unit_codes (
    id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    unit_code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX idx_unit_code_code ON unit_codes (unit_code);