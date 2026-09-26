package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RedactionSuggestion
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiCopilotSheet(
    chatMessages: List<ChatMessage>,
    isAiLoading: Boolean,
    onSendMessage: (String) -> Unit,
    piiSuggestions: List<RedactionSuggestion>,
    onApplyAutoRedaction: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(DocSurfaceDark)
            .border(1.dp, DocBorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(12.dp)
            .testTag("ai_copilot_sheet")
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Document OS Intelligence",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by Gemini 3.5 Flash RAG Engine",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close AI",
                    tint = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PII Auto-Redaction Alert Banner if found
        if (piiSuggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF7F1D1D).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI detected ${piiSuggestions.size} sensitive items (SSN, emails, accounts)",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onApplyAutoRedaction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp).testTag("apply_auto_redaction_button")
                ) {
                    Text("Redact All", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Quick Suggestion Prompt Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PromptChip("Summarize Document") { onSendMessage("Provide a structured executive summary of this document.") }
            PromptChip("Verify Clearances") { onSendMessage("Verify MEP and architectural clearances mentioned in this sheet.") }
            PromptChip("Check PII") { onSendMessage("Scan this document for private personal info and summarize compliance risks.") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Message Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(chatMessages) { msg ->
                val isAi = msg.sender == "ai"
                val bg = if (isAi) DocSurfaceCardDark else DocPrimaryCyan.copy(alpha = 0.3f)
                val align = if (isAi) Alignment.Start else Alignment.End

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = align
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(0.5.dp, DocBorderDark, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DocSurfaceCardDark)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color(0xFFA855F7),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Document OS RAG is reasoning...",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask about dimensions, clauses, MEP specs...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = DocBorderDark
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        val q = inputQuery.trim()
                        inputQuery = ""
                        onSendMessage(q)
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFA855F7))
                    .testTag("send_ai_query_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PromptChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DocSurfaceCardDark)
            .border(0.5.dp, DocBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color(0xFFCBD5E1), fontSize = 10.sp)
    }
}
