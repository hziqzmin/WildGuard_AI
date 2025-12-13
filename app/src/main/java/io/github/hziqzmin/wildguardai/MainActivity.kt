package io.github.hziqzmin.wildguardai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import for by viewModels()
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Search // <-- ADDED for FAB
import androidx.compose.material.icons.filled.Settings // <-- ADDED for Profile
import androidx.compose.material.icons.filled.AttachMoney // <-- ADDED for Profile
import androidx.compose.material3.*
import io.github.hziqzmin.wildguardai.ui.theme.DarkGreen
import io.github.hziqzmin.wildguardai.ui.theme.Cream
import io.github.hziqzmin.wildguardai.ui.theme.AccentTurquoise
import io.github.hziqzmin.wildguardai.ui.theme.White
import io.github.hziqzmin.wildguardai.ui.theme.Black

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Import for installSplashScreen
import io.github.hziqzmin.wildguardai.ui.screens.GuideScreen
import io.github.hziqzmin.wildguardai.ui.screens.MapScreen
import io.github.hziqzmin.wildguardai.ui.screens.SosScreen
import io.github.hziqzmin.wildguardai.ui.screens.ChatScreen
// REMOVED these imports, as we define the screens in this file
// import io.github.hziqzmin.wildguardai.ui.screens.ChatScreen
// import io.github.hziqzmin.wildguardai.ui.screens.ProfileScreen
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
                        WildGuardApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// --- UPDATED COMPOSABLE FOR YOUR ENTIRE APP UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildGuardApp(viewModel: MainViewModel) {
    // 1. State for managing which tab is selected. 4 = Profile (to match image)
    var selectedItemIndex by remember { mutableStateOf(2) }

    // List of items for the bottom bar.
    // NOTE: Using Search icon (index 2) from your image
    val items = listOf(
        BottomNavItem("Guides", Icons.Default.Book),
        BottomNavItem("Map", Icons.Default.Map),
        BottomNavItem("AI", Icons.Default.Chat),
        BottomNavItem("SOS", Icons.Default.Warning),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 1. Set the main background color for the content area
        containerColor = Cream,

        // 2. Use CenterAlignedTopAppBar
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WildGuard AI", color = White) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = White // Make icon white
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkGreen // Set background color
                )
            )
        },

        // 5. Use BottomAppBar with a cutout for the FAB
        bottomBar = {
            NavigationBar(
                containerColor = Cream, // Light background for the bar
                contentColor = DarkGreen
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedItemIndex == index
                    val isAiButton = index == 2

                    // The "Search" button will be AccentTurquoise when not selected
                    val unselectedColor = if (isAiButton) AccentTurquoise else Color.Gray

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItemIndex = index },
                        label = { Text(item.label) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkGreen,
                            selectedTextColor = DarkGreen,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = AccentTurquoise.copy(alpha = 0.2f) // Use a subtle indicator
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // 6. Main content area
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItemIndex) {
                0 -> GuideScreen()
                1 -> MapScreen()
                2 -> ChatScreen(viewModel = viewModel)
                3 -> SosScreen()
                4 -> ProfileScreen()
            }
        }
    }
}


// Data class to hold info for bottom navigation items
data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// --- YOUR CHAT SCREEN CONTENT (from your file) ---
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

// A simple composable to display a single chat bubble (from your file)
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

// --- NEW PROFILE SCREEN COMPOSABLE (to match your image) ---
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // The content background is Cream
            .background(Cream)
    ) {
        // --- Top Dark Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGreen) // This blends with the TopAppBar
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Black)
                    .padding(12.dp),
                tint = DarkGreen
            )

            // Username
            Text(
                text = "hziqzmin",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Premium Status
            Text(
                text = "Premium",
                color = AccentTurquoise, // Using the custom gold-like color
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- Divider ---
        // This divider is inside the DarkGreen section in your image
        Divider(color = White.copy(alpha = 0.5f), thickness = 1.dp)

        // --- Light-Background List Items ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileListItem(
                text = "Upgrade Plan",
                icon = Icons.Default.AttachMoney
            )
            ProfileListItem(
                text = "Personalize",
                icon = Icons.Default.Person
            )
            ProfileListItem(
                text = "Settings",
                icon = Icons.Default.Settings
            )
        }
    }
}

// Helper composable for the list items
@Composable
fun ProfileListItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Handle click */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Black
        )
        Text(
            text = text,
            color = Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// --- GENERIC PLACEHOLDER SCREEN (from your file) ---
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
        // Instead, let's preview the new Profile Screen!
        ProfileScreen()
    }
}