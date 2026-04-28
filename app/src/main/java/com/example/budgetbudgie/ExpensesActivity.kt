package com.example.budgetbudgie

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgie.R
import com.example.budgetbudgie.data.Expense
import Data.database.AppDatabase
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ExpensesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var db: AppDatabase
    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        recyclerView = findViewById(R.id.recyclerExpenses)
        tvTotal = findViewById(R.id.tvTotal)

        adapter = ExpenseAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        db = AppDatabase.getDatabase(this)

        loadExpenses()
        setupBottomNav()
    }
    private fun loadExpenses() {
        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()

            adapter.updateData(expenses)

            val total = expenses.sumOf { it.amount }
            tvTotal.text = "R%.2f".format(total)
        }
    }
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.expenses

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.expenses -> true

                R.id.balance -> {
                    startActivity(Intent(this, BalancesActivity::class.java))
                    finish()
                    true
                }
                R.id.home -> {
                    startActivity(Intent(this, HomePage::class.java))
                    finish()
                    true
                }
                R.id.shared -> {
                    startActivity(Intent(this, SharedBudgetActivity::class.java))
                    finish()
                    true
                }
                R.id.analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}