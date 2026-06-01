package com.example.budgetbudgie

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgie.data.Expense
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.database.*

class ExpensesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var tvTotal: TextView
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnFilter: Button
    private lateinit var tvCategoryTotals: TextView

    private val expenseRef =
        FirebaseDatabase.getInstance().getReference("expenses")

    private val dbRef =
        FirebaseDatabase.getInstance().getReference("expenses")

    private var allExpenses = mutableListOf<Expense>()

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

        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnFilter = findViewById(R.id.btnFilter)

        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }

        btnFilter.setOnClickListener {
            val start = etStartDate.text.toString()
            val end = etEndDate.text.toString()

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Select both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            applyFilter(start, end)
        }

        loadExpenses()
    }

    // ================= FIREBASE LOAD =================
    private fun loadExpenses() {

        expenseRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val expenses = mutableListOf<Expense>()

                for (child in snapshot.children) {
                    val exp = child.getValue(Expense::class.java)
                    if (exp != null) expenses.add(exp)
                }

                updateUI(expenses)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ================= FILTER (LOCAL) =================
    private fun applyFilter(start: String, end: String) {

        val filtered = allExpenses.filter {
            it.date >= start && it.date <= end
        }

        updateUI(filtered)
    }

    // ================= UI UPDATE =================
    private fun updateUI(expenses: List<Expense>) {

        adapter.updateData(expenses)

        tvTotal.text = "R%.2f".format(expenses.sumOf { it.amount })

        val grouped = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }

        val builder = StringBuilder()

        grouped.forEach {
            builder.append("${it.key}: R%.2f\n".format(it.value))
        }

        tvCategoryTotals.text = builder.toString()
    }

    // ================= BOTTOM NAV =================
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

    // ================= DATE PICKER =================
    private fun showDatePicker(target: EditText) {

        val calendar = Calendar.getInstance()

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, day ->

                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)

                target.setText("$year-$formattedMonth-$formattedDay")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}