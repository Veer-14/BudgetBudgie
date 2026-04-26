package Data.database

import Data.dao.AccountDao
import Data.dao.ExpenseDao
import Data.dao.UserDao
import android.content.Context
import com.example.budgetbudgie.data.Account
import com.example.budgetbudgie.data.Expense
import com.example.budgetbudgie.data.User
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Expense::class, Account::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun UserDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao

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