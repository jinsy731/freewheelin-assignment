
## 성능 및 비용 최적화

### 1. 이미 구현된 최적화 사항

#### 1.1 데이터베이스 인덱스 최적화

**구현된 인덱스**:
```sql
-- problems 테이블 복합 인덱스
CREATE INDEX idx_unit_code_level_type ON problems (unit_code, problem_level, problem_type);

-- piece_problems 테이블 순서 정렬 인덱스
CREATE INDEX idx_piece_position ON piece_problems (piece_id, position);

-- 사용자 테이블 고유 인덱스
CREATE UNIQUE INDEX idx_users_username ON users (username);
CREATE UNIQUE INDEX idx_users_email ON users (email);
```

**성능 효과**:
- 문제 조회 시 복합 조건 필터링 성능 향상
- 학습지 문제 정렬 성능 향상

#### 1.2 통계 테이블 사전 구축

**구현된 통계 테이블**:
```kotlin
// 문제별 통계 테이블
data class PieceProblemStat(
    val pieceId: PieceId,
    val problemId: ProblemId,
    val totalCount: Int,
    val correctCount: Int,
    val correctnessRate: CorrectnessRate
)

// 학생별 통계 테이블
data class PieceStudentStat(
    val assignmentId: AssignmentId,
    val pieceId: PieceId,
    val studentId: StudentId,
    val totalCount: Int,
    val correctCount: Int,
    val correctnessRate: CorrectnessRate
)
```

**성능 효과**:
- 통계 조회 시 집계 연산 제거로 응답 시간 단축
- 이벤트 기반 업데이트로 응답 지연 감소 DB 부하 감소

#### 1.3 이벤트 기반 비동기 처리

**구현된 이벤트 시스템**:
```kotlin
// 이벤트 처리
@Component
class StatisticsEventHandler {
    @EventListener
    fun handleSubmissionGraded(event: SubmissionGradedEvent) {
        statisticsUpdateService.updateStatistics(event)
    }
}
```

**성능 효과**:
- 채점 API 응답 시간 단축 (통계 업데이트 비동기 분리)
- 시스템 확장성 향상

#### 1.4 JPA 최적화 설정

**적용된 설정**:
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20        # 배치 처리 최적화
    open-in-view: false         # 불필요한 지연 로딩 방지
```

### 2. 추가 개선 방안

#### 2.1 캐싱 전략 도입

**Redis 기반 캐싱**:
```kotlin
@Service
class ProblemService {
    @Cacheable(
        value = ["problems"],
        key = "#unitCodes?.toString() + '_' + #level + '_' + #problemType"
    )
    fun searchProblems(
        unitCodes: List<String>?,
        level: String?,
        problemType: String?
    ): List<Problem> {
        return problemRepository.findByConditions(unitCodes, level, problemType)
    }
}

@CacheEvict(value = ["problems"], allEntries = true)
fun clearProblemCache() {
    // 문제 데이터 변경 시 캐시 무효화
}
```

**예상 효과**:
- 반복 조회 시 응답 시간 단축
- DB 부하 감소

#### 2.2 커넥션 풀 최적화

**HikariCP 설정 개선**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

**예상 효과**:
- 동시 접속자 증가 시 DB 연결 대기 시간 감소

#### 2.3 페이징 처리 도입

**대용량 데이터 처리 최적화**:
```kotlin
@GetMapping("/problems")
fun getProblems(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam unitCodes: List<String>?
): Page<ProblemResponse> {
    val pageable = PageRequest.of(page, size)
    return problemService.findProblems(pageable, unitCodes)
        .map { ProblemResponse.from(it) }
}
```

**예상 효과**:
- 메모리 사용량 감소
- 초기 로딩 시간 단축

---

## 🔄 학습지 순서 변경 로직 최적화

### 1. 현재 구현 및 문제점

#### 1.1 현재 Double 기반 Position 시스템

**현재 구현**:
```kotlin
// Position.kt - 부동소수점 기반 중간값 계산
companion object {
    fun between(before: Position?, after: Position?): Position {
        return when {
            before == null && after == null -> Position(1.0)
            before == null -> Position(after!!.value / 2.0)
            after == null -> Position(before.value + 1.0)
            else -> Position((before.value + after.value) / 2.0)  // 정밀도 손실 지점
        }
    }
}
```

**현재 순서 변경 서비스**:
```kotlin
// PieceOrderUpdateService.kt
fun updateProblemOrder(command: ProblemOrderUpdateCommand): ProblemOrderUpdateResult {
    val updatedProblem = problemToMove.moveTo(prevProblem, nextProblem)
    pieceProblemRepository.save(updatedProblem)
    return ProblemOrderUpdateResult(/* ... */)
}
```

#### 1.2 현재 시스템의 문제점

**1. 정밀도 한계**:
- 반복적인 중간값 계산으로 `1.0` → `1.5` → `1.25` → `1.125` → ...
- 부동소수점 정밀도 한계에 도달하면 순서 변경 불가

**2. 리밸런싱 메커니즘 부재**:
- 정밀도 한계 도달 시 자동 리밸런싱 로직 없음
- 수동 개입 필요

**3. 클라이언트-서버 통신 비효율**:
- 현재는 최적화되어 있지만, 정밀도 문제로 인한 잠재적 이슈


### 2. 권장 개선방안

#### 2.1 문자열 기반 Rank 시스템 도입

**개선된 Position 구조**:
```kotlin
// 기존 Double 타입을 String 타입으로 변경
data class Position(val value: String) {
    
    companion object {
        private val CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        
        fun between(before: Position?, after: Position?): Position {
            return when {
                before == null && after == null -> Position("n") // 중간값
                before == null -> Position(generateBefore(after!!.value))
                after == null -> Position(generateAfter(before!!.value))
                else -> Position(generateMiddle(before.value, after.value))
            }
        }
        
        private fun generateMiddle(prev: String, next: String): String {
            // Base62 기반 중간값 계산 - 무한 확장 가능
            val prevChars = prev.toCharArray()
            val nextChars = next.toCharArray()
            
            // 두 문자열 사이의 사전순 중간값 생성
            return calculateLexicographicMiddle(prevChars, nextChars)
        }
    }
}
```