package com.example.budgetbudgie

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgie.R
import com.example.budgetbudgie.data.Expense
import java.io.File

class ExpenseAdapter(
    private var expenses: List<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.tvDescription)
        val amount: TextView = view.findViewById(R.id.tvAmount)
        val category: TextView = view.findViewById(R.id.tvCategory)
        val date: TextView = view.findViewById(R.id.tvDate)
        val image: ImageView = view.findViewById(R.id.imgExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.description.text = expense.description
        holder.amount.text = "R%.2f".format(expense.amount)
        holder.category.text = "Category: ${expense.category}"
        holder.date.text = "Date: ${expense.date}"

        // 🔥 IMPORTANT: reset first (prevents wrong images)
        holder.image.setImageDrawable(null)
        holder.image.visibility = View.GONE

        if (!expense.imageUri.isNullOrEmpty()) {
            holder.image.visibility = View.VISIBLE
            expense.imageUri?.let {
                val file = File(it)
                if (file.exists()) {
                    holder.image.setImageURI(Uri.fromFile(file))
                    holder.image.visibility = View.VISIBLE
                } else {
                    holder.image.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}