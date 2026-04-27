package com.example.budgetbudgie
import android.widget.Toast
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import Data.database.AppDatabase
import com.example.budgetbudgie.data.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etRegUsername: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var tvRegError: TextView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = AppDatabase.getDatabase(this)

        etRegUsername = findViewById(R.id.etRegUsername)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
        tvRegError = findViewById(R.id.tvRegError)

        btnRegister.setOnClickListener {
            val username = etRegUsername.text.toString().trim()
            val password = etRegPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                tvRegError.setText(R.string.error_fill_fields)
                tvRegError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvRegError.setText(R.string.error_password_short)
                tvRegError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existing = db.UserDao().getUserByUsername(username)
                runOnUiThread {
                    if (existing != null) {
                        tvRegError.setText(R.string.error_username_taken)
                        tvRegError.visibility = View.VISIBLE
                    } else {
                        lifecycleScope.launch {
                            db.UserDao().insertUser(
                                User(username = username, password = password)
                            )
                            runOnUiThread {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Account created successfully! Please log in.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}