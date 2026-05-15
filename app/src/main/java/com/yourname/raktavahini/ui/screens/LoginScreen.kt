package com.yourname.raktavahini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.ui.theme.BloodRed
import com.yourname.raktavahini.ui.theme.DeepRed
import com.yourname.raktavahini.ui.theme.SoftRed
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var isSignUp    by remember { mutableStateOf(false) }
    var showPass    by remember { mutableStateOf(false) }
    var isLoading   by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepRed, BloodRed, Color(0xFFE53935)))),
        contentAlignment = Alignment.Center
    ) {
        // Decorative top circle
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(y = (-280).dp)
                .background(Color.White.copy(alpha = 0.06f), shape = androidx.compose.foundation.shape.CircleShape)
                .align(Alignment.TopCenter)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SoftRed, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🩸", fontSize = 36.sp) }

                Text(
                    "Rakta-Vahini",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BloodRed
                )
                Text(
                    if (isSignUp) "Create your account" else "Welcome back",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color(0xFFEEEEEE))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = "" },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodRed,
                        focusedLabelColor = BloodRed
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = "" },
                    label = { Text("Password") },
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        TextButton(onClick = { showPass = !showPass }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text(if (showPass) "Hide" else "Show", fontSize = 12.sp, color = BloodRed)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodRed,
                        focusedLabelColor = BloodRed
                    )
                )

                AnimatedVisibility(visible = errorMsg.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            errorMsg,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) { errorMsg = "Please fill in all fields."; return@Button }
                        if (password.length < 6) { errorMsg = "Password must be at least 6 characters."; return@Button }
                        isLoading = true; errorMsg = ""
                        scope.launch {
                            val result = if (isSignUp)
                                FirebaseRepository.signUp(email.trim(), password)
                            else
                                FirebaseRepository.signIn(email.trim(), password)
                            isLoading = false
                            result.onSuccess {
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            }.onFailure { e ->
                                errorMsg = when {
                                    e.message?.contains("email") == true       -> "Invalid email address."
                                    e.message?.contains("password") == true    -> "Wrong password."
                                    e.message?.contains("no user") == true     -> "No account found — sign up first."
                                    e.message?.contains("already") == true     -> "Email already registered — log in."
                                    e.message?.contains("network") == true     -> "No internet connection."
                                    else -> e.message ?: "Authentication failed."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    if (isLoading)
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    else
                        Text(
                            if (isSignUp) "Create Account" else "Login",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                }

                TextButton(onClick = { isSignUp = !isSignUp; errorMsg = "" }) {
                    Text(
                        if (isSignUp) "Already have an account? Login"
                        else "New here? Create an account",
                        color = BloodRed, fontSize = 13.sp
                    )
                }
            }
        }

        // Bottom tag line
        Text(
            "Every drop counts. Be a hero. 🩸",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}