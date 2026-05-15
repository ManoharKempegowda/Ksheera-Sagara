package com.example.dairyfarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dairyfarm.ui.screens.*
import com.example.dairyfarm.ui.theme.DairyFarmTheme
import com.example.dairyfarm.ui.viewmodels.DashboardViewModel
import com.example.dairyfarm.utils.LocaleManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", R.string.dashboard, Icons.Default.Dashboard)
    object Income : Screen("income", R.string.income, Icons.Default.TrendingUp)
    object Expense : Screen("expense", R.string.expense, Icons.Default.Payments)
    object Reports : Screen("reports", R.string.reports, Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val locale by LocaleManager.currentLocale.collectAsState()
            val baseContext = LocalContext.current
            
            val localizedContext = remember(locale) {
                LocaleManager.getLocalizedContext(baseContext, locale)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                DairyFarmTheme {
                    val navController = rememberNavController()
                    val viewModel: DashboardViewModel = viewModel()
                    
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(
                    Screen.Dashboard,
                    Screen.Income,
                    Screen.Expense,
                    Screen.Reports
                )
                
                val showBottomBar = currentDestination?.route in items.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleResId)) },
                                        label = { Text(stringResource(screen.titleResId)) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(navController, viewModel)
                            }
                            composable("signup") {
                                SignupScreen(navController)
                            }
                            composable("dashboard") {
                                DashboardScreen(navController, viewModel)
                            }
                            composable("income") {
                                IncomeScreen(navController, viewModel)
                            }
                            composable("expense") {
                                ExpenseScreen(navController, viewModel)
                            }
                            composable("reports") {
                                ReportsScreen(navController, viewModel)
                            }
                            composable("add_income") {
                                AddIncomeScreen(navController, viewModel)
                            }
                            composable("add_expense") {
                                AddExpenseScreen(navController, viewModel)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

