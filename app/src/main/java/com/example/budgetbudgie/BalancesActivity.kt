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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class BalancesActivity : AppCompatActivity() {

    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvForecastBalance: TextView
    private lateinit var spinnerAccount: Spinner
    private lateinit var spinnerTimePeriod: Spinner
    private lateinit var btnAddAccount: Button
    private lateinit var accountsContainer: LinearLayout
    private lateinit var barChart: BarChart

    private var allAccounts: MutableList<Account> = mutableListOf()
    private var allExpenses: MutableList<Expense> = mutableListOf()

    // Currently selected filter values
    private var selectedAccountName: String = "All Accounts"
    private var selectedDays: Int = 30 // default to 1 month


    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val accountRef =
        FirebaseDatabase.getInstance().getReference("accounts")

    private val expenseRef =
        FirebaseDatabase.getInstance().getReference("expenses")

    // Colours used for each bar
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
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        accountsContainer = findViewById(R.id.accountsContainer)
        barChart = findViewById(R.id.barChart)

        setupBarChart()
        setupTimePeriodSpinner()
        setupBottomNav()
        loadData()

        btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }


    private fun setupTimePeriodSpinner() {
        val periods = listOf("1 Week", "2 Weeks", "1 Month", "3 Months")
        val dayValues = listOf(7, 14, 30, 90)

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, periods
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                view.setBackgroundColor(Color.parseColor("#1E293B"))
                return view
            }
        }

        spinnerTimePeriod.adapter = adapter
        spinnerTimePeriod.setSelection(2) // default to 1 Month

        spinnerTimePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // Update selected days and refresh the graph
                selectedDays = dayValues[pos]
                refreshGraph()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun loadData() {
        accountRef.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val accounts = mutableListOf<Account>()
                    for (child in snapshot.children) {
                        val acc = child.getValue(Account::class.java)
                        if (acc != null) accounts.add(acc)
                    }
                    allAccounts = accounts
                    setupAccountSpinner()
                    loadExpenses()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun loadExpenses() {
        expenseRef.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<Expense>()
                    for (child in snapshot.children) {
                        val exp = child.getValue(Expense::class.java)
                        if (exp != null) expenses.add(exp)
                    }
                    allExpenses = expenses
                    updateUI()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun updateUI() {
        val totalBalance = allAccounts.sumOf { it.balance }
        val totalSpent = allExpenses.sumOf { it.amount }
        val avgDailySpend = if (allExpenses.isNotEmpty()) totalSpent / 30 else 0.0
        val forecast = totalBalance - (avgDailySpend * 30)

        tvCurrentBalance.text = "R%.2f".format(totalBalance)
        tvForecastBalance.text = "R%.2f".format(forecast.coerceAtLeast(0.0))

        displayAccounts()
        refreshGraph()
    }



    private fun refreshGraph() {
        val accountsToShow = if (selectedAccountName == "All Accounts") {
            allAccounts
        } else {
            allAccounts.filter { it.name == selectedAccountName }
        }

        if (accountsToShow.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        accountsToShow.forEachIndexed { index, account ->
            entries.add(BarEntry(index.toFloat(), account.balance.toFloat()))
            labels.add(account.name)
        }

        val dataSet = BarDataSet(entries, "Balance")

        dataSet.colors = accountsToShow.mapIndexed { index, _ ->
            barColors[index % barColors.size]
        }

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.labelCount = labels.size
        barChart.animateY(400)
        barChart.invalidate()
    }


    private fun setupAccountSpinner() {
        val names = mutableListOf("All Accounts")
        names.addAll(allAccounts.map { it.name })

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, names
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                view.setBackgroundColor(Color.parseColor("#1E293B"))
                return view
            }
        }

        spinnerAccount.adapter = adapter

        spinnerAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

                selectedAccountName = names[pos]
                refreshGraph()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.textColor = Color.WHITE
        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisRight.isEnabled = false
    }

    private fun showAddAccountDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)

        val etName = view.findViewById<EditText>(R.id.etAccountName)
        val etBalance = view.findViewById<EditText>(R.id.etAccountBalance)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelAccount)
        val btnSave = view.findViewById<Button>(R.id.btnSaveAccount)

        val dialog = AlertDialog.Builder(this).setView(view).create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val balance = etBalance.text.toString().toDoubleOrNull()

            if (name.isEmpty() || balance == null) {
                Toast.makeText(this, "Enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val id = accountRef.push().key!!
            val account = Account(name = name, balance = balance, userId = currentUserId)
            accountRef.child(id).setValue(account)

            Toast.makeText(this, "Account added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun deleteAccount(account: Account) {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete \"${account.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                accountRef.orderByChild("userId").equalTo(currentUserId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                val acc = child.getValue(Account::class.java)
                                if (acc?.name == account.name) {
                                    child.ref.removeValue()
                                }
                            }
                            Toast.makeText(this@BalancesActivity, "Account deleted", Toast.LENGTH_SHORT).show()
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun displayAccounts() {
        accountsContainer.removeAllViews()

        if (allAccounts.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No accounts yet. Tap '+ Add Account' to get started."
            empty.setTextColor(Color.parseColor("#9CA3AF"))
            empty.textSize = 14f
            empty.setPadding(0, 16, 0, 16)
            accountsContainer.addView(empty)
            return
        }

        allAccounts.forEachIndexed { index, account ->


            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setBackgroundColor(Color.parseColor("#1E293B"))
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, 10)
            card.layoutParams = cardParams
            card.setPadding(20, 16, 20, 16)


            val accent = View(this)
            val accentParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4
            )
            accentParams.setMargins(0, 0, 0, 12)
            accent.layoutParams = accentParams
            accent.setBackgroundColor(barColors[index % barColors.size])
            card.addView(accent)

            // Account name
            val tvName = TextView(this)
            tvName.text = account.name
            tvName.setTextColor(Color.WHITE)
            tvName.textSize = 16f
            tvName.setPadding(0, 0, 0, 4)
            card.addView(tvName)

            // Balance amount
            val tvBalance = TextView(this)
            tvBalance.text = "R%.2f".format(account.balance)
            tvBalance.setTextColor(Color.parseColor("#22C55E"))
            tvBalance.textSize = 22f
            val boldFont = android.graphics.Typeface.DEFAULT_BOLD
            tvBalance.typeface = boldFont
            tvBalance.setPadding(0, 0, 0, 12)
            card.addView(tvBalance)


            val divider = View(this)
            val divParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            divParams.setMargins(0, 0, 0, 10)
            divider.layoutParams = divParams
            divider.setBackgroundColor(Color.parseColor("#334155"))
            card.addView(divider)

            // Delete button
            val btnRow = LinearLayout(this)
            btnRow.orientation = LinearLayout.HORIZONTAL
            btnRow.gravity = android.view.Gravity.END

            val delete = Button(this)
            delete.text = "Delete"
            delete.setTextColor(Color.parseColor("#EF4444"))
            delete.setBackgroundColor(Color.TRANSPARENT)
            delete.textSize = 13f
            delete.setOnClickListener { deleteAccount(account) }

            btnRow.addView(delete)
            card.addView(btnRow)

            accountsContainer.addView(card)
        }
    }


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