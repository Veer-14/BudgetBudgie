package Data.database

import Data.dao.AccountDao
import Data.dao.ExpenseDao
import Data.dao.UserDao
import Data.dao.SharedBudgetDao
import android.content.Context
import com.example.budgetbudgie.data.Account
import com.example.budgetbudgie.data.Expense
import com.example.budgetbudgie.data.User
import com.example.budgetbudgie.data.SharedBudget
import com.example.budgetbudgie.data.SharedExpense
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Expense::class,
        Account::class,
        SharedBudget::class,
        SharedExpense::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun UserDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao
    abstract fun sharedBudgetDao(): SharedBudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}