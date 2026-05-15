package com.yourname.raktavahini.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.BLOOD_GROUPS
import com.yourname.raktavahini.data.BloodRequest
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.data.URGENCY_LEVELS
import com.yourname.raktavahini.data.todayString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodRequestScreen(navController: NavController) {
    var requests     by remember { mutableStateOf<List<BloodRequest>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var showForm     by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var bloodGroup      by remember { mutableStateOf("") }
    var hospital        by remember { mutableStateOf("") }
    var city            by remember { mutableStateOf("") }
    var contactName     by remember { mutableStateOf("") }
    var contactPhone    by remember { mutableStateOf("") }
    var urgency         by remember { mutableStateOf("Normal") }
    var bgExpanded      by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getBloodRequests().onSuccess { requests = it }
        isLoading = false
    }

    val fieldShape = RoundedCornerShape(12.dp)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Blood Request Board") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("‹", fontSize = 26.sp, color = Color.White)
                }
            },
            actions = {
                TextButton(onClick = { showForm = !showForm; errorMsg = "" }) {
                    Text(
                        if (showForm) "View Board" else "+ Post Request",
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (showForm) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text("Post Emergency Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))

                        ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = it }) {
                            OutlinedTextField(
                                value = bloodGroup.ifBlank { "Select Blood Group *" },
                                onValueChange = {}, readOnly = true,
                                label = { Text("Blood Group Needed") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = fieldShape
                            )
                            ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                                BLOOD_GROUPS.forEach { g ->
                                    DropdownMenuItem(text = { Text(g) }, onClick = { bloodGroup = g; bgExpanded = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = hospital, onValueChange = { hospital = it },
                            label = { Text("Hospital Name *") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = city, onValueChange = { city = it },
                            label = { Text("City *") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = contactName, onValueChange = { contactName = it },
                            label = { Text("Contact Person Name") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it },
                            label = { Text("Contact Phone *") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape)
                        Spacer(Modifier.height(6.dp))

                        ExposedDropdownMenuBox(expanded = urgencyExpanded, onExpandedChange = { urgencyExpanded = it }) {
                            OutlinedTextField(
                                value = urgency, onValueChange = {}, readOnly = true,
                                label = { Text("Urgency Level") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(urgencyExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = fieldShape
                            )
                            ExposedDropdownMenu(expanded = urgencyExpanded, onDismissRequest = { urgencyExpanded = false }) {
                                URGENCY_LEVELS.forEach { u ->
                                    DropdownMenuItem(text = { Text(u) }, onClick = { urgency = u; urgencyExpanded = false })
                                }
                            }
                        }

                        if (errorMsg.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (bloodGroup.isBlank() || hospital.isBlank() || city.isBlank() || contactPhone.isBlank()) {
                                    errorMsg = "Please fill all required fields (*)."; return@Button
                                }
                                scope.launch {
                                    FirebaseRepository.postBloodRequest(BloodRequest(
                                        bloodGroup = bloodGroup, hospital = hospital, city = city,
                                        contactName = contactName, contactPhone = contactPhone,
                                        urgency = urgency, timestamp = todayString(),
                                        postedByUid = FirebaseRepository.currentUserId ?: ""
                                    )).onSuccess {
                                        showForm = false; isLoading = true
                                        FirebaseRepository.getBloodRequests().onSuccess { requests = it }
                                        isLoading = false
                                    }.onFailure { errorMsg = it.message ?: "Failed to post." }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Post Request", fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (requests.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No active blood requests.\nTap '+ Post Request' to add one.",
                            style = MaterialTheme.typography.bodyLarge, color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(requests) { req -> BloodRequestCard(req) }
                    }
                }
            }
        }
    }
}

@Composable
fun BloodRequestCard(request: BloodRequest) {
    val context = LocalContext.current
    val urgencyColor = when (request.urgency) {
        "Critical" -> Color(0xFFB71C1C)
        "Urgent"   -> Color(0xFFE65100)
        else       -> Color(0xFF2E7D32)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        request.bloodGroup,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.hospital, fontWeight = FontWeight.SemiBold)
                    Text("📍 ${request.city}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Card(colors = CardDefaults.cardColors(containerColor = urgencyColor), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        request.urgency, color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (request.contactName.isNotBlank())
                Text("Contact: ${request.contactName}", style = MaterialTheme.typography.bodySmall)
            Text("Posted: ${request.timestamp}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.contactPhone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("📞  Call: ${request.contactPhone}") }
        }
    }
}