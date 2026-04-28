package com.example.budgetbudgie

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        updateBudgie(12)
        setupBottomNav()
    }

    private fun updateBudgie(progress: Int) {
        val img = findViewById<ImageView>(R.id.imgBudgie)
        when {
            progress < 30 -> img.setImageResource(R.drawable.budgie_happy)
            progress < 70 -> img.setImageResource(R.drawable.budgie_ok)
            else -> img.setImageResource(R.drawable.budgie_sad)
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true
                R.id.balance -> {
                    startActivity(Intent(this, BalancesActivity::class.java))
                    finish()
                    true
                }
                R.id.expenses -> {
                    // startActivity(Intent(this, ExpensesActivity::class.java))
                    // finish()
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
    }
