package com.maka.flymoney.domain.model

data class Transaction(
    val id: String = "",
    val uid: String = "",
    val type: TransactionType = TransactionType.BET,
    val amount: Double = 0.0,
    val multiplier: Double? = null,
    val roundId: String? = null,
    val timestamp: Long = 0L
)

enum class TransactionType {
    BET, WIN, BUST, DEPOSIT, WITHDRAW
}
