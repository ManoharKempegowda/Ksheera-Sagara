package com.example.dairyfarm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dairyfarm.data.entities.Cow
import com.example.dairyfarm.data.entities.ExpenseEntry
import com.example.dairyfarm.data.entities.IncomeEntry
import com.example.dairyfarm.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface DairyDao {
    // Cows
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCow(cow: Cow)

    @Query("SELECT * FROM cows WHERE isActive = 1 AND username = :username")
    fun getActiveCows(username: String): Flow<List<Cow>>

    // Income
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntry)

    @Query("SELECT * FROM income_entries WHERE username = :username ORDER BY date DESC")
    fun getAllIncome(username: String): Flow<List<IncomeEntry>>

    @Query("SELECT SUM(totalAmount) FROM income_entries WHERE username = :username")
    fun getTotalIncomeAmount(username: String): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM income_entries WHERE username = :username AND date >= :startDate AND date <= :endDate")
    fun getIncomeAmountBetween(username: String, startDate: Long, endDate: Long): Flow<Double?>

    // Expense
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntry)

    @Query("SELECT * FROM expense_entries WHERE username = :username ORDER BY date DESC")
    fun getAllExpenses(username: String): Flow<List<ExpenseEntry>>

    @Query("SELECT SUM(amount) FROM expense_entries WHERE username = :username")
    fun getTotalExpenseAmount(username: String): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM expense_entries WHERE username = :username AND date >= :startDate AND date <= :endDate")
    fun getExpenseAmountBetween(username: String, startDate: Long, endDate: Long): Flow<Double?>

    // Cow-wise Analysis
    @Query("SELECT SUM(totalAmount) FROM income_entries WHERE username = :username AND cowId = :cowId")
    fun getIncomeForCow(username: String, cowId: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM expense_entries WHERE username = :username AND cowId = :cowId")
    fun getExpenseForCow(username: String, cowId: Long): Flow<Double?>

    // Users
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}
