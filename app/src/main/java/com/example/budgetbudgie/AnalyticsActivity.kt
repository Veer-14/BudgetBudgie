package com.example.budgetbudgie

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.example.budgetbudgie.data.Expense
import java.util.Calendar

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tvTotalBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvBudgetCount: TextView
    private lateinit var tvExpenseCount: TextView

    private lateinit var pieChart: PieChart
    private lateinit var categoryContainer: LinearLayout

    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnFilter: Button

    private var allExpenses = mutableListOf<Expense>()

    private val expenseRef =
        FirebaseDatabase.getInstance().getReference("expenses")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvBudgetCount = findViewById(R.id.tvBudgetCount)
        tvExpenseCount = findViewById(R.id.tvExpenseCount)

        pieChart = findViewById(R.id.pieChart)
        categoryContainer = findViewById(R.id.categoryContainer)

        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnFilter = findViewById(R.id.btnFilter)

        etStartDate.setOnClickListener {
            showDatePicker(etStartDate)
        }

        etEndDate.setOnClickListener {
            showDatePicker(etEndDate)
        }

        btnFilter.setOnClickListener {

            val start = etStartDate.text.toString()
            val end = etEndDate.text.toString()

            if (start.isEmpty() || end.isEmpty())
                return@setOnClickListener

            val filtered = allExpenses.filter {
                it.date >= start && it.date <= end
            }

            updateAnalytics(filtered)
        }

        loadAnalytics()
        setupBottomNav()
    }

    private fun loadAnalytics() {

        expenseRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val expenses = mutableListOf<Expense>()

                for (child in snapshot.children) {

                    val exp = child.getValue(Expense::class.java)

                    if (exp != null) {
                        expenses.add(exp)
                    }
                }

                allExpenses.clear()
                allExpenses.addAll(expenses)

                updateAnalytics(expenses)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateAnalytics(expenses: List<Expense>) {

        val totalSpent = expenses.sumOf { it.amount }
        val expenseCount = expenses.size

        val grouped = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }

        val totalBudget = 0.0
        val budgetCount = 0

        val remaining = totalBudget - totalSpent

        val pieEntries = ArrayList<PieEntry>()

        grouped.forEach {
            pieEntries.add(
                PieEntry(
                    it.value.toFloat(),
                    it.key
                )
            )
        }

        tvTotalSpent.text = "R%.2f".format(totalSpent)
        tvTotalBudget.text = "Total Budget: R%.2f".format(totalBudget)
        tvRemaining.text = "Remaining: R%.2f".format(remaining)
        tvExpenseCount.text = "Expenses: $expenseCount"
        tvBudgetCount.text = "Budgets: $budgetCount"

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors =
            com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.toList()

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.centerText = "Spending"
        pieChart.setHoleColor(Color.parseColor("#1E293B"))
        pieChart.legend.textColor = Color.WHITE
        pieChart.animateY(1000)
        pieChart.invalidate()

        categoryContainer.removeAllViews()

        grouped.forEach { (category, total) ->

            val percent =
                if (totalSpent > 0)
                    ((total / totalSpent) * 100).toInt()
                else
                    0

            categoryContainer.addView(
                createCategoryRow(
                    category,
                    total,
                    percent,
                    Color.WHITE
                )
            )
        }
    }

    private fun createCategoryRow(
        name: String,
        amount: Double,
        percent: Int,
        color: Int
    ): LinearLayout {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL

        val dot = TextView(this)
        dot.text = "● "
        dot.setTextColor(color)

        val label = TextView(this)
        label.text = "$name ($percent%)  "

        val value = TextView(this)
        value.text = "R%.2f".format(amount)

        row.addView(dot)
        row.addView(label)
        row.addView(value)

        return row
    }

    private fun showDatePicker(target: EditText) {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val formattedMonth =
                    String.format("%02d", month + 1)

                val formattedDay =
                    String.format("%02d", day)

                target.setText(
                    "$year-$formattedMonth-$formattedDay"
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupBottomNav() {

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.analytics

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.analytics -> true

                R.id.home -> {
                    startActivity(
                        Intent(this, HomePage::class.java)
                    )
                    true
                }

                R.id.balance -> {
                    startActivity(
                        Intent(this, BalancesActivity::class.java)
                    )
                    true
                }

                R.id.expenses -> {
                    startActivity(
                        Intent(this, ExpensesActivity::class.java)
                    )
                    true
                }

                R.id.shared -> {
                    startActivity(
                        Intent(this, SharedBudgetActivity::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }
}