package io.github.hziqzmin.wildguardai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hziqzmin.wildguardai.ui.theme.Cream
import io.github.hziqzmin.wildguardai.ui.theme.DarkGreen

// A simple data class for our tips
data class GuideTip(
    val title: String,
    val description: String
)

// Mock data for the guide
val survivalTips = listOf(
    GuideTip(
        title = "How to Build a Fire",
        description = "- You need tinder and kindling – dry grasses and branches that can catch fire easily.\n- Create an initial 'bird's nest' structure.\n- Ignite tinder by holding it near an open flame or using a friction method."
    ),
    GuideTip(
        title = "Finding Clean Water",
        description = "- Never drink stagnant water. Always look for flowing rivers or streams.\n- If possible, boil all water for at least 1 minute before drinking.\n- Morning dew collected from leaves can be a safe source in small amounts."
    ),
    GuideTip(
        title = "Creating a Simple Shelter",
        description = "- Find a natural shelter first, like a cave or dense cluster of trees.\n- A simple lean-to is effective. Prop large branches against a fallen log or rock.\n- Cover the frame with smaller branches, leaves, and moss to block wind and rain."
    ),
    GuideTip(
        title = "Basic Navigation",
        description = "- The sun rises in the east and sets in the west.\n- At night, find the North Star (in the Northern Hemisphere).\n- Moss typically grows thicker on the shadier side of trees (usually north)."
    )
)

/**
 * A mock screen showing a list of survival tips.
 */
@Composable
fun GuideScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Survival Guides",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // Create a Card for each tip
        items(survivalTips) { tip ->
            GuideTipCard(tip = tip)
        }
    }
}

/**
 * A reusable Card composable to display a single guide tip.
 */
@Composable
fun GuideTipCard(tip: GuideTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreen) // Using your theme colors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleLarge,
                color = Cream,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Cream,
                lineHeight = 22.sp // A bit more line spacing for readability
            )
        }
    }
}