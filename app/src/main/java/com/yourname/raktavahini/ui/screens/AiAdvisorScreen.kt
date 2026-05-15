package com.yourname.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourname.raktavahini.data.ClaudeAiService
import com.yourname.raktavahini.ui.theme.SoftRed
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(navController: NavController) {
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Hello! I am your AI Blood Donation Advisor. Ask me anything about blood types, eligibility, donation process, or health tips! \uD83E\uDE78", false)
    )) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val suggestions = listOf("Am I eligible to donate?", "What is O- blood group?", "Side effects of donation?", "Can diabetics donate?")

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("AI Blood Advisor") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("<", fontSize = 20.sp, color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg -> ChatBubble(msg) }
                if (isLoading) {
                    item {
                        Row(Modifier.fillMaxWidth()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftRed)
                            ) { Text("Thinking...", Modifier.padding(10.dp)) }
                        }
                    }
                }
            }

            if (messages.size == 1) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.take(2).forEach { s ->
                        SuggestionChip(onClick = { input = s }, label = { Text(s, maxLines = 1) })
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.drop(2).forEach { s ->
                        SuggestionChip(onClick = { input = s }, label = { Text(s, maxLines = 1) })
                    }
                }
            }

            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text("Ask about blood donation...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val question = input.trim()
                        if (question.isNotBlank() && !isLoading) {
                            messages = messages + ChatMessage(question, true)
                            input = ""
                            isLoading = true
                            scope.launch {
                                val reply = ClaudeAiService.askAdvisor(question)
                                messages = messages + ChatMessage(reply, false)
                                isLoading = false
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    enabled = input.isNotBlank() && !isLoading,
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text(">>") }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (msg.isUser) 12.dp else 2.dp,
                bottomEnd = if (msg.isUser) 2.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else SoftRed
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                msg.text,
                modifier = Modifier.padding(10.dp),
                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}