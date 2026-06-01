package com.example.budgetbudgie

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbudgie.data.Account
import com.example.budgetbudgie.data.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class BalancesActivity : AppCompatActivity() {

    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvForecastBalance: TextView
    private lateinit var spinnerAccount: Spinner
    private lateinit var btnAddAccount: Button
    private lateinit var accountsContainer: LinearLayout
    private lateinit var barChart: BarChart

    private var allAccounts: MutableList<Account> = mutableListOf()
    private var isDataLoaded = false

    private val currentUserId = 1

    private val accountRef =
        FirebaseDatabase.getInstance().getReference("accounts")

    private val expenseRef =
        FirebaseDatabase.getInstance().getReference("expenses")

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

        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        tvForecastBalance = findViewById(R.id.tvForecastBalance)
        spinnerAccount = findViewById(R.id.spinnerAccount)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        accountsContainer = findViewById(R.id.accountsContainer)
        barChart = findViewById(R.id.barChart)

        setupBarChart()
        setupBottomNav()
        loadAccounts()

        btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }

    // ---------------- LOAD ACCOUNTS FROM FIREBASE ----------------
    private fun loadAccounts() {

        accountRef.orderByChild("userId").equalTo(currentUserId.toDouble())
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val accounts = mutableListOf<Account>()

                    for (child in snapshot.children) {
                        val acc = child.getValue(Account::class.java)
                        if (acc != null) accounts.add(acc)
                    }

                    allAccounts = accounts

                    loadExpensesAndUpdateUI()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ---------------- LOAD EXPENSES + CALCULATE ----------------
    private fun loadExpensesAndUpdateUI() {

        expenseRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val expenses = mutableListOf<Expense>()

                for (child in snapshot.children) {
                    val exp = child.getValue(Expense::class.java)
                    if (exp != null) expenses.add(exp)
                }

                val totalBalance = allAccounts.sumOf { it.balance }
                val totalSpent = expenses.sumOf { it.amount }

                val avgDailySpend = if (expenses.isNotEmpty()) totalSpent / 30 else 0.0
                val forecast = totalBalance - (avgDailySpend * 30)

                tvCurrentBalance.text = "R%.2f".format(totalBalance)
                tvForecastBalance.text = "R%.2f".format(forecast.coerceAtLeast(0.0))

                setupAccountSpinner()
                displayAccounts()
                refreshGraph()

                isDataLoaded = true
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ---------------- ADD ACCOUNT ----------------
    private fun showAddAccountDialog() {

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)

        val etName = view.findViewById<EditText>(R.id.etAccountName)
        val etBalance = view.findViewById<EditText>(R.id.etAccountBalance)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelAccount)
        val btnSave = view.findViewById<Button>(R.id.btnSaveAccount)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()
            val balance = etBalance.text.toString().toDoubleOrNull()

            if (name.isEmpty() || balance == null) {
                Toast.makeText(this, "Enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = accountRef.push().key!!

            val account = Account(
                name = name,
                balance = balance,
                userId = currentUserId
            )

            accountRef.child(id).setValue(account)

            Toast.makeText(this, "Account added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ---------------- DELETE ACCOUNT ----------------
    private fun deleteAccount(account: Account) {

        accountRef.orderByChild("name").equalTo(account.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }

                    Toast.makeText(this@BalancesActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ---------------- DISPLAY ACCOUNTS ----------------
    private fun displayAccounts() {

        accountsContainer.removeAllViews()

        if (allAccounts.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No accounts yet"
            accountsContainer.addView(empty)
            return
        }

        allAccounts.forEachIndexed { index, account ->

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val name = TextView(this)
            name.text = account.name
            name.setTextColor(Color.WHITE)

            val balance = TextView(this)
            balance.text = "R%.2f".format(account.balance)
            balance.setTextColor(Color.GREEN)

            val delete = Button(this)
            delete.text = "Delete"
            delete.setOnClickListener {
                deleteAccount(account)
            }

            row.addView(name)
            row.addView(balance)
            row.addView(delete)

            accountsContainer.addView(row)
        }
    }

    // ---------------- SPINNER ----------------
    private fun setupAccountSpinner() {

        val names = mutableListOf("All Accounts")
        names.addAll(allAccounts.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        spinnerAccount.adapter = adapter
    }

    // ---------------- BAR CHART ----------------
    private fun setupBarChart() {

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.axisRight.isEnabled = false
    }

    private fun refreshGraph() {

        if (allAccounts.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        allAccounts.forEachIndexed { index, account ->
            entries.add(BarEntry(index.toFloat(), account.balance.toFloat()))
            labels.add(account.name)
        }

        val dataSet = BarDataSet(entries, "Accounts")

        val data = BarData(dataSet)

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }

    // ---------------- NAV ----------------
    private fun setupBottomNav() {

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)

        nav.selectedItemId = R.id.balance

        nav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> startActivity(Intent(this, HomePage::class.java))
                R.id.expenses -> startActivity(Intent(this, ExpensesActivity::class.java))
                R.id.shared -> startActivity(Intent(this, SharedBudgetActivity::class.java))
                R.id.analytics -> startActivity(Intent(this, AnalyticsActivity::class.java))

                else -> return@setOnItemSelectedListener false
            }

            true
        }
    }
}