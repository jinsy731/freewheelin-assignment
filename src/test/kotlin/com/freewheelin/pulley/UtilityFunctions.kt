package com.freewheelin.pulley

/**
 * 다른 [Double] 값과 지정된 소수점 자리까지 동일한지 비교합니다.
 *
 * @param other 비교할 Double 값
 * @param decimalPlaces 비교할 소수점 자리 수
 * @return 동일하면 true, 아니면 false
 */
fun Double.isEqualToUpTo(other: Double, decimalPlaces: Int): Boolean {
    require(decimalPlaces >= 0) { "소수점 자리는 0 이상이어야 합니다." }
    val format = "%.${decimalPlaces}f"
    return String.format(format, this) == String.format(format, other)
}