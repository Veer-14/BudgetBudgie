package com.example.budgetbudgie

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class DashboardCustomizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_customization)

        val prefs = getSharedPreferences("dashboard_prefs", MODE_PRIVATE)

        val checkBudget = findViewById<CheckBox>(R.id.checkBudget)
        val checkSpent = findViewById<CheckBox>(R.id.checkSpent)
        val checkExpenses = findViewById<CheckBox>(R.id.checkExpenses)
        val checkQuick = findViewById<CheckBox>(R.id.checkQuick)
        val checkRecent = findViewById<CheckBox>(R.id.checkRecent)
        val checkRewards = findViewById<CheckBox>(R.id.checkRewards)

        val btnSave = findViewById<Button>(R.id.btnSave)

        // LOAD saved values
        checkBudget.isChecked = prefs.getBoolean("show_budget", true)
        checkSpent.isChecked = prefs.getBoolean("show_spent", true)
        checkExpenses.isChecked = prefs.getBoolean("show_expenses", true)
        checkQuick.isChecked = prefs.getBoolean("show_quick", true)
        checkRecent.isChecked = prefs.getBoolean("show_recent", true)
        checkRewards.isChecked = prefs.getBoolean("show_rewards", true)

        // SAVE values
        btnSave.setOnClickListener {
            val editor = prefs.edit()

            editor.putBoolean("show_budget", checkBudget.isChecked)
            editor.putBoolean("show_spent", checkSpent.isChecked)
            editor.putBoolean("show_expenses", checkExpenses.isChecked)
            editor.putBoolean("show_quick", checkQuick.isChecked)
            editor.putBoolean("show_recent", checkRecent.isChecked)
            editor.putBoolean("show_rewards", checkRewards.isChecked)

            editor.apply()
            finish()
        }
    }
}