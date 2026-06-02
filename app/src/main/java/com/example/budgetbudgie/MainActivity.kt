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

   //variables
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

                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    prefs.edit().putString("username", email).apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Welcome, $email!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@MainActivity, HomePage::class.java))
                    finish()
                }
                .addOnFailureListener {
                   // if login fails
                    tvError.setText(R.string.error_invalid_login)
                    tvError.visibility = View.VISIBLE
                }
        }

       //directs user to register screen
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}