
## 잠재적 위험요소 및 해결방안

#### 1.통계 테이블 동시성 문제

**위험요소**:
- `piece_problem_stats` 테이블이 여러 학생의 동시 제출에 의해 업데이트될 때 데이터 불일치 발생 가능
- Race Condition으로 인한 통계 데이터 손실

**현재 코드의 문제점**:
```kotlin
// StatisticsUpdateService.kt - 동시성 이슈 발생 지점
val existingStat = pieceProblemStatRepository.findByPieceIdAndProblemId(event.pieceId, problemId)
val updatedStat = if (existingStat != null) {
    // 기존 통계에 증분 추가 - 동시성 문제 발생
    existingStat.update(
        existingStat.totalCount + problemStats.totalCount,
        existingStat.correctCount + problemStats.correctCount
    )
}
```

**해결방안**:

1. **비관적 락 (Pessimistic Lock) 적용**
```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM PieceProblemStatJpaEntity p WHERE p.pieceId = :pieceId AND p.problemId = :problemId")
fun findByPieceIdAndProblemIdForUpdate(pieceId: Long, problemId: Long): PieceProblemStatJpaEntity?
```

2. **원자적 업데이트 쿼리 사용**
```kotlin
@Modifying
@Query("""
    UPDATE PieceProblemStatJpaEntity p 
    SET p.totalCount = p.totalCount + :increment, 
        p.correctCount = p.correctCount + :correctIncrement,
        p.correctnessRate = (p.correctCount + :correctIncrement) / (p.totalCount + :increment)
    WHERE p.pieceId = :pieceId AND p.problemId = :problemId
""")
fun incrementCounts(pieceId: Long, problemId: Long, increment: Int, correctIncrement: Int)
```

#### 2. N+1 쿼리 문제

**위험요소**:
- `ProblemSearchService`에서 각 난이도별로 별도 쿼리 실행
- 대량 데이터 조회 시 성능 저하

**현재 문제 코드**:
```kotlin
// ProblemSearchService.kt - 각 난이도별 개별 쿼리
val lowProblems = problemRepository.findByConditions(
    unitCodes = query.unitCodeList,
    problemType = domainProblemType,
    levels = Level.LOW.levels,
    limit = distributionPlan.lowCount
)
val middleProblems = problemRepository.findByConditions(...) // 추가 쿼리
val highProblems = problemRepository.findByConditions(...)   // 추가 쿼리
```

**해결방안**:

1. **단일 쿼리로 통합**
```kotlin
@Query("""
    SELECT p FROM ProblemJpaEntity p
    WHERE p.unitCode IN :unitCodes
    AND (:problemType IS NULL OR p.problemType = :problemType)
    ORDER BY p.level ASC, FUNCTION('RAND')
""")
fun findAllByConditionsWithRandomOrder(
    unitCodes: List<String>,
    problemType: ProblemType?
): List<ProblemJpaEntity>
```

2. **애플리케이션 레벨에서 분배**
```kotlin
fun searchProblems(query: ProblemSearchQuery): ProblemSearchResult {
    val allProblems = problemRepository.findAllByConditionsWithRandomOrder(
        query.unitCodeList, query.problemType
    )
    
    return LevelDistribution.fromLevel(query.level)
        .distribute(allProblems, query.totalCount)
}
```

---