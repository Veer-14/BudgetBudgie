package Data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.budgetbudgie.data.SharedBudget
import com.example.budgetbudgie.data.SharedExpense

@Dao
interface SharedBudgetDao {

    @Insert
    suspend fun insertBudget(budget: SharedBudget)

    @Insert
    suspend fun insertExpense(expense: SharedExpense)


    @Query("DELETE FROM shared_expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)

    @Query("SELECT * FROM shared_budgets")
    suspend fun getAllBudgets(): List<SharedBudget>

    @Query("SELECT * FROM shared_expenses WHERE budgetId = :budgetId")
    suspend fun getExpensesForBudget(budgetId: Int): List<SharedExpense>

    @Query("SELECT SUM(totalBudget) FROM shared_budgets")
    suspend fun getTotalBudget(): Double?

    @Query("SELECT SUM(amount) FROM shared_expenses")
    suspend fun getTotalSharedSpent(): Double?

    @Query("SELECT COUNT(*) FROM shared_budgets")
    suspend fun getBudgetCount(): Int

    @Query("SELECT COUNT(*) FROM shared_expenses")
    suspend fun getSharedExpenseCount(): Int
}