package com.example.budgetbudgie

import Data.database.AppDatabase
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
    }

    private fun loadAnalytics() {
        lifecycleScope.launch {
            val budgets = db.sharedBudgetDao().getAllBudgets()

            var totalBudget = 0.0
            var totalSpent = 0.0
            var expenseCount = 0

            val pieEntries = ArrayList<PieEntry>()
            val categoryData = mutableListOf<Pair<String, Double>>()

            budgets.forEach { budget ->
                totalBudget += budget.totalBudget

                val expenses = db.sharedBudgetDao().getExpensesForBudget(budget.id)
                var budgetSpent = 0.0

                expenses.forEach { exp ->
                    totalSpent += exp.amount
                    budgetSpent += exp.amount
                    expenseCount++
                }

                if (budgetSpent > 0) {
                    pieEntries.add(PieEntry(budgetSpent.toFloat(), budget.name))
                    categoryData.add(Pair(budget.name, budgetSpent))
                }
            }

            val remaining = totalBudget - totalSpent

            runOnUiThread {
                tvTotalBudget.text = "Total Budget: R$totalBudget"
                tvTotalSpent.text = "R$totalSpent"
                tvRemaining.text = "Remaining: R$remaining"
                tvBudgetCount.text = "Budgets: ${budgets.size}"
                tvExpenseCount.text = "Expenses: $expenseCount"

                // PIE CHART
                val dataSet = PieDataSet(pieEntries, "")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                dataSet.valueTextSize = 13f
                dataSet.valueTextColor = android.graphics.Color.WHITE

                val data = PieData(dataSet)
                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.centerText = "Spending"
                pieChart.setCenterTextColor(android.graphics.Color.WHITE)
                pieChart.setCenterTextSize(14f)
                pieChart.setHoleColor(android.graphics.Color.parseColor("#1E293B"))
                pieChart.legend.textColor = android.graphics.Color.WHITE
                pieChart.animateY(1000)
                pieChart.invalidate()

                // CATEGORY ROWS
                categoryContainer.removeAllViews()
                val colors = ColorTemplate.MATERIAL_COLORS
                categoryData.forEachIndexed { index, (name, amount) ->
                    val percent = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0
                    val row = createCategoryRow(name, amount, percent, colors[index % colors.size])
                    categoryContainer.addView(row)
                }
            }
        }
    }

    private fun createCategoryRow(name: String, amount: Double, percent: Int, color: Int): LinearLayout {
        val dp = resources.displayMetrics.density

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = android.view.Gravity.CENTER_VERTICAL
        val rowParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rowParams.bottomMargin = (8 * dp).toInt()
        row.layoutParams = rowParams
        row.setPadding(0, (8 * dp).toInt(), 0, (8 * dp).toInt())

        // Color dot
        val dot = TextView(this)
        dot.text = "●"
        dot.textSize = 18f
        dot.setTextColor(color)
        val dotParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dotParams.marginEnd = (12 * dp).toInt()
        dot.layoutParams = dotParams

        // Name + percent column
        val nameCol = LinearLayout(this)
        nameCol.orientation = LinearLayout.VERTICAL
        val nameColParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        nameCol.layoutParams = nameColParams

        val tvName = TextView(this)
        tvName.text = name
        tvName.textSize = 14f
        tvName.setTextColor(android.graphics.Color.WHITE)
        tvName.setTypeface(null, android.graphics.Typeface.BOLD)

        val tvPercent = TextView(this)
        tvPercent.text = "$percent%"
        tvPercent.textSize = 12f
        tvPercent.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))

        nameCol.addView(tvName)
        nameCol.addView(tvPercent)

        // Amount
        val tvAmount = TextView(this)
        tvAmount.text = "R$amount"
        tvAmount.textSize = 14f
        tvAmount.setTextColor(android.graphics.Color.WHITE)
        tvAmount.setTypeface(null, android.graphics.Typeface.BOLD)

        row.addView(dot)
        row.addView(nameCol)
        row.addView(tvAmount)

        return row
    }
}