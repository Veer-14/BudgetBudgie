package com.example.budgetbudgie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to HomePage
        startActivity(Intent(this, HomePage::class.java))
        finish()
    }
}