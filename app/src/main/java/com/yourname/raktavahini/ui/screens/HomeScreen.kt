package com.yourname.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.data.daysUntilEligible
import com.yourname.raktavahini.data.isDonorEligible
import com.yourname.raktavahini.ui.theme.BloodRed
import com.yourname.raktavahini.ui.theme.DeepRed
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var donorName   by remember { mutableStateOf("") }
    var lastDonation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseRepository.getDonorProfile().onSuccess { donor ->
            donorName    = donor?.name ?: ""
            lastDonation = donor?.lastDonationDate ?: ""
        }
    }

    val eligible = isDonorEligible(lastDonation)
    val daysLeft = daysUntilEligible(lastDonation)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepRed, BloodRed, Color(0xFFE53935))))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Top bar — greeting + logout
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hello,", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Text(
                    donorName.ifBlank { FirebaseRepository.currentUserEmail?.substringBefore("@") ?: "Donor" },
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp
                )
            }
            TextButton(
                onClick = {
                    scope.launch {
                        FirebaseRepository.signOut()
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
            ) { Text("Logout", fontSize = 13.sp) }
        }

        Spacer(Modifier.height(20.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) { Text("🩸", fontSize = 42.sp) }

        Spacer(Modifier.height(10.dp))
        Text("Rakta-Vahini", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("रक्त वाहिनी", color = Color.White.copy(alpha = 0.75f), fontSize = 15.sp)
        Text(
            "Filtered Blood Donor Network",
            color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Eligibility banner (only shown if profile has donation date)
        if (lastDonation.isNotBlank()) {
            Card(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (eligible) Color(0xFF1B5E20) else Color(0xFF4A0000)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (eligible) "✅  You are eligible to donate today!"
                               else "⏳  $daysLeft more days until eligible",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // Menu buttons
        val buttons = listOf(
            Triple("🔍", "Emergency Search",    "search"),
            Triple("📝", "Register as Donor",   "register"),
            Triple("🆘", "Blood Request Board", "requests"),
            Triple("👤", "My Profile",          "profile"),
            Triple("📋", "Donation Log",        "donationlog"),
            Triple("📊", "Community Stats",     "stats"),
            Triple("🤖", "AI Blood Advisor",    "advisor"),
        )

        buttons.forEach { (icon, label, route) ->
            HomeButton(icon = icon, label = label) { navController.navigate(route) }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(28.dp))
        Text(
            "Every drop counts. Be a hero.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun HomeButton(icon: String, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.14f),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(icon, fontSize = 19.sp)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.5f))
    }
}