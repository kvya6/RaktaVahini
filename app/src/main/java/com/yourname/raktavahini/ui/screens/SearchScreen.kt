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
import com.yourname.raktavahini.data.Donor
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.ui.theme.GreenEligible
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var selectedGroup   by remember { mutableStateOf("") }
    var locationFilter  by remember { mutableStateOf("") }
    var results         by remember { mutableStateOf<List<Donor>>(emptyList()) }
    var searched        by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var expanded        by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val fieldShape = RoundedCornerShape(12.dp)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Emergency Search") },
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
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedGroup.ifBlank { "Any Blood Group" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Blood Group Needed") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = fieldShape
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Any Group") }, onClick = { selectedGroup = ""; expanded = false })
                    BLOOD_GROUPS.forEach { g ->
                        DropdownMenuItem(text = { Text(g) }, onClick = { selectedGroup = g; expanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = locationFilter, onValueChange = { locationFilter = it },
                label = { Text("City / Area (optional)") },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape
            )

            Button(
                onClick = {
                    isLoading = true; errorMsg = ""; searched = false
                    scope.launch {
                        FirebaseRepository.searchDonors(selectedGroup, locationFilter)
                            .onSuccess { results = it; searched = true }
                            .onFailure { errorMsg = it.message ?: "Search failed. Check internet."; searched = true }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else
                    Text("Find Eligible Donors", fontWeight = FontWeight.Bold)
            }

            if (errorMsg.isNotBlank()) Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)

            if (searched && errorMsg.isBlank()) {
                Text(
                    if (results.isEmpty()) "No eligible donors found — try broadening the filters."
                    else "${results.size} eligible donor(s) found:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (results.isEmpty()) MaterialTheme.colorScheme.error else GreenEligible,
                    fontWeight = FontWeight.SemiBold
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(results) { donor -> DonorCard(donor) }
                }
            }
        }
    }
}

@Composable
fun DonorCard(donor: Donor) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Blood group badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        donor.bloodGroup,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(donor.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("📍 ${donor.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (donor.lastDonationDate.isNotBlank())
                        Text("Last donated: ${donor.lastDonationDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) { Text("📞  Call Donor") }
        }
    }
}