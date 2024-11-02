package com.ianterhaar.accountit.models

data class BudgetCategory(
    val name: String,
    val budget: Double,
    val spent: Double
)