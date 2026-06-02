package com.example.budgetbudgie.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo


@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "balance")
    var balance: Double = 0.0,

    @ColumnInfo(name = "userId")
    var userId: String = ""
)