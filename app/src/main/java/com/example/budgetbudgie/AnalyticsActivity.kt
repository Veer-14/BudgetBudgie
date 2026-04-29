package com.example.budgetbudgie

import Data.database.AppDatabase
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private lateinit var tvTotalBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvBudgetCount: TextView
    private lateinit var tvExpenseCount: TextView

    private lateinit var pieChart: PieChart
    private lateinit var categoryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        db = AppDatabase.getDatabase(this)

        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvBudgetCount = findViewById(R.id.tvBudgetCount)
        tvExpenseCount = findViewById(R.id.tvExpenseCount)

        pieChart = findViewById(R.id.pieChart)
        categoryContainer = findViewById(R.id.categoryContainer)

        loadAnalytics()
        setupBottomNav()
    }

    private fun loadAnalytics() {
        lifecycleScope.launch {


            val totalSpent = db.expenseDao().getTotalExpenses() ?: 0.0
            val expenseCount = db.expenseDao().getExpenseCount()
            val categoryTotals = db.expenseDao().getAllCategoryTotals()


            val totalBudget = db.sharedBudgetDao().getTotalBudget() ?: 0.0
            val budgetCount = db.sharedBudgetDao().getBudgetCount()

            val remaining = totalBudget - totalSpent

            val pieEntries = ArrayList<PieEntry>()

            categoryTotals.forEach {
                pieEntries.add(PieEntry(it.total.toFloat(), it.category))
            }

            runOnUiThread {

                // TEXT VALUES
                tvTotalSpent.text = "R%.2f".format(totalSpent)
                tvTotalBudget.text = "Total Budget: R%.2f".format(totalBudget)
                tvRemaining.text = "Remaining: R%.2f".format(remaining)
                tvBudgetCount.text = "Budgets: $budgetCount"
                tvExpenseCount.text = "Expenses: $expenseCount"

                // PIE CHART
                val dataSet = PieDataSet(pieEntries, "")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueTextSize = 12f

                val data = PieData(dataSet)

                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.centerText = "Spending"
                pieChart.setCenterTextColor(Color.WHITE)
                pieChart.setHoleColor(Color.parseColor("#1E293B"))
                pieChart.legend.textColor = Color.WHITE
                pieChart.animateY(1000)
                pieChart.invalidate()

                // CATEGORY LIST
                categoryContainer.removeAllViews()

                val colors = ColorTemplate.MATERIAL_COLORS

                categoryTotals.forEachIndexed { index, item ->

                    val percent =
                        if (totalSpent > 0) ((item.total / totalSpent) * 100).toInt() else 0

                    val row = createCategoryRow(
                        item.category,
                        item.total,
                        percent,
                        colors[index % colors.size]
                    )

                    categoryContainer.addView(row)
                }
            }
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

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.analytics -> true
                R.id.home -> {
                    startActivity(Intent(this, HomePage::class.java)); true
                }
                R.id.balance -> {
                    startActivity(Intent(this, BalancesActivity::class.java)); true
                }
                R.id.expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java)); true
                }
                R.id.shared -> {
                    startActivity(Intent(this, SharedBudgetActivity::class.java)); true
                }
                else -> false
            }
        }
    }
}