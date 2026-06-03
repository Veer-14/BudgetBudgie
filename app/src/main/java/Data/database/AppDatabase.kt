package Data.database

import Data.dao.AccountDao
import Data.dao.BudgetDao
import Data.dao.UserDao
import android.content.Context
import com.example.budgetbudgie.data.Account
import com.example.budgetbudgie.data.Expense
import com.example.budgetbudgie.data.User
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.budgetbudgie.data.Budget

@Database(
    entities = [
        User::class,
        Expense::class,
        Account::class,
        Budget::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun UserDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

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