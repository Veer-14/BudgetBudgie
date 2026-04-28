package com.example.budgetbudgie

import Data.database.AppDatabase
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.data.Budget
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        updateBudgie(12)
        setupBottomNav()
        setupTopButtons()
        setupQuickActions()
        loadRecentExpenses()
        loadBudget()
         findViewById<View>(R.id.btnSetBudget).setOnClickListener {
            showBudgetDialog()
        }

        findViewById<View>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
    }

    // ---------------- BUDGIE IMAGE ----------------
    private fun updateBudgie(progress: Int) {
        val img = findViewById<ImageView>(R.id.imgBudgie)

        when {
            progress < 30 -> img.setImageResource(R.drawable.budgie_happy)
            progress < 70 -> img.setImageResource(R.drawable.budgie_ok)
            else -> img.setImageResource(R.drawable.budgie_sad)
        }
    }

    // ---------------- BUDGET DIALOG ----------------
    private fun showBudgetDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_budget, null)

        val minInput = view.findViewById<EditText>(R.id.etMin)
        val maxInput = view.findViewById<EditText>(R.id.etMax)

        AlertDialog.Builder(this)
            .setTitle("Set Monthly Budget")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->

                val min = minInput.text.toString().toDoubleOrNull() ?: 0.0
                val max = maxInput.text.toString().toDoubleOrNull() ?: 0.0

                val db = AppDatabase.getDatabase(this)

                lifecycleScope.launch {
                    db.budgetDao().insertBudget(Budget(1, min, max))
                    loadBudget()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- RECENT EXPENSES ----------------
    private fun loadRecentExpenses() {
        val container = findViewById<LinearLayout>(R.id.recentContainer)
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()

            container.removeAllViews()

            for (expense in expenses.reversed().take(5)) {

                val item = layoutInflater.inflate(R.layout.item_expense, container, false)

                val tvDescription = item.findViewById<TextView>(R.id.tvDescription)
                val tvCategory = item.findViewById<TextView>(R.id.tvCategory)
                val tvDate = item.findViewById<TextView>(R.id.tvDate)
                val tvAmount = item.findViewById<TextView>(R.id.tvAmount)

                tvDescription.text = expense.description
                tvCategory.text = expense.category
                tvDate.text = expense.date
                tvAmount.text = "R${expense.amount}"

                container.addView(item)
            }
        }
    }

    // ---------------- BUDGET LOAD ----------------
    private fun loadBudget() {
        val minText = findViewById<TextView>(R.id.txtMin)
        val maxText = findViewById<TextView>(R.id.txtMax)

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val budget = db.budgetDao().getBudget()

            budget?.let {
                minText.text = "R${it.minAmount} min"
                maxText.text = "R${it.maxAmount} max"
            }
        }
    }

    // ---------------- BOTTOM NAV ----------------
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.home -> true

                R.id.balance -> {
                    startActivity(Intent(this, BalancesActivity::class.java))
                    true
                }

                R.id.expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
                    true
                }

                R.id.shared -> {
                    startActivity(Intent(this, SharedBudgetActivity::class.java))
                    true
                }

                R.id.analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    // ---------------- TOP BUTTONS ----------------
    private fun setupTopButtons() {

        findViewById<ImageView>(R.id.btnLogout).setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<ImageView>(R.id.btnCustomize).setOnClickListener {
            startActivity(Intent(this, DashboardCustomizationActivity::class.java))
        }
    }

    // ---------------- QUICK ACTIONS ----------------
    private fun setupQuickActions() {

        findViewById<View>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<View>(R.id.btnBalances).setOnClickListener {
            startActivity(Intent(this, BalancesActivity::class.java))
        }

        findViewById<View>(R.id.btnShared).setOnClickListener {
            startActivity(Intent(this, SharedBudgetActivity::class.java))
        }

        findViewById<View>(R.id.btnAnalytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadBudget()
    }
}