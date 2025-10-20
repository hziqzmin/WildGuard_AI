package io.github.hziqzmin.wildguardai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import for by viewModels()
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Import for installSplashScreen
import io.github.hziqzmin.wildguardai.ui.theme.WildGuardAITheme

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

        // enableEdgeToEdge() // Often handled by Scaffold, can re-enable if needed
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
                        // Replace this Text with your actual main app UI call,
                        // e.g., MyAppNavigation() or ChatScreen()
                        //Greeting("WildGuard AI")
                        WildGuardApp()
                    }
                    // Optional: You could show a different minimal loading indicator here
                    // within the main theme if you prefer, but usually letting the
                    // splash screen handle it is cleaner.
                }
            }
        }
    }
}

// --- NEW COMPOSABLE FOR YOUR ENTIRE APP UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildGuardApp() {
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
                2 -> ChatScreen() // Your main chat screen will go here
                3 -> PlaceholderScreen("SOS Screen")
                4 -> PlaceholderScreen("Profile Screen")
            }
        }
    }
}

// Data class to hold info for bottom navigation items
data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// --- NEW COMPOSABLE FOR YOUR CHAT SCREEN CONTENT ---
@Composable
fun ChatScreen() {
    // TODO: Build the actual chat UI from your Figma design here
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "How can WildGuard AI help you?")
        // ... Add your text input field and buttons here ...
    }
}

// --- NEW GENERIC PLACEHOLDER SCREEN ---
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
        WildGuardApp()
    }
}
