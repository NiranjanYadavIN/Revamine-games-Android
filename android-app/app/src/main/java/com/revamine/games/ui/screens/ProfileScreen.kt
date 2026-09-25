package com.revamine.games.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revamine.games.data.PrefsStore

@Composable
fun ProfileScreen(
    prefs: PrefsStore,
    streak: Int,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    isMuted: Boolean,
    onToggleMute: (Boolean) -> Unit,
    isAdsRemoved: Boolean,
    onRemoveAdsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        "$streak Day Streak",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Come back daily to keep it going!",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        SettingsRow(
            icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
            title = "Sound",
            checked = !isMuted,
            onCheckedChange = { onToggleMute(!it) }
        )

        SettingsRow(
            icon = Icons.Filled.Shield,
            title = "Dark Theme",
            checked = isDarkTheme,
            onCheckedChange = onToggleTheme
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (isAdsRemoved) "VIP Active — No Ads" else "Remove Ads (VIP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Removes all banner and interstitial ads across the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                if (!isAdsRemoved) {
                    Button(onClick = onRemoveAdsClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Upgrade — \$1.99")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
