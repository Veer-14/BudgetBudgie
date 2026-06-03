package com.example.budgetbudgie.data

data class SharedBudget(
    var id: String = "",
    var name: String = "",
    var totalBudget: Double = 0.0,
    val userId: String = ""
)