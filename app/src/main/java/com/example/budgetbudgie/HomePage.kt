package com.example.budgetbudgie

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        updateBudgie(12) // your current percentage
    }


    private fun updateBudgie(progress: Int) {
        val img = findViewById<ImageView>(R.id.imgBudgie)

        when {
            progress < 30 -> img.setImageResource(R.drawable.budgie_happy)
            progress < 70 -> img.setImageResource(R.drawable.budgie_ok)
            else -> img.setImageResource(R.drawable.budgie_sad)
        }
    }

}