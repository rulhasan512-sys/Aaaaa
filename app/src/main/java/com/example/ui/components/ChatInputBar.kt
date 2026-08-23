package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.data.model.AttachmentItem
import com.example.data.model.ImageAspectRatio
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    stagedAttachments: List<AttachmentItem>,
    onRemoveAttachment: (AttachmentItem) -> Unit,
    selectedModel: AiModel,
    onOpenModelSelector: () -> Unit,
    selectedAspectRatio: ImageAspectRatio,
    onOpenAspectRatioSelector: () -> Unit,
    autoVoiceRead: Boolean,
    onToggleAutoVoiceRead: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onOpenAttachmentPicker: () -> Unit,
    onStartVoiceRecognition: () -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val canSend = (inputText.trim().isNotBlank() || stagedAttachments.isNotEmpty()) && !isGenerating

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF030407).copy(alpha = 0.95f), Color(0xFF030407))
                )
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Staged attachment thumbnails tray
        StagedAttachmentTray(
            attachments = stagedAttachments,
            onRemove = onRemoveAttachment
        )

        // Main input container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                .padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Input TextField
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (selectedModel == AiModel.PICTURE_AI) "Describe the image you want to generate..." else "What do you want to build, upload, or ask?",
                            color = TextMuted,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
                        )
                    }

                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputTextChanged,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CrimsonLight),
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_text_field")
                    )
                }

                // Action Bar (Bottom row inside container)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: (+) Attachment button, Model selector badge, Aspect ratio badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // (+) Plus Attachment Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .border(1.dp, BorderDark, CircleShape)
                                .clickable(onClick = onOpenAttachmentPicker)
                                .testTag("attachment_plus_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Attachment",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Model Selector Pill
                        Surface(
                            onClick = onOpenModelSelector,
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.testTag("model_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = selectedModel.getIcon(),
                                    contentDescription = null,
                                    tint = selectedModel.iconColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedModel.displayName,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Aspect Ratio Pill (Only for Picture AI)
                        if (selectedModel == AiModel.PICTURE_AI) {
                            Surface(
                                onClick = onOpenAspectRatioSelector,
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.testTag("aspect_ratio_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = null,
                                        tint = PurpleNeon,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedAspectRatio.id,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Auto-Voice Reading Pill Indicator
                        Surface(
                            onClick = onToggleAutoVoiceRead,
                            shape = RoundedCornerShape(16.dp),
                            color = if (autoVoiceRead) ElectricBlue.copy(alpha = 0.2f) else SurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (autoVoiceRead) ElectricBlue else BorderDark
                            ),
                            modifier = Modifier.testTag("auto_voice_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Voice Mode",
                                    tint = if (autoVoiceRead) ElectricBlue else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (autoVoiceRead) "Voice ON" else "Voice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (autoVoiceRead) ElectricBlue else TextMuted
                                )
                            }
                        }
                    }

                    // Right Side: Voice Mic input button + Send button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Mic Button (Speech to Text)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .border(1.dp, BorderDark, CircleShape)
                                .clickable(onClick = onStartVoiceRecognition)
                                .testTag("voice_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = ElectricBlue,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Send Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (canSend) Brush.horizontalGradient(
                                        listOf(CrimsonPrimary, CrimsonLight)
                                    ) else SolidColor(SurfaceCard)
                                )
                                .border(
                                    1.dp,
                                    if (canSend) CrimsonLight else BorderDark,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable(enabled = canSend, onClick = onSend)
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        color = CrimsonLight,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Send",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (canSend) TextPrimary else TextMuted
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (canSend) TextPrimary else TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
