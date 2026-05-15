package com.yourname.raktavahini.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.yourname.raktavahini.data.DonationRecord
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.data.todayString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationLogScreen(navController: NavController) {
    var location     by remember { mutableStateOf("") }
    var note         by remember { mutableStateOf("") }
    var log          by remember { mutableStateOf<List<DonationRecord>>(emptyList()) }
    var showThankYou by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(true) }
    var isSaving     by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FirebaseRepository.getDonationLog().onSuccess { log = it }
        isLoading = false
    }

    val fieldShape = RoundedCornerShape(12.dp)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Donation Log") },
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
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Log a New Donation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(value = location, onValueChange = { location = it; showThankYou = false },
                label = { Text("Hospital / Donation Centre *") },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape)

            OutlinedTextField(value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape)

            if (showThankYou) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "🎉  Thank you! Your donation can save up to 3 lives. You are a hero!",
                        Modifier.padding(14.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = {
                    if (location.isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                        FirebaseRepository.logDonation(
                            DonationRecord(date = todayString(), location = location, note = note)
                        ).onSuccess {
                            showThankYou = true; location = ""; note = ""
                            FirebaseRepository.getDonationLog().onSuccess { log = it }
                        }
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = location.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else
                    Text("Log Donation", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider()

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                }
            } else {
                Text(
                    "My History  (${log.size} donation${if (log.size != 1) "s" else ""})",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                if (log.isEmpty()) {
                    Text("No donations logged yet — use the form above!", color = Color.Gray, fontSize = 14.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(log) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🩸", fontSize = 24.sp, modifier = Modifier.width(36.dp))
                                    Column {
                                        Text(record.date, fontWeight = FontWeight.SemiBold)
                                        Text(record.location, style = MaterialTheme.typography.bodyMedium)
                                        if (record.note.isNotBlank())
                                            Text(record.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}