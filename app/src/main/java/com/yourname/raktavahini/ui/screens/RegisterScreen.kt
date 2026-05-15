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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var name            by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var selectedGroup   by remember { mutableStateOf("") }
    var lastDonation    by remember { mutableStateOf("") }
    var expanded        by remember { mutableStateOf(false) }
    var showSuccess     by remember { mutableStateOf(false) }
    var isSaving        by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val fieldShape = RoundedCornerShape(12.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor  = MaterialTheme.colorScheme.primary
    )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Register as Donor") },
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
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Join as a Blood Donor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Your profile will be visible to people searching for donors in emergencies.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            OutlinedTextField(value = name, onValueChange = { name = it; showSuccess = false },
                label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(),
                shape = fieldShape, colors = fieldColors)

            OutlinedTextField(value = phone, onValueChange = { phone = it; showSuccess = false },
                label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth(),
                shape = fieldShape, colors = fieldColors)

            OutlinedTextField(value = location, onValueChange = { location = it; showSuccess = false },
                label = { Text("City / Area *") }, modifier = Modifier.fillMaxWidth(),
                shape = fieldShape, colors = fieldColors)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedGroup.ifBlank { "Select Blood Group *" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = fieldShape, colors = fieldColors
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    BLOOD_GROUPS.forEach { g ->
                        DropdownMenuItem(text = { Text(g) }, onClick = { selectedGroup = g; expanded = false })
                    }
                }
            }

            OutlinedTextField(value = lastDonation, onValueChange = { lastDonation = it },
                label = { Text("Last Donation Date (optional)") },
                placeholder = { Text("yyyy-MM-dd  or leave blank") },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)

            if (showSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "🎉  Registered! Thank you for being a lifesaver.",
                        Modifier.padding(14.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (errorMsg.isNotBlank()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || selectedGroup.isBlank()) {
                        errorMsg = "Please fill in Name, Phone, and Blood Group."
                        return@Button
                    }
                    isSaving = true; errorMsg = ""
                    scope.launch {
                        FirebaseRepository.saveDonorProfile(
                            Donor(name = name.trim(), phone = phone.trim(),
                                bloodGroup = selectedGroup, location = location.trim(),
                                lastDonationDate = lastDonation.trim())
                        ).onSuccess {
                            showSuccess = true
                            name = ""; phone = ""; location = ""; lastDonation = ""; selectedGroup = ""
                        }.onFailure { errorMsg = it.message ?: "Registration failed." }
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank() && phone.isNotBlank() && selectedGroup.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else
                    Text("Register as Donor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}