package com.example.budgetbudgie

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var tvError: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect to Firebase Authentication
        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        tvError = findViewById(R.id.tvError)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.setText(R.string.error_fill_fields)
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    tvError.visibility = View.GONE



                    auth.currentUser?.reload()?.addOnCompleteListener {
                        val displayName = auth.currentUser?.displayName
                        val nameToShow = if (!displayName.isNullOrEmpty()) displayName else email
                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        prefs.edit().putString("username", nameToShow).apply()
                    }
                    val displayName = auth.currentUser?.displayName
                    val nameToShow = if (!displayName.isNullOrEmpty()) displayName else email

                    // Save name to shared preferences
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    prefs.edit().putString("username", nameToShow).apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Welcome, $nameToShow!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@MainActivity, HomePage::class.java))
                    finish()
                }
                .addOnFailureListener {
                    // If login fails show error
                    tvError.setText(R.string.error_invalid_login)
                    tvError.visibility = View.VISIBLE
                }
        }

        // Directs user to register screen
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}