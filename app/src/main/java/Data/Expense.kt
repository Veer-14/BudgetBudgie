package com.example.budgetbudgie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = "",
    val imageUri: String? = null

)