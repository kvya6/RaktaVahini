package com.yourname.raktavahini.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.AppStats
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.ui.theme.BloodRed
import com.yourname.raktavahini.ui.theme.SoftRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    var stats     by remember { mutableStateOf(AppStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getStats().onSuccess { stats = it }
        isLoading = false
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Community Stats") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("‹", fontSize = 26.sp, color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Making a Difference", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Real-time community impact", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                StatCard("🩸", stats.totalDonors.toString(), "Registered Donors")
                StatCard("🏥", stats.totalRequests.toString(), "Blood Requests Posted")
                StatCard("❤️", (stats.totalDonors * 3).toString(), "Potential Lives Saved")
            }

            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftRed),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Did you know?", fontWeight = FontWeight.Bold, color = BloodRed)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "One blood donation can save up to 3 lives. Blood cannot be manufactured — " +
                        "it can only come from generous donors like you.",
                        color = BloodRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(emoji: String, value: String, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 34.sp, modifier = Modifier.width(48.dp))
            Column {
                Text(value, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary)
                Text(label, color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}