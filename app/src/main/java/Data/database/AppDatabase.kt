package Data.database

import Data.dao.ExpenseDao

import Data.dao.UserDao

import android.content.Context
import com.example.budgetbudgie.data.Expense

import com.example.budgetbudgie.data.User
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase



@Database(

    entities = [User::class, Expense::class],
    version = 1,
    exportSchema = false

)
abstract class AppDatabase : RoomDatabase(){

    abstract fun UserDao(): UserDao
    abstract fun expenseDao(): ExpenseDao


    companion object{

        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase{

            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tracker_database"

                ).build()
                INSTANCE = instance
                instance
            }
        }


    }

}

