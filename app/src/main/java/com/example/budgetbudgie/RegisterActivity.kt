package com.example.budgetbudgie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var etRegName: EditText
    private lateinit var etRegEmail: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var tvRegError: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etRegName = findViewById(R.id.etRegName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
        tvRegError = findViewById(R.id.tvRegError)

        btnRegister.setOnClickListener {
            val name = etRegName.text.toString().trim()
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString().trim()


            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvRegError.setText(R.string.error_fill_fields)
                tvRegError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvRegError.setText(R.string.error_password_short)
                tvRegError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val profileUpdate = userProfileChangeRequest {
                        displayName = name
                    }
                    auth.currentUser?.updateProfile(profileUpdate)
                        ?.addOnSuccessListener {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Account created successfully! Please log in.",
                                Toast.LENGTH_LONG
                            ).show()
                            auth.signOut()
                            finish()
                        }
                }
                .addOnFailureListener {
                    tvRegError.text = it.message ?: "Registration failed"
                    tvRegError.visibility = View.VISIBLE
                }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}