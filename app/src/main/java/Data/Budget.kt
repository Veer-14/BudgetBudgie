package com.example.budgetbudgie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey val id: Int = 0,
    var minAmount: Double = 0.0,
    var maxAmount: Double = 0.0
)