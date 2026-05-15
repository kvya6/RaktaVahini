package com.yourname.raktavahini.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.BLOOD_GROUPS
import com.yourname.raktavahini.data.Donor
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.data.isDonorEligible
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var name         by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var location     by remember { mutableStateOf("") }
    var lastDonation by remember { mutableStateOf("") }
    var isEligible   by remember { mutableStateOf(true) }
    var selectedGroup by remember { mutableStateOf("") }
    var expanded     by remember { mutableStateOf(false) }
    var saved        by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMsg     by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FirebaseRepository.getDonorProfile().onSuccess { donor ->
            donor?.let {
                name = it.name; phone = it.phone; location = it.location
                lastDonation = it.lastDonationDate; isEligible = it.isEligible
                selectedGroup = it.bloodGroup
            }
        }
        isLoading = false
    }

    val autoEligible = isDonorEligible(lastDonation)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("My Profile") },
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
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Eligibility badge
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (autoEligible && isEligible)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (autoEligible && isEligible) "✅  Eligible to donate"
                            else "⏳  Not eligible right now",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                val fieldShape = RoundedCornerShape(12.dp)
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor  = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(value = name, onValueChange = { name = it; saved = false },
                    label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape, colors = fieldColors)

                OutlinedTextField(value = phone, onValueChange = { phone = it; saved = false },
                    label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape, colors = fieldColors)

                OutlinedTextField(value = location, onValueChange = { location = it; saved = false },
                    label = { Text("City / Area") }, modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape, colors = fieldColors)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedGroup.ifBlank { "Select Blood Group" },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Blood Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        BLOOD_GROUPS.forEach { g ->
                            DropdownMenuItem(text = { Text(g) },
                                onClick = { selectedGroup = g; expanded = false; saved = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = lastDonation, onValueChange = { lastDonation = it; saved = false },
                    label = { Text("Last Donation Date") },
                    placeholder = { Text("yyyy-MM-dd  e.g. 2025-08-15") },
                    modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
                )

                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ready to Donate", fontWeight = FontWeight.SemiBold)
                            Text("Show as available in search", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(checked = isEligible, onCheckedChange = { isEligible = it; saved = false })
                    }
                }

                if (saved) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(10.dp)) {
                        Text("✅  Profile saved to cloud!", Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            FirebaseRepository.saveDonorProfile(
                                Donor(name = name, phone = phone, location = location,
                                    bloodGroup = selectedGroup, lastDonationDate = lastDonation, isEligible = isEligible)
                            ).onSuccess { saved = true; errorMsg = "" }
                             .onFailure { errorMsg = it.message ?: "Save failed." }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Save Profile to Cloud", fontWeight = FontWeight.Bold) }
            }
        }
    }
}