package com.maka.flymoney.domain.model

data class Round(
    val roundId: String = "",
    val state: RoundState = RoundState.WAITING,
    val multiplier: Double = 1.0,
    val crashAt: Double? = null,
    val startTime: Long = 0L,
    val countdownSec: Int = 0,
    val serverSeedHash: String? = null
)

enum class RoundState {
    WAITING, RUNNING, CRASHED
}
