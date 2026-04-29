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
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.view.animation.DecelerateInterpolator
import kotlin.jvm.java

class HomePage : AppCompatActivity() {

    private var username: String? = null

    // ✅ FIX: class-level references for card visibility
    private lateinit var cardBudget: View
    private lateinit var cardSpent: View
    private lateinit var cardExpenses: View
    private lateinit var cardQuick: View
    private lateinit var cardRecent: View
    private lateinit var rewardsCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)



        // ✅ FIX: initialize cards properly
        cardBudget = findViewById(R.id.cardBudget)
        cardSpent = findViewById(R.id.cardSpent)
        cardExpenses = findViewById(R.id.cardExpenses)
        cardQuick = findViewById(R.id.cardQuickActions)
        cardRecent = findViewById(R.id.cardRecent)
        rewardsCard = findViewById(R.id.rewardsCard)

        updateBudgie(12)
        setupBottomNav()
        setupTopButtons()
        setupQuickActions()
        loadRecentExpenses()
        loadDashboard()


        findViewById<View>(R.id.btnSetBudget).setOnClickListener {
            showBudgetDialog()
        }

        findViewById<View>(R.id.btnViewRewards).setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }

        findViewById<View>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
    }
    private fun getUsername(): String {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("username", "User") ?: "User"
    }
    // ---------------- BUDGIE IMAGE ----------------
    private fun updateBudgie(progress: Int) {
        val img = findViewById<ImageView>(R.id.imgBudgie)

        when {
            progress < 50 -> img.setImageResource(R.drawable.budgie_happy)
            progress < 80 -> img.setImageResource(R.drawable.budgie_ok)
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
                    loadDashboard()
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

    // ---------------- DASHBOARD ----------------
    private fun loadDashboard() {

        val minText = findViewById<TextView>(R.id.txtMin)
        val maxText = findViewById<TextView>(R.id.txtMax)
        val remainingText = findViewById<TextView>(R.id.txtRemaining)
        val percentText = findViewById<TextView>(R.id.txtPercentage)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        val totalSpentText = findViewById<TextView>(R.id.txtTotalSpent)
        val expenseCountText = findViewById<TextView>(R.id.txtExpenseCount)
        val seedsText = findViewById<TextView>(R.id.txtSeeds)
        val levelText = findViewById<TextView>(R.id.txtLevel)
        val progressSeeds = findViewById<android.widget.ProgressBar>(R.id.progressSeeds)

        val prefs = getSharedPreferences("dashboard_prefs", MODE_PRIVATE)


        val showBudget = prefs.getBoolean("show_budget", true)
        val showSpent = prefs.getBoolean("show_spent", true)
        val showExpenses = prefs.getBoolean("show_expenses", true)
        val showQuick = prefs.getBoolean("show_quick", true)
        val showRecent = prefs.getBoolean("show_recent", true)
        val showRewards = prefs.getBoolean("show_rewards", true)


        cardBudget.visibility = if (showBudget) View.VISIBLE else View.GONE
        cardSpent.visibility = if (showSpent) View.VISIBLE else View.GONE
        cardExpenses.visibility = if (showExpenses) View.VISIBLE else View.GONE
        cardQuick.visibility = if (showQuick) View.VISIBLE else View.GONE
        cardRecent.visibility = if (showRecent) View.VISIBLE else View.GONE
        rewardsCard.visibility = if (showRewards) View.VISIBLE else View.GONE

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {


            val greeting = findViewById<TextView>(R.id.txtGreeting)
            greeting.text = "Hello, ${getUsername()}"

            val budget = db.budgetDao().getBudget()
            val expenses = db.expenseDao().getAllExpenses()

            if (budget == null) return@launch

            val max = budget.maxAmount
            val min = budget.minAmount

            val totalSpent = expenses.sumOf { it.amount }
            val count = expenses.size
            val remaining = max - totalSpent

            val percentUsed = if (max > 0) {
                ((totalSpent / max) * 100).toInt()
            } else 0

            var seeds = 0
            seeds += count * 5
            if (percentUsed < 80) seeds += 20
            if (percentUsed < 50) seeds += 30

            val level = (seeds / 100) + 1
            val progress = seeds % 100

            val gamePrefs = getSharedPreferences("game", MODE_PRIVATE)
            gamePrefs.edit().putInt("seeds", seeds).apply()

            seedsText.text = "Seeds: $seeds 🌱"
            levelText.text = "Level $level"
            progressSeeds.progress = progress

            val color = when {
                percentUsed < 50 -> android.graphics.Color.parseColor("#22C55E")
                percentUsed < 80 -> android.graphics.Color.parseColor("#F59E0B")
                else -> android.graphics.Color.parseColor("#EF4444")
            }

            minText.text = "R%.2f min".format(min)
            maxText.text = "R%.2f max".format(max)
            remainingText.text = "R%.2f".format(remaining)
            percentText.text = "$percentUsed% used"

            progressBar.progressTintList = ColorStateList.valueOf(color)

            ObjectAnimator.ofInt(progressBar, "progress", 0, percentUsed).apply {
                duration = 900
                interpolator = DecelerateInterpolator()
                start()
            }

            totalSpentText.text = "R%.2f".format(totalSpent)
            expenseCountText.text = count.toString()

            updateBudgie(percentUsed)
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

        loadDashboard()
    }
}