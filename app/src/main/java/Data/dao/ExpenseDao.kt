package Data.dao

import androidx.annotation.DeprecatedSinceApi
import androidx.core.view.WindowInsetsCompat
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.budgetbudgie.data.CategoryTotal
import com.example.budgetbudgie.data.Expense

@Dao
interface ExpenseDao{

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseBetweenDates(startDate: String, endDate: String): List<Expense>

    @Query("""
    SELECT category, SUM(amount) as total 
    FROM expenses 
    WHERE date BETWEEN :startDate AND :endDate 
    GROUP BY category
""")
    suspend fun getCategoryTotals(startDate: String, endDate: String): List<CategoryTotal>


}


