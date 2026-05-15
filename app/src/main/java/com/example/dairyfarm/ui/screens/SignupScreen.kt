package com.example.dairyfarm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dairyfarm.data.AppDatabase
import com.example.dairyfarm.data.entities.User
import com.example.dairyfarm.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    var username        by remember { mutableStateOf("") }
    var farmName        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error           by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }

    val scope   = rememberCoroutineScope()
    val context = LocalContext.current
    val db      = AppDatabase.getDatabase(context)

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Hero ─────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", fontSize = 44.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Join Ksheera Sagara",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
            Text(
                text = "Start managing your farm smarter today",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "One account · All your farm insights 🐄",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ── Signup Card ──────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Fill in the details to register your farm",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                    )

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; error = "" },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
                        },
                        supportingText = {
                            Text("Minimum 3 characters", color = TextSecondary, fontSize = 11.sp)
                        },
                        singleLine = true,
                        isError = error.isNotEmpty() && username.trim().length < 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor  = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Farm Name
                    OutlinedTextField(
                        value = farmName,
                        onValueChange = { farmName = it; error = "" },
                        label = { Text("Farm Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryGreen)
                        },
                        supportingText = {
                            Text("e.g. Sri Lakshmi Dairy Farm", color = TextSecondary, fontSize = 11.sp)
                        },
                        singleLine = true,
                        isError = error.isNotEmpty() && farmName.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor  = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = "" },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        supportingText = {
                            Text("Minimum 6 characters", color = TextSecondary, fontSize = 11.sp)
                        },
                        singleLine = true,
                        isError = error.isNotEmpty() && password.length < 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor  = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = "" },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (confirmVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && confirmPassword != password)
                                Text("Passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        },
                        singleLine = true,
                        isError = error.isNotEmpty() && confirmPassword != password,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor  = PrimaryGreen
                        )
                    )

                    // Error banner
                    AnimatedVisibility(visible = error.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "⚠  $error",
                                color = Color(0xFFC62828),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create Account button
                    Button(
                        onClick = {
                            when {
                                username.trim().length < 3 ->
                                    error = "Username must be at least 3 characters"
                                farmName.isBlank() ->
                                    error = "Please enter your farm name"
                                password.length < 6 ->
                                    error = "Password must be at least 6 characters"
                                password != confirmPassword ->
                                    error = "Passwords do not match"
                                else -> {
                                    isLoading = true
                                    error     = ""
                                    scope.launch {
                                        try {
                                            db.dairyDao().insertUser(
                                                User(
                                                    username     = username.trim(),
                                                    passwordHash = password,
                                                    farmName     = farmName.trim()
                                                )
                                            )
                                            navController.navigate("login") {
                                                popUpTo("signup") { inclusive = true }
                                            }
                                        } catch (e: Exception) {
                                            error     = "Username '${username.trim()}' is already taken. Try another."
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back to login
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already a member?  ", color = TextSecondary, fontSize = 14.sp)
                        TextButton(
                            onClick = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Sign in →",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ── Footer note ──────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "🔒  Your data is stored securely on your device\nNo internet required • No data shared",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}
