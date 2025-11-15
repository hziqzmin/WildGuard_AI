package io.github.hziqzmin.wildguardai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import for by viewModels()
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Import for installSplashScreen
import io.github.hziqzmin.wildguardai.ui.theme.WildGuardAITheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // Get an instance of your MainViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install the splash screen *before* super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 2. Keep the splash screen visible based on the ViewModel's loading state
        //    It stays on screen as long as isLoading.value is true
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        enableEdgeToEdge() // Often handled by Scaffold, can re-enable if needed
        setContent {
            WildGuardAITheme {
                // Observe the loading state from the ViewModel
                val isLoading by viewModel.isLoading.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Only show the main app UI *after* loading is complete
                    if (!isLoading) {
                        // --- EDIT 1: Pass the viewModel in ---
                        WildGuardApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// --- NEW COMPOSABLE FOR YOUR ENTIRE APP UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildGuardApp(viewModel: MainViewModel) { // --- EDIT 2: Accept the viewModel ---
    // 1. State for managing which tab is selected. 2 is "Search" (Chat) by default.
    var selectedItemIndex by remember { mutableStateOf(2) }

    // 2. Scaffold provides the layout structure (top bar, bottom bar, content)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WildGuard AI") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // List of items for the bottom bar
                val items = listOf(
                    BottomNavItem("Guides", Icons.Default.Book),
                    BottomNavItem("Map", Icons.Default.Map),
                    BottomNavItem("Chat", Icons.Default.Search),
                    BottomNavItem("SOS", Icons.Default.Warning),
                    BottomNavItem("Profile", Icons.Default.Person)
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = { Text(item.label) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 3. This is the main content area of your app.
        //    It shows a different screen based on which tab is selected.
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItemIndex) {
                0 -> PlaceholderScreen("Guides Screen")
                1 -> PlaceholderScreen("Map Screen")
                2 -> ChatScreen(viewModel = viewModel) // --- EDIT 3: Pass the viewModel in ---
                3 -> PlaceholderScreen("SOS Screen")
                4 -> PlaceholderScreen("Profile Screen")
            }
        }
    }
}

// Data class to hold info for bottom navigation items
data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// --- REPLACED COMPOSABLE FOR YOUR CHAT SCREEN CONTENT ---
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    // 1. Observe the list of messages from the ViewModel
    val messages by viewModel.messages.collectAsState()
    val isAITyping by viewModel.isAITyping.collectAsState()

    // 2. State for the user's text input
    var textInput by remember { mutableStateOf("") }

    // 3. State for the LazyColumn to auto-scroll
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 4. Auto-scroll when a new message arrives
    LaunchedEffect(messages.size) {
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Message List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
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

        // 2. Text Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask WildGuard AI...") },
                // Disable input while AI is typing
                enabled = !isAITyping
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendMessage(textInput)
                        textInput = "" // Clear the input
                    }
                },
                // Disable button if input is blank or AI is typing
                enabled = textInput.isNotBlank() && !isAITyping
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

// A simple composable to display a single chat bubble
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

// --- GENERIC PLACEHOLDER SCREEN ---
@Composable
fun PlaceholderScreen(screenName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = screenName)
    }
}

@Preview(showBackground = true)
@Composable
fun WildGuardAppPreview() {
    WildGuardAITheme {
        // You can't preview the full app with a ViewModel easily.
        // Instead, preview the placeholder screen.
        PlaceholderScreen("Chat Screen Preview")
    }
}