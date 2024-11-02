package com.ianterhaar.accountit.models

data class SavingsTransaction(
    val goalName: String,
    val amount: Double,
    val date: String
)

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String? = null
)