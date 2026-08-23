package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopNavBar(
    selectedModel: AiModel,
    onOpenModelSelector: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onOpenApiKeySettings: () -> Unit,
    autoVoiceRead: Boolean,
    hasCustomApiKey: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Sidebar Toggle Button
        IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(1.dp, BorderDark, CircleShape)
                .testTag("sidebar_toggle_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Chats",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Center: Model Selector Pill
        Surface(
            onClick = onOpenModelSelector,
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.testTag("top_model_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = selectedModel.getIcon(),
                    contentDescription = null,
                    tint = selectedModel.iconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedModel.displayName,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        // Right Actions: API Key Settings, Voice Settings & New Chat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // API Key Button
            IconButton(
                onClick = onOpenApiKeySettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (hasCustomApiKey) CrimsonSubtle else SurfaceCard)
                    .border(
                        1.dp,
                        if (hasCustomApiKey) CrimsonLight.copy(alpha = 0.5f) else BorderDark,
                        CircleShape
                    )
                    .testTag("api_key_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "API Key Settings",
                    tint = if (hasCustomApiKey) CrimsonLight else TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Voice Settings Button
            IconButton(
                onClick = onOpenVoiceSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (autoVoiceRead) ElectricBlue.copy(alpha = 0.2f) else SurfaceCard)
                    .border(
                        1.dp,
                        if (autoVoiceRead) ElectricBlue.copy(alpha = 0.5f) else BorderDark,
                        CircleShape
                    )
                    .testTag("voice_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Voice Settings",
                    tint = if (autoVoiceRead) ElectricBlue else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // New Chat Button
            IconButton(
                onClick = onNewChat,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CrimsonSubtle)
                    .border(1.dp, CrimsonLight.copy(alpha = 0.4f), CircleShape)
                    .testTag("top_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = CrimsonLight,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
