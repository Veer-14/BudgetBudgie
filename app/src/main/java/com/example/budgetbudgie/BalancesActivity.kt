package com.example.budgetbudgie

import Data.database.AppDatabase
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.data.Account
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class BalancesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvForecastBalance: TextView
    private lateinit var spinnerTimePeriod: Spinner
    private lateinit var spinnerAccount: Spinner
    private lateinit var btnAddAccount: Button
    private lateinit var accountsContainer: LinearLayout
    private lateinit var barChart: BarChart

    private var allAccounts: List<Account> = emptyList()


    private var isDataLoaded = false

    private val currentUserId = 1

    private val barColors = listOf(
        Color.parseColor("#3B82F6"),
        Color.parseColor("#22C55E"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#EF4444"),
        Color.parseColor("#8B5CF6"),
        Color.parseColor("#06B6D4")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balances)

        db = AppDatabase.getDatabase(this)

        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        tvForecastBalance = findViewById(R.id.tvForecastBalance)
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod)
        spinnerAccount = findViewById(R.id.spinnerAccount)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        accountsContainer = findViewById(R.id.accountsContainer)
        barChart = findViewById(R.id.barChart)

        setupBarChart()
        setupTimePeriodSpinner()
        setupBottomNav()
        loadAccountsAndRefresh()

        btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }



    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            legend.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            setNoDataText("Add accounts to see your balance chart")
            setNoDataTextColor(Color.parseColor("#9CA3AF"))

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#9CA3AF")
                textSize = 11f
                granularity = 1f
                isGranularityEnabled = true
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1F334155")
                textColor = Color.parseColor("#9CA3AF")
                textSize = 10f
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R${value.toInt()}"
                    }
                }
            }

            axisRight.isEnabled = false
        }
    }



    private fun setupTimePeriodSpinner() {
        val periods = listOf("Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerTimePeriod.adapter = adapter

        spinnerTimePeriod.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long
                ) {

                    if (isDataLoaded) refreshGraph()
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>) {}
            }
    }

    private fun setupAccountSpinner(accounts: List<Account>) {
        val names = mutableListOf("All Accounts")
        names.addAll(accounts.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(R.layout.spinner_item)


        spinnerAccount.onItemSelectedListener = null
        spinnerAccount.adapter = adapter

        spinnerAccount.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long
                ) {
                    if (isDataLoaded) refreshGraph()
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>) {}
            }
    }



    private fun loadAccountsAndRefresh() {
        lifecycleScope.launch {
            try {
                allAccounts = db.accountDao().getAccountsForUser(currentUserId)
                val expenses = db.expenseDao().getAllExpenses()
                val totalBalance = allAccounts.sumOf { it.balance }
                val totalSpent = expenses.sumOf { it.amount }
                val avgDailySpend = if (expenses.isNotEmpty()) totalSpent / 30 else 0.0
                val forecast = totalBalance - (avgDailySpend * 30)

                runOnUiThread {
                    tvCurrentBalance.text = "R%.2f".format(totalBalance)
                    tvForecastBalance.text = "R%.2f".format(forecast.coerceAtLeast(0.0))
                    setupAccountSpinner(allAccounts)
                    displayAccounts(allAccounts)
                    isDataLoaded = true
                    refreshGraph()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@BalancesActivity,
                        "Error loading data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

   //Bar graph

    private fun refreshGraph() {
        if (allAccounts.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        val selectedPos = spinnerAccount.selectedItemPosition
        val accountsToShow = if (selectedPos == 0) allAccounts
        else if (selectedPos - 1 < allAccounts.size) listOf(allAccounts[selectedPos - 1])
        else allAccounts

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val colors = ArrayList<Int>()

        accountsToShow.forEachIndexed { index, account ->
            entries.add(BarEntry(index.toFloat(), account.balance.toFloat()))
            labels.add(
                if (account.name.length > 10) account.name.take(9) + "…"
                else account.name
            )
            colors.add(barColors[index % barColors.size])
        }

        val dataSet = BarDataSet(entries, "Account Balances").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R%.0f".format(value)
                }
            }
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            setVisibleXRangeMaximum(accountsToShow.size.toFloat().coerceAtLeast(3f))
            moveViewToX(0f)
            animateY(600)
            invalidate()
        }
    }



    private fun displayAccounts(accounts: List<Account>) {
        accountsContainer.removeAllViews()

        if (accounts.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "No accounts added yet. Tap 'Add Account' to get started."
            emptyView.setTextColor(0xFF9CA3AF.toInt())
            emptyView.textSize = 13f
            emptyView.setPadding(0, 8, 0, 8)
            accountsContainer.addView(emptyView)
            return
        }

        accounts.forEachIndexed { index, account ->
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


            val dotView = View(this)
            val dotParams = LinearLayout.LayoutParams(16, 16)
            dotParams.setMargins(0, 0, 16, 0)
            dotParams.gravity = android.view.Gravity.CENTER_VERTICAL
            dotView.layoutParams = dotParams
            dotView.setBackgroundColor(barColors[index % barColors.size])
            cardLayout.addView(dotView)

            val nameView = TextView(this)
            nameView.text = account.name
            nameView.setTextColor(0xFFFFFFFF.toInt())
            nameView.textSize = 15f
            val nameParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            nameView.layoutParams = nameParams
            cardLayout.addView(nameView)

            val balanceView = TextView(this)
            balanceView.text = "R%.2f".format(account.balance)
            balanceView.setTextColor(0xFF22C55E.toInt())
            balanceView.textSize = 15f
            balanceView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            cardLayout.addView(balanceView)


          //Delete button
            val deleteBtn = Button(this)
            deleteBtn.text = "Delete"
            deleteBtn.textSize = 13f
            deleteBtn.setTextColor(Color.WHITE)
            deleteBtn.setBackgroundResource(R.drawable.bg_delete_btn)
            val deleteBtnParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                80
            )
            deleteBtnParams.setMargins(16, 0, 0, 0)
            deleteBtn.layoutParams = deleteBtnParams
            deleteBtn.setPadding(24, 0, 24, 0)
            deleteBtn.setOnClickListener {
                showDeleteConfirmDialog(account)
            }
            cardLayout.addView(deleteBtn)

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
                    Account(name = name, balance = balance, userId = currentUserId)
                )
                runOnUiThread {
                    Toast.makeText(
                        this@BalancesActivity,
                        "Account added!",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    isDataLoaded = false
                    loadAccountsAndRefresh()
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

    private fun showDeleteConfirmDialog(account: Account) {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete \"${account.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.accountDao().deleteAccount(account)
                    runOnUiThread {
                        Toast.makeText(
                            this@BalancesActivity,
                            "${account.name} deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        isDataLoaded = false
                        loadAccountsAndRefresh()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}