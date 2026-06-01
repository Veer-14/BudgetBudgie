package com.example.budgetbudgie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String = "",
    var balance: Double = 0.0,
    var userId: Int = 0
)