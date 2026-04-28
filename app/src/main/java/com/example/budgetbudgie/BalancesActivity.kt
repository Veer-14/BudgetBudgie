package com.example.budgetbudgie

import Data.database.AppDatabase
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.data.Account
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class BalancesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvForecastBalance: TextView
    private lateinit var spinnerTimePeriod: Spinner
    private lateinit var btnAddAccount: Button
    private lateinit var accountsContainer: LinearLayout
    private lateinit var barGraphContainer: LinearLayout
    private lateinit var graphLabelsContainer: LinearLayout


    private val currentUserId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balances)

        db = AppDatabase.getDatabase(this)

        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        tvForecastBalance = findViewById(R.id.tvForecastBalance)
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        accountsContainer = findViewById(R.id.accountsContainer)
        barGraphContainer = findViewById(R.id.barGraphContainer)
        graphLabelsContainer = findViewById(R.id.graphLabelsContainer)

        setupTimePeriodSpinner()
        setupBottomNav()
        loadBalances()

        btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }

    private fun setupTimePeriodSpinner() {
        val periods = listOf("Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimePeriod.adapter = adapter

        spinnerTimePeriod.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    loadBalances()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }
    }

    private fun loadBalances() {
        lifecycleScope.launch {
            val accounts = db.accountDao().getAccountsForUser(currentUserId)
            val totalBalance = accounts.sumOf { it.balance }
            val expenses = db.expenseDao().getAllExpenses()
            val totalSpent = expenses.sumOf { it.amount }

            val avgDailySpend = if (expenses.isNotEmpty()) totalSpent / 30 else 0.0
            val forecast = totalBalance - (avgDailySpend * 30)

            runOnUiThread {
                tvCurrentBalance.text = "R%.2f".format(totalBalance)
                tvForecastBalance.text = "R%.2f".format(forecast)
                drawBarGraph(accounts.map { it.balance }, accounts.map { it.name })
                displayAccounts(accounts)
            }
        }
    }

    private fun drawBarGraph(values: List<Double>, labels: List<String>) {
        barGraphContainer.removeAllViews()
        graphLabelsContainer.removeAllViews()

        if (values.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "No accounts yet"
            emptyText.setTextColor(0xFF9CA3AF.toInt())
            emptyText.textSize = 13f
            barGraphContainer.addView(emptyText)
            return
        }

        val maxValue = values.maxOrNull() ?: 1.0
        val graphHeight = 150

        values.forEachIndexed { index, value ->
            // Bar column wrapper
            val columnLayout = LinearLayout(this)
            columnLayout.orientation = LinearLayout.VERTICAL
            columnLayout.gravity = android.view.Gravity.BOTTOM
            val columnParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            columnParams.setMargins(4, 0, 4, 0)
            columnLayout.layoutParams = columnParams

            // The bar itself
            val barView = View(this)
            val barHeightRatio = if (maxValue > 0) value / maxValue else 0.0
            val barHeightPx = (barHeightRatio * graphHeight).toInt()
                .coerceAtLeast(8)
            val barParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                barHeightPx
            )
            barView.layoutParams = barParams
            barView.setBackgroundResource(R.drawable.bg_bar)
            columnLayout.addView(barView)
            barGraphContainer.addView(columnLayout)

            // Label below the graph
            val labelView = TextView(this)
            labelView.text = if (labels[index].length > 8)
                labels[index].take(7) + "…"
            else labels[index]
            labelView.setTextColor(0xFF9CA3AF.toInt())
            labelView.textSize = 10f
            labelView.gravity = android.view.Gravity.CENTER
            val labelParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            labelView.layoutParams = labelParams
            graphLabelsContainer.addView(labelView)
        }
    }

    private fun displayAccounts(accounts: List<Account>) {
        accountsContainer.removeAllViews()

        if (accounts.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "No accounts added yet. Tap '+ Add Account' to get started."
            emptyView.setTextColor(0xFF9CA3AF.toInt())
            emptyView.textSize = 13f
            emptyView.setPadding(0, 8, 0, 8)
            accountsContainer.addView(emptyView)
            return
        }

        accounts.forEach { account ->
            val cardLayout = LinearLayout(this)
            cardLayout.orientation = LinearLayout.HORIZONTAL
            cardLayout.gravity = android.view.Gravity.CENTER_VERTICAL
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, 8)
            cardLayout.layoutParams = cardParams
            cardLayout.setBackgroundResource(R.drawable.card_dark)
            cardLayout.setPadding(32, 32, 32, 32)

            // Account name
            val nameView = TextView(this)
            nameView.text = account.name
            nameView.setTextColor(0xFFFFFFFF.toInt())
            nameView.textSize = 15f
            val nameParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            nameView.layoutParams = nameParams
            cardLayout.addView(nameView)

            // Balance
            val balanceView = TextView(this)
            balanceView.text = "R%.2f".format(account.balance)
            balanceView.setTextColor(0xFF22C55E.toInt())
            balanceView.textSize = 15f
            balanceView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            cardLayout.addView(balanceView)

            accountsContainer.addView(cardLayout)
        }
    }

    private fun showAddAccountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)
        val etName = dialogView.findViewById<EditText>(R.id.etAccountName)
        val etBalance = dialogView.findViewById<EditText>(R.id.etAccountBalance)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelAccount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveAccount)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val balanceText = etBalance.text.toString().trim()

            if (name.isEmpty() || balanceText.isEmpty()) {
                etName.error = if (name.isEmpty()) "Enter account name" else null
                etBalance.error = if (balanceText.isEmpty()) "Enter balance" else null
                return@setOnClickListener
            }

            val balance = balanceText.toDoubleOrNull()
            if (balance == null) {
                etBalance.error = "Enter a valid amount"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.accountDao().insertAccount(
                    Account(
                        name = name,
                        balance = balance,
                        userId = currentUserId
                    )
                )
                runOnUiThread {
                    dialog.dismiss()
                    loadBalances()
                }
            }
        }

        dialog.show()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.balance

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.balance -> true
                R.id.home -> {
                    startActivity(Intent(this, HomePage::class.java))
                    finish()
                    true
                }
                R.id.expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
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