package io.github.hziqzmin.wildguardai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hziqzmin.wildguardai.ui.theme.WildGuardAITheme
import io.github.hziqzmin.wildguardai.ui.theme.DarkGreen
import io.github.hziqzmin.wildguardai.ui.theme.Cream
import io.github.hziqzmin.wildguardai.ui.theme.AccentTurquoise
import io.github.hziqzmin.wildguardai.ui.theme.White
import io.github.hziqzmin.wildguardai.ui.theme.Black

/**
 * This composable function creates the UI for the Profile screen,
 * based on the provided design screenshot.
 */
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Profile Header Section
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(100.dp),
            tint = White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "hziqzmin", // This would be dynamic in a real app
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Premium", // This would also be dynamic
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Divider
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Menu Options Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.AttachMoney,
                text = "Upgrade Plan",
                onClick = { /* TODO: Handle Upgrade click */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Person,
                text = "Personalize",
                onClick = { /* TODO: Handle Personalize click */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Settings,
                text = "Settings",
                onClick = { /* TODO: Handle Settings click */ }
            )
        }
    }
}

/**
 * A reusable composable for a single item in the profile menu.
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    WildGuardAITheme {
        ProfileScreen()
    }
}