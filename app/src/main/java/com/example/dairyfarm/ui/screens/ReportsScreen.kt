package com.example.dairyfarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dairyfarm.ui.theme.PrimaryGreen
import com.example.dairyfarm.ui.viewmodels.DashboardViewModel
import com.example.dairyfarm.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, viewModel: DashboardViewModel) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val incomeList by viewModel.allIncome.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                actions = {
                    IconButton(onClick = {
                        ExportUtils.exportToPdfAndShare(
                            context, totalIncome, totalExpense, netProfit, incomeList, expenses
                        )
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export PDF", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ReportItem("Total Revenue", "₹${"%.2f".format(totalIncome)}", PrimaryGreen)
                ReportItem("Total Expenses", "₹${"%.2f".format(totalExpense)}", Color.Red)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ReportItem(
                    "Net Profit", 
                    "₹${"%.2f".format(netProfit)}", 
                    if (netProfit >= 0) PrimaryGreen else Color.Red,
                    isBold = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val categoryTotals = expenses.groupBy { it.category }
                    .mapValues { it.value.sumOf { exp -> exp.amount } }
                
                if (categoryTotals.isNotEmpty()) {
                    PieChartNative(data = categoryTotals)
                } else {
                    Text("No expenses recorded yet.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ReportItem(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
