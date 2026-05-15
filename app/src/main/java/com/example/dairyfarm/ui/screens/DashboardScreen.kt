package com.example.dairyfarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dairyfarm.R
import androidx.compose.ui.res.stringResource
import com.example.dairyfarm.ui.theme.*
import com.example.dairyfarm.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val incomeList by viewModel.allIncome.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()
    
    val isProfitable = netProfit >= 0
    val profitMargin = if (totalIncome > 0) (netProfit / totalIncome) * 100 else 0.0
    val isNoData = totalIncome == 0.0 && totalExpense == 0.0
    
    val healthStatusRes = when {
        isNoData -> R.string.stable
        netProfit < 0 -> R.string.loss
        profitMargin < 15.0 -> R.string.risk
        profitMargin < 35.0 -> R.string.stable
        else -> R.string.excellent
    }
    
    val healthColor = when {
        isNoData -> ProfitGreen
        netProfit < 0 -> LossRed
        profitMargin < 15.0 -> Color(0xFFF57F17) // Deep Amber
        profitMargin < 35.0 -> Color(0xFF1976D2) // Blue
        else -> ProfitGreen
    }
    
    val healthBgColor = when {
        isNoData -> LightGreen
        netProfit < 0 -> LightRed
        profitMargin < 15.0 -> Color(0xFFFFF3E0) // Light Orange
        profitMargin < 35.0 -> Color(0xFFE3F2FD) // Light Blue
        else -> LightGreen
    }
    var showRecommendDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentUser?.farmName ?: "Dairy Farm", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(currentUser?.username?.let { "@$it" } ?: "Dashboard", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = { com.example.dairyfarm.utils.LocaleManager.toggleLocale() }) {
                        Icon(Icons.Filled.Language, contentDescription = "Change Language", tint = Color.White)
                    }
                    IconButton(onClick = { 
                        com.example.dairyfarm.utils.ExportUtils.exportToPdfAndShare(
                            context, totalIncome, totalExpense, netProfit, incomeList, expenses
                        )
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { showRecommendDialog = true }) {
                        Icon(Icons.Filled.Star, contentDescription = "Recommend", tint = Color.White)
                    }
                    IconButton(onClick = {
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RecommendationBanner(totalIncome, totalExpense)
            }
            
            item {
                val selectedFilter by viewModel.timeFilter.collectAsState()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    com.example.dairyfarm.ui.viewmodels.TimeFilter.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.setTimeFilter(filter) },
                            label = { 
                                Text(
                                    when(filter) {
                                        com.example.dairyfarm.ui.viewmodels.TimeFilter.DAILY -> stringResource(R.string.daily)
                                        com.example.dairyfarm.ui.viewmodels.TimeFilter.WEEKLY -> stringResource(R.string.weekly)
                                        com.example.dairyfarm.ui.viewmodels.TimeFilter.MONTHLY -> stringResource(R.string.monthly)
                                        com.example.dairyfarm.ui.viewmodels.TimeFilter.ALL_TIME -> stringResource(R.string.all_time)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                // Profit Indicator Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = healthBgColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.financial_health),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(healthStatusRes),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = healthColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%.2f".format(netProfit)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IncomeExpenseCard(stringResource(R.string.total_income), totalIncome, PrimaryGreen, Modifier.weight(1f))
                    IncomeExpenseCard(stringResource(R.string.total_expense), totalExpense, PrimaryRed, Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.expense_analysis),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Calculate category sums
                        val categoryTotals = expenses.groupBy { it.category }
                            .mapValues { it.value.sumOf { exp -> exp.amount } }
                        
                        if (categoryTotals.isNotEmpty()) {
                            PieChartNative(data = categoryTotals)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No expenses recorded.", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.monthly_trends),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        MonthlyTrendChart(trendData = monthlyTrend)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Extra padding at the bottom of the list
            }
        }
    }
    if (showRecommendDialog) {
        RecommendationDialog(totalIncome, totalExpense) { showRecommendDialog = false }
    }
}

@Composable
fun RecommendationDialog(income: Double, expense: Double, onDismiss: () -> Unit) {
    val recommendations = remember(income, expense) {
        mutableListOf<String>().apply {
            if (expense > income) {
                add("CRITICAL: Your expenses (₹$expense) are higher than income (₹$income).")
                add("Action: Audit fodder wastage and reduce unnecessary labour costs.")
            } else if (income > 0 && expense / income > 0.7) {
                add("ADVICE: High input costs detected (70%+ of income).")
                add("Action: Look for wholesale fodder suppliers or cultivate your own fodder.")
            } else if (income > 100000) {
                add("OPPORTUNITY: Your revenue is strong.")
                add("Action: Consider automated milking machines to improve efficiency.")
            } else {
                add("GOOD: Farm is running stably.")
                add("Action: Keep regular health checkups for cows to maintain yield.")
            }
            if (income > 0) {
                add("Insight: Your current profit margin is ${"%.1f".format(((income - expense) / income) * 100)}%.")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Farm Recommendations", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                recommendations.forEach { rec ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold)
                        Text(rec)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Got it") }
        }
    )
}

@Composable
fun RecommendationBanner(income: Double, expense: Double) {
    val recommendation = remember(income, expense) {
        when {
            expense > income -> "Warning: Expenses exceed income. Review labour and fodder costs."
            expense > 0 && income / expense < 1.5 -> "Tip: Consider optimizing feed costs to increase profit margin."
            income > 0 && expense == 0.0 -> "Great start! Remember to log all maintenance and feed expenses."
            income > 50000 -> "Stable income detected. Maybe it's time to invest in better quality breeds?"
            else -> "Keep monitoring your daily yields and expenses for better insights."
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
