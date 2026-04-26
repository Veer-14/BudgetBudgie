package Data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.budgetbudgie.data.Account

@Dao
interface AccountDao {

    @Insert
    suspend fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountsForUser(userId: Int): List<Account>

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAllAccountsForUser(userId: Int): List<Account>

    @Delete
    suspend fun deleteAccount(account: Account)
}