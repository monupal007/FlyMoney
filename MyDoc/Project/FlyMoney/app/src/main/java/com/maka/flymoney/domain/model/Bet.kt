package com.maka.flymoney.domain.model

data class Bet(
    val uid: String = "",
    val username: String = "",
    val amount: Double = 0.0,
    val autoCashoutAt: Double? = null,
    val placedAt: Long = 0L,
    val cashedOut: Boolean = false,
    val multiplier: Double? = null,
    val winnings: Double? = null,
    val cashedOutAt: Long? = null
)
