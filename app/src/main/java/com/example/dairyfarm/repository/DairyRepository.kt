package com.example.dairyfarm.repository

import com.example.dairyfarm.data.dao.DairyDao
import com.example.dairyfarm.data.entities.Cow
import com.example.dairyfarm.data.entities.ExpenseEntry
import com.example.dairyfarm.data.entities.IncomeEntry
import kotlinx.coroutines.flow.Flow

class DairyRepository(private val dairyDao: DairyDao) {

    fun getActiveCows(username: String): Flow<List<Cow>> = dairyDao.getActiveCows(username)
    fun getAllIncome(username: String): Flow<List<IncomeEntry>> = dairyDao.getAllIncome(username)
    fun getAllExpenses(username: String): Flow<List<ExpenseEntry>> = dairyDao.getAllExpenses(username)

    fun getTotalIncome(username: String): Flow<Double?> = dairyDao.getTotalIncomeAmount(username)
    fun getTotalExpense(username: String): Flow<Double?> = dairyDao.getTotalExpenseAmount(username)

    suspend fun insertCow(cow: Cow) {
        dairyDao.insertCow(cow)
    }

    suspend fun insertIncome(income: IncomeEntry) {
        dairyDao.insertIncome(income)
    }

    suspend fun insertExpense(expense: ExpenseEntry) {
        dairyDao.insertExpense(expense)
    }

    fun getIncomeForCow(username: String, cowId: Long): Flow<Double?> = dairyDao.getIncomeForCow(username, cowId)
    
    fun getExpenseForCow(username: String, cowId: Long): Flow<Double?> = dairyDao.getExpenseForCow(username, cowId)
}
