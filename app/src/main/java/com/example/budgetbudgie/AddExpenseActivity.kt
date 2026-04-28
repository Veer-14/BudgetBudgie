package com.example.budgetbudgie

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.R
import com.example.budgetbudgie.data.Expense
import Data.database.AppDatabase
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Input fields
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)

        // Buttons
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Add Expense Button
        btnAdd.setOnClickListener {

            val category = etCategory.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val date = etDate.text.toString().trim()

            // Basic validation (prevents empty data)
            if (category.isEmpty() || description.isEmpty() || date.isEmpty()) {
                etDescription.error = "Please fill all fields"
                return@setOnClickListener
            }

            val expense = Expense(
                category = category,
                amount = amount,
                date = date,
                description = description
            )

            lifecycleScope.launch {
                db.expenseDao().insertExpense(expense)
                finish() // Return to Expenses screen
            }
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            finish()
        }
    }
}