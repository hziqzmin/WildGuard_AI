package io.github.hziqzmin.wildguardai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hziqzmin.wildguardai.ui.theme.AccentTurquoise
import io.github.hziqzmin.wildguardai.ui.theme.Cream
import io.github.hziqzmin.wildguardai.ui.theme.DarkGreen
import io.github.hziqzmin.wildguardai.ui.theme.White
import androidx.compose.foundation.Image // Import for Image composable
import androidx.compose.ui.layout.ContentScale // Import for ContentScale
import androidx.compose.ui.res.painterResource // Import for painterResource
import io.github.hziqzmin.wildguardai.R // Import your R file

/**
 * A mock screen for a plain, offline map.
 */
@Composable
fun MapScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream), // Use Cream as the base background
        contentAlignment = Alignment.Center
    ) {
        // Mock Map Placeholder
        Image(
            painter = painterResource(id = R.drawable.mock_map), // <-- REPLACE 'jungle_map' with your image name
            contentDescription = "Jungle Map",
            modifier = Modifier.fillMaxSize(), // Make the image fill the entire Box
            contentScale = ContentScale.Crop // Crop to fill, or use .Fit to show entire image
        )

        // Mock Zoom Buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { /* TODO: Handle Zoom In */ },
                containerColor = AccentTurquoise,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, "Zoom In")
            }

            Spacer(Modifier.height(16.dp))

            FloatingActionButton(
                onClick = { /* TODO: Handle Zoom Out */ },
                containerColor = AccentTurquoise,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Remove, "Zoom Out")
            }
        }
    }
}