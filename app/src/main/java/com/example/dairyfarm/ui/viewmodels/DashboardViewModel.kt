package com.example.dairyfarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dairyfarm.data.AppDatabase
import com.example.dairyfarm.data.entities.Cow
import com.example.dairyfarm.data.entities.ExpenseEntry
import com.example.dairyfarm.data.entities.IncomeEntry
import com.example.dairyfarm.data.entities.User
import com.example.dairyfarm.repository.DairyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TreeMap
import java.util.Calendar

enum class TimeFilter {
    DAILY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

/** Holds aggregated income / expense / profit for a single calendar month. */
data class MonthlyTrendData(
    val month: String,      // e.g. "Jan 25"
    val income: Double,
    val expense: Double,
    val profit: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DairyRepository
    private val _currentUsername = MutableStateFlow<String?>(null)
    val currentUser = MutableStateFlow<User?>(null)
    
    val allExpenses: StateFlow<List<ExpenseEntry>>
    val totalIncome: StateFlow<Double>
    val totalExpense: StateFlow<Double>
    val netProfit: StateFlow<Double>
    val allIncome: StateFlow<List<IncomeEntry>>
    val monthlyTrend: StateFlow<List<MonthlyTrendData>>
    
    val timeFilter = MutableStateFlow(TimeFilter.ALL_TIME)
    
    fun setTimeFilter(filter: TimeFilter) {
        timeFilter.value = filter
    }

    init {
        val dairyDao = AppDatabase.getDatabase(application).dairyDao()
        repository = DairyRepository(dairyDao)
        
        val rawExpenses = _currentUsername.flatMapLatest { username ->
            repository.getAllExpenses(username ?: "")
        }

        val rawIncome = _currentUsername.flatMapLatest { username ->
            repository.getAllIncome(username ?: "")
        }

        allExpenses = combine(rawExpenses, timeFilter) { expenses, filter ->
            filterEntries(expenses, filter) { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allIncome = combine(rawIncome, timeFilter) { income, filter ->
            filterEntries(income, filter) { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
        totalIncome = allIncome.map { list -> list.sumOf { it.totalAmount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalExpense = allExpenses.map { list -> list.sumOf { it.amount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        
        netProfit = combine(totalIncome, totalExpense) { income, expense ->
            income - expense
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        // Build monthly trend by merging income + expense lists and grouping by month
        monthlyTrend = combine(rawIncome, rawExpenses) { incomeList: List<IncomeEntry>, expenseList: List<ExpenseEntry> ->
            val fmt = SimpleDateFormat("MMM yy", Locale.getDefault())
            val sortFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())

            val monthIncome  = TreeMap<String, Double>()
            val monthExpense = TreeMap<String, Double>()

            incomeList.forEach { entry ->
                val sortKey = sortFmt.format(Date(entry.date))
                monthIncome[sortKey] = (monthIncome[sortKey] ?: 0.0) + entry.totalAmount
            }
            expenseList.forEach { entry ->
                val sortKey = sortFmt.format(Date(entry.date))
                monthExpense[sortKey] = (monthExpense[sortKey] ?: 0.0) + entry.amount
            }

            // Union of all months, sorted chronologically, last 6 only
            val allKeys = (monthIncome.keys + monthExpense.keys).toSortedSet().toList().takeLast(6)

            allKeys.map { sortKey ->
                val displayLabel = fmt.format(sortFmt.parse(sortKey) ?: Date())
                val inc = monthIncome[sortKey] ?: 0.0
                val exp = monthExpense[sortKey] ?: 0.0
                MonthlyTrendData(
                    month   = displayLabel,
                    income  = inc,
                    expense = exp,
                    profit  = inc - exp
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    private fun <T> filterEntries(entries: List<T>, filter: TimeFilter, dateSelector: (T) -> Long): List<T> {
        if (filter == TimeFilter.ALL_TIME) return entries
        
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_YEAR)
        val currentYear = now.get(Calendar.YEAR)
        val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = now.get(Calendar.MONTH)

        return entries.filter {
            val entryCal = Calendar.getInstance().apply { timeInMillis = dateSelector(it) }
            val entryYear = entryCal.get(Calendar.YEAR)
            when (filter) {
                TimeFilter.DAILY -> {
                    entryYear == currentYear && entryCal.get(Calendar.DAY_OF_YEAR) == currentDay
                }
                TimeFilter.WEEKLY -> {
                    entryYear == currentYear && entryCal.get(Calendar.WEEK_OF_YEAR) == currentWeek
                }
                TimeFilter.MONTHLY -> {
                    entryYear == currentYear && entryCal.get(Calendar.MONTH) == currentMonth
                }
                TimeFilter.ALL_TIME -> true
            }
        }
    }

    fun setCurrentUser(username: String) {
        _currentUsername.value = username
        viewModelScope.launch {
            currentUser.value = AppDatabase.getDatabase(getApplication()).dairyDao().getUserByUsername(username)
        }
    }

    fun addCow(name: String, tag: String) {
        viewModelScope.launch {
            val username = _currentUsername.value ?: return@launch
            repository.insertCow(Cow(username = username, name = name, tagNumber = tag))
        }
    }

    fun addIncome(cowId: Long?, liters: Double, pricePerLiter: Double, fatPercentage: Double, amount: Double) {
        viewModelScope.launch {
            val username = _currentUsername.value ?: return@launch
            repository.insertIncome(
                IncomeEntry(
                    username = username, 
                    cowId = cowId, 
                    liters = liters, 
                    pricePerLiter = pricePerLiter,
                    fatPercentage = fatPercentage, 
                    totalAmount = amount
                ),
            )
        }
    }

    fun addExpense(category: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val username = _currentUsername.value ?: return@launch
            repository.insertExpense(
                ExpenseEntry(username = username, category = category, amount = amount, notes = notes),
            )
        }
    }
}
