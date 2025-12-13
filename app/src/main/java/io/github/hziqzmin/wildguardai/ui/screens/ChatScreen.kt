package io.github.hziqzmin.wildguardai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions // 👈 *** ADD THIS IMPORT ***
import androidx.compose.foundation.text.KeyboardOptions // 👈 *** ADD THIS IMPORT ***
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction // 👈 *** ADD THIS IMPORT ***
import androidx.compose.ui.unit.dp
import io.github.hziqzmin.wildguardai.Author
import io.github.hziqzmin.wildguardai.ChatMessage
import io.github.hziqzmin.wildguardai.MainViewModel
import kotlinx.coroutines.launch

/**
 * The main ChatScreen composable.
 * It displays either an empty state (like the "Home Page" screenshot)
 * or the list of chat messages, along with the new input bar.
 */
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isAITyping by viewModel.isAITyping.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll when a new message arrives
    LaunchedEffect(messages.size) {
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Conditionally show Empty State or Chat History
        if (messages.size <= 1) {
            // Show the "Home Page" empty state
            ChatEmptyState(modifier = Modifier.weight(1f))
        } else {
            // Show the chat message history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message)
                }

                if (isAITyping) {
                    item {
                        // Show a typing indicator
                        Text(
                            text = "WildGuard AI is typing...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }

        // The new input bar, used in both states
        ChatInputBar(
            viewModel = viewModel,
            isAITyping = isAITyping
        )
    }
}

/**
 * The "Home Page" UI shown when the chat is empty.
 */
@Composable
fun ChatEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "How can WildGuard AI help you?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * The new chat input bar from the screenshot.
 */
@Composable
fun ChatInputBar(viewModel: MainViewModel, isAITyping: Boolean) {
    var textInput by remember { mutableStateOf("") }
    // Color picked from the screenshot
    val darkContainerColor = Color(0xFF3B4948)
    val iconGrayColor = Color(0xFFB0B0B0)

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = darkContainerColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Text input field
            BasicTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp) // Set a min height
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                enabled = !isAITyping,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    if (textInput.isEmpty()) {
                        Text("Ask anything", color = iconGrayColor)
                    }
                    innerTextField()
                }
            )

            // Row for icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left icons
                IconButton(onClick = { /* TODO: Add action */ }, enabled = !isAITyping) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = iconGrayColor)
                }
                IconButton(onClick = { /* TODO: Attach action */ }, enabled = !isAITyping) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = iconGrayColor)
                }

                Spacer(Modifier.weight(1f))

                // Right icon (Mic or Send)
                val showSend = textInput.isNotBlank()
                IconButton(
                    onClick = {
                        if (showSend) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        } else {
                            // TODO: Handle Mic click
                        }
                    },
                    enabled = !isAITyping
                ) {
                    Icon(
                        imageVector = if (showSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (showSend) "Send" else "Microphone", // <-- THIS IS THE FIX
                        tint = if (showSend) Color.White else iconGrayColor // Send button is more prominent
                    )
                }
            }
        }
    }
}

/**
 * A simple composable to display a single chat bubble
 * (Moved from MainActivity.kt)
 */
@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.author == Author.USER) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.author == Author.USER) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f) // Max 80% of screen width
                .padding(4.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}