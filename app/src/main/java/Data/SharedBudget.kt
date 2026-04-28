package com.example.budgetbudgie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shared_budgets")
data class SharedBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val totalBudget: Double
)