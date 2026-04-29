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
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ExpensesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var db: AppDatabase
    private lateinit var tvTotal: TextView
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnFilter: Button
    private lateinit var tvCategoryTotals: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
            .setOnClickListener {
                startActivity(Intent(this, AddExpenseActivity::class.java))
            }

        setupBottomNav()

        recyclerView = findViewById(R.id.recyclerExpenses)
        tvTotal = findViewById(R.id.tvTotal)
        tvCategoryTotals = findViewById(R.id.tvCategoryTotals)

        adapter = ExpenseAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        db = AppDatabase.getDatabase(this)

        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnFilter = findViewById(R.id.btnFilter)

        // Open DatePicker
        etStartDate.setOnClickListener {
            showDatePicker(etStartDate)
        }

        etEndDate.setOnClickListener {
            showDatePicker(etEndDate)
        }

        // Apply filter
        btnFilter.setOnClickListener {
            val start = etStartDate.text.toString()
            val end = etEndDate.text.toString()

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Select both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            filterExpenses(start, end)
        }

        loadExpenses()
        setupBottomNav()
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()
            updateUI(expenses)
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

    private fun showDatePicker(target: EditText) {

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->

                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)

                val selectedDate = "$year-$formattedMonth-$formattedDay"
                target.setText(selectedDate)

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun filterExpenses(start: String, end: String) {
        lifecycleScope.launch {

            val expenses = db.expenseDao()
                .getExpenseBetweenDates(start, end)

            updateUI(expenses)

            val categoryTotals = db.expenseDao()
                .getCategoryTotals(start, end)

            val builder = StringBuilder()

            categoryTotals.forEach {
                builder.append("${it.category}: R%.2f\n".format(it.total))
            }

            tvCategoryTotals.text = builder.toString()
        }
    }

    private fun updateUI(expenses: List<Expense>) {
        adapter.updateData(expenses)

        val total = expenses.sumOf { it.amount }
        tvTotal.text = "R%.2f".format(total)
    }
}