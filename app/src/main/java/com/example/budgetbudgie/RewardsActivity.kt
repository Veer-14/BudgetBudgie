package com.example.budgetbudgie

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RewardsActivity : AppCompatActivity() {

    private lateinit var txtLevel: TextView
    private lateinit var txtSeeds: TextView
    private lateinit var badgeContainer: LinearLayout
    private lateinit var unlockedContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        txtLevel = findViewById(R.id.txtLevelRewards)
        txtSeeds = findViewById(R.id.txtSeedsRewards)
        badgeContainer = findViewById(R.id.badgeContainer)
        unlockedContainer = findViewById(R.id.unlockedContainer)
    }

    override fun onResume() {
        super.onResume()
        loadRewards() // 🔥 refresh every time screen opens
    }

    private fun loadRewards() {
        val prefs = getSharedPreferences("game", MODE_PRIVATE)
        val seeds = prefs.getInt("seeds", 0)
        val level = (seeds / 100) + 1

        txtLevel.text = "Level $level"
        txtSeeds.text = "Seeds: $seeds 🌱"

        // 🔥 Clear old views before re-adding
        badgeContainer.removeAllViews()
        unlockedContainer.removeAllViews()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // closes activity and returns to HomePage
        }

        addReward(badgeContainer, unlockedContainer, "🎉 First Expense", "Log 1 expense", seeds >= 5)
        addReward(badgeContainer, unlockedContainer, "💰 Budget Keeper", "Reach 50 seeds", seeds >= 50)
        addReward(badgeContainer, unlockedContainer, "🔥 Saving Master", "Reach 150 seeds", seeds >= 150)
        addReward(badgeContainer, unlockedContainer, "👑 Budgie King", "Reach 300 seeds", seeds >= 300)
    }

    private fun addReward(
        allContainer: LinearLayout,
        unlockedContainer: LinearLayout,
        title: String,
        requirement: String,
        unlocked: Boolean
    ) {
        val badge = TextView(this)

        badge.text = if (unlocked) {
            "✅ $title"
        } else {
            "🔒 $title\n   → $requirement"
        }

        badge.textSize = 16f
        badge.setPadding(0, 12, 0, 12)

        badge.setTextColor(
            if (unlocked)
                android.graphics.Color.WHITE
            else
                android.graphics.Color.GRAY
        )

        allContainer.addView(badge)

        if (unlocked) {
            val unlockedBadge = TextView(this)
            unlockedBadge.text = "🏆 $title"
            unlockedBadge.textSize = 16f
            unlockedBadge.setPadding(0, 10, 0, 10)
            unlockedBadge.setTextColor(android.graphics.Color.parseColor("#22C55E"))

            unlockedContainer.addView(unlockedBadge)
        }
    }
}