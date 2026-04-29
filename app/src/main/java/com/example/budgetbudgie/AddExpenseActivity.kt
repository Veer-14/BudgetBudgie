package com.example.budgetbudgie

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgie.data.Expense
import Data.database.AppDatabase
import android.app.DatePickerDialog
import android.icu.util.Calendar
import kotlinx.coroutines.launch
import java.io.File

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        db = AppDatabase.getDatabase(this)

        // Inputs
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)

        // Image components
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)

        // Buttons
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Image picker
        val imagePicker = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {

                val savedPath = saveImageToInternalStorage(it)

                if (savedPath != null) {
                    selectedImageUri = savedPath
                    imgPreview.setImageURI(Uri.fromFile(File(savedPath)))
                }
            }
        }

        btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }
        btnAdd.setOnClickListener {

            val category = etCategory.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val date = etDate.text.toString().trim()

            if (category.isEmpty() || description.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                category = category,
                amount = amount,
                date = date,
                description = description,
                imageUri = selectedImageUri
            )

            lifecycleScope.launch {
                db.expenseDao().insertExpense(expense)
                finish()

            }
        }
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }
        btnCancel.setOnClickListener {
            finish()
        }
    }
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "expense_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = file.outputStream()

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun showDatePicker(target: EditText) {

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->

                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)

                val selectedDate = "$year-$formattedMonth-$formattedDay"
                target.setText(selectedDate)

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}