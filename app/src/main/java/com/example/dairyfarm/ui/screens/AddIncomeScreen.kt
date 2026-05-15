package com.example.dairyfarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dairyfarm.ui.theme.PrimaryGreen
import com.example.dairyfarm.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(navController: NavController, viewModel: DashboardViewModel) {
    var liters by remember { mutableStateOf("") }
    var fatPercentage by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }

    val calculatedTotal = remember(liters, pricePerLiter) {
        val l = liters.toDoubleOrNull() ?: 0.0
        val p = pricePerLiter.toDoubleOrNull() ?: 0.0
        l * p
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Income", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = liters,
                onValueChange = { liters = it },
                label = { Text("Liters") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fatPercentage,
                onValueChange = { fatPercentage = it },
                label = { Text("Fat %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pricePerLiter,
                onValueChange = { pricePerLiter = it },
                label = { Text("Price per Liter (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Amount: ₹${"%.2f".format(calculatedTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val l = liters.toDoubleOrNull() ?: 0.0
                    val f = fatPercentage.toDoubleOrNull() ?: 0.0
                    val p = pricePerLiter.toDoubleOrNull() ?: 0.0
                    if (l > 0 && p > 0) {
                        viewModel.addIncome(
                            cowId = null, 
                            liters = l, 
                            pricePerLiter = p,
                            fatPercentage = f, 
                            amount = calculatedTotal
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Save Income")
            }
        }
    }
}
