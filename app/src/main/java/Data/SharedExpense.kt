package com.example.budgetbudgie.data

data class SharedExpense(
    var id: String = "",
    var budgetId: String = "",
    var description: String = "",
    var amount: Double = 0.0
)