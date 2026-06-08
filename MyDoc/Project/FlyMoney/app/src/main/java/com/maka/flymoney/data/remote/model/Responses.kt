package com.maka.flymoney.data.remote.model

data class BetResponse(
    val success: Boolean,
    val newBalance: Double
)

data class CashoutResponse(
    val success: Boolean,
    val multiplier: Double,
    val winnings: Double,
    val newBalance: Double
)
