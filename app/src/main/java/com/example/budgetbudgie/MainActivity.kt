package com.example.budgetbudgie
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import Data.database.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var tvError: TextView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)


        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        tvError = findViewById(R.id.tvError)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                tvError.setText(R.string.error_fill_fields)
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.UserDao().loginUser(username, password)
                runOnUiThread {
                    if (user != null) {
                        tvError.visibility = View.GONE

                        Toast.makeText(
                            this@MainActivity,
                            "Welcome, ${user.username}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@MainActivity, HomePage::class.java)
                        intent.putExtra("username", user.username) // ✅ SEND IT HERE
                        startActivity(intent)

                        finish()

                    } else {
                        tvError.setText(R.string.error_invalid_login)
                        tvError.visibility = View.VISIBLE
                    }
                }
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}