package com.maka.flymoney.domain.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val walletBalance: Double = 0.0,
    val totalBets: Int = 0,
    val totalWins: Int = 0,
    val biggestWin: Double = 0.0,
    val createdAt: Long = 0L
)
