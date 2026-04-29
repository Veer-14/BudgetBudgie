package com.example.budgetbudgie

import Data.database.AppDatabase
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.data.SharedBudget
import com.example.budgetbudgie.data.SharedExpense
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class SharedBudgetActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var container: LinearLayout
    private lateinit var btnAddBudget: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_budget)

        db = AppDatabase.getDatabase(this)

        container = findViewById(R.id.budgetContainer)
        btnAddBudget = findViewById(R.id.btnAddBudget)

        loadBudgets()

        btnAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.shared

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.shared -> true
                R.id.home -> {
                    startActivity(Intent(this, HomePage::class.java))
                    finish()
                    true
                }
                R.id.balance -> {
                    startActivity(Intent(this, BalancesActivity::class.java))
                    finish()
                    true
                }
                R.id.expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
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

    private fun loadBudgets() {
        lifecycleScope.launch {
            val budgets = db.sharedBudgetDao().getAllBudgets()

            runOnUiThread {
                container.removeAllViews()

                budgets.forEach { budget ->

                    val card = LayoutInflater.from(this@SharedBudgetActivity)
                        .inflate(R.layout.item_budget, container, false)

                    val tvTitle = card.findViewById<TextView>(R.id.tvBudgetName)
                    val tvAmount = card.findViewById<TextView>(R.id.tvBudgetAmount)
                    val tvSpent = card.findViewById<TextView>(R.id.tvSpent)
                    val tvRemaining = card.findViewById<TextView>(R.id.tvRemaining)
                    val progressBudget = card.findViewById<ProgressBar>(R.id.progressBudget)
                    val tvDetailsHeader = card.findViewById<TextView>(R.id.tvDetailsHeader)
                    val tvTotalBudgetDetail = card.findViewById<TextView>(R.id.tvTotalBudgetDetail)
                    val tvTotalSpentDetail = card.findViewById<TextView>(R.id.tvTotalSpentDetail)
                    val tvRemainingDetail = card.findViewById<TextView>(R.id.tvRemainingDetail)
                    val membersContainer = card.findViewById<LinearLayout>(R.id.membersContainer)
                    val btnAddExpense = card.findViewById<Button>(R.id.btnAddExpense)
                    val expenseContainer = card.findViewById<LinearLayout>(R.id.expenseContainer)

                    tvTitle.text = budget.name
                    tvAmount.text = "Total Budget: R${budget.totalBudget}"
                    tvDetailsHeader.text = "${budget.name} - Details"
                    tvTotalBudgetDetail.text = "R${budget.totalBudget}"

                    membersContainer.removeAllViews()
                    membersContainer.addView(createMemberRow("You", "Owner"))

                    lifecycleScope.launch {
                        val expenses = db.sharedBudgetDao().getExpensesForBudget(budget.id)

                        runOnUiThread {
                            expenseContainer.removeAllViews()
                            var totalSpent = 0.0

                            expenses.forEach { exp ->
                                totalSpent += exp.amount
                                expenseContainer.addView(createExpenseRow(exp))
                            }

                            val remaining = budget.totalBudget - totalSpent
                            tvSpent.text = "R$totalSpent"
                            tvRemaining.text = "R$remaining"
                            tvTotalSpentDetail.text = "R$totalSpent"
                            tvRemainingDetail.text = "R$remaining"

                            val progress = if (budget.totalBudget > 0)
                                ((totalSpent / budget.totalBudget) * 100).toInt() else 0
                            progressBudget.progress = progress
                        }
                    }

                    btnAddExpense.setOnClickListener {
                        showAddExpenseDialog(budget.id)
                    }

                    container.addView(card)
                }
            }
        }
    }

    private fun createMemberRow(name: String, role: String): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 8, 0, 8)

        val avatar = TextView(this)
        avatar.text = name.first().toString()
        avatar.textSize = 16f
        avatar.setTextColor(0xFFFFFFFF.toInt())
        avatar.gravity = android.view.Gravity.CENTER
        avatar.setBackgroundColor(0xFF6366F1.toInt())
        val avatarSize = (40 * resources.displayMetrics.density).toInt()
        val avatarParams = LinearLayout.LayoutParams(avatarSize, avatarSize)
        avatarParams.marginEnd = (12 * resources.displayMetrics.density).toInt()
        avatar.layoutParams = avatarParams

        val nameCol = LinearLayout(this)
        nameCol.orientation = LinearLayout.VERTICAL

        val tvName = TextView(this)
        tvName.text = name
        tvName.textSize = 14f
        tvName.setTextColor(0xFFFFFFFF.toInt())

        val tvRole = TextView(this)
        tvRole.text = role
        tvRole.textSize = 12f
        tvRole.setTextColor(0xFF9CA3AF.toInt())

        nameCol.addView(tvName)
        nameCol.addView(tvRole)
        row.addView(avatar)
        row.addView(nameCol)

        return row
    }

    private fun createExpenseRow(exp: SharedExpense): LinearLayout {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setBackgroundColor(0xFF1E293B.toInt())

        val dp8 = (8 * resources.displayMetrics.density).toInt()
        row.setPadding(dp8, dp8, dp8, dp8)

        val rowParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rowParams.bottomMargin = dp8
        row.layoutParams = rowParams

        // ICON
        val icon = TextView(this)
        icon.text = "📄"
        icon.textSize = 20f

        val iconParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        iconParams.marginEnd = dp8
        icon.layoutParams = iconParams

        // MIDDLE TEXT
        val middleCol = LinearLayout(this)
        middleCol.orientation = LinearLayout.VERTICAL
        middleCol.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tvDesc = TextView(this)
        tvDesc.text = exp.description
        tvDesc.textSize = 14f
        tvDesc.setTextColor(0xFFFFFFFF.toInt())
        tvDesc.setTypeface(null, android.graphics.Typeface.BOLD)

        val tvMeta = TextView(this)
        tvMeta.text = "Member"
        tvMeta.textSize = 12f
        tvMeta.setTextColor(0xFF9CA3AF.toInt())

        middleCol.addView(tvDesc)
        middleCol.addView(tvMeta)

        // AMOUNT
        val tvAmount = TextView(this)
        tvAmount.text = "R${exp.amount}"
        tvAmount.textSize = 14f
        tvAmount.setTextColor(0xFFFFFFFF.toInt())
        tvAmount.setTypeface(null, android.graphics.Typeface.BOLD)

        val amountParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        amountParams.marginEnd = dp8
        tvAmount.layoutParams = amountParams

        // DELETE BUTTON
        val tvDelete = TextView(this)
        tvDelete.text = "🗑"
        tvDelete.textSize = 18f

        tvDelete.setOnClickListener {

            lifecycleScope.launch {
                db.sharedBudgetDao().deleteExpenseById(exp.id)

                runOnUiThread {
                    Toast.makeText(
                        this@SharedBudgetActivity,
                        "Expense deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadBudgets() // refresh UI
                }
            }
        }

        // ADD VIEWS
        row.addView(icon)
        row.addView(middleCol)
        row.addView(tvAmount)
        row.addView(tvDelete)

        return row
    }

    private fun showAddBudgetDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_budget, null)
        val etName = view.findViewById<EditText>(R.id.tvBudgetName)
        val etAmount = view.findViewById<EditText>(R.id.tvBudgetAmount)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(view).create()

        view.findViewById<Button>(R.id.btnSaveBudget).setOnClickListener {
            val name = etName.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (name.isEmpty() || amount == null) return@setOnClickListener

            lifecycleScope.launch {
                db.sharedBudgetDao().insertBudget(SharedBudget(name = name, totalBudget = amount))
                runOnUiThread { dialog.dismiss(); loadBudgets() }
            }
        }
        dialog.show()
    }

    private fun showAddExpenseDialog(budgetId: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val etDesc = view.findViewById<EditText>(R.id.etExpenseDesc)
        val etAmount = view.findViewById<EditText>(R.id.etExpenseAmount)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(view).create()

        view.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val desc = etDesc.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (desc.isEmpty() || amount == null) return@setOnClickListener

            lifecycleScope.launch {
                db.sharedBudgetDao().insertExpense(
                    SharedExpense(budgetId = budgetId, description = desc, amount = amount)
                )
                runOnUiThread { dialog.dismiss(); loadBudgets() }
            }
        }
        dialog.show()
    }
}