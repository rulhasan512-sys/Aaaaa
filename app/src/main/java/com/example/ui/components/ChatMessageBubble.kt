@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AttachmentItem
import com.example.data.model.AttachmentType
import com.example.data.model.ChatMessage
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakToggle: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            UserMessageBubble(
                message = message,
                onImageClick = onImageClick
            )
        } else {
            AiMessageBubble(
                message = message,
                isSpeaking = isSpeaking,
                onSpeakToggle = onSpeakToggle,
                onImageClick = onImageClick,
                onCopyText = {
                    clipboardManager.setText(AnnotatedString(message.text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.widthIn(max = 320.dp)
    ) {
        // Render any attached images/files in user bubble
        if (message.attachments.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.attachments.forEach { item ->
                    UserAttachmentThumbnail(item = item, onImageClick = onImageClick)
                }
            }
        }

        if (message.text.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 4.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
                    .background(CrimsonPrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextMuted
            ),
            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        )
    }
}

@Composable
fun UserAttachmentThumbnail(
    item: AttachmentItem,
    onImageClick: (String) -> Unit
) {
    if (item.type == AttachmentType.IMAGE) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CrimsonLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { onImageClick(item.uri.toString()) }
        )
    } else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.type == AttachmentType.VIDEO) Icons.Default.Videocam else Icons.Default.Description,
                contentDescription = null,
                tint = if (item.type == AttachmentType.VIDEO) PurpleNeon else ElectricBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = item.name,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
    }
}

@Composable
fun AiMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakToggle: () -> Unit,
    onImageClick: (String) -> Unit,
    onCopyText: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth(0.92f)
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .padding(14.dp)
        ) {
            Column {
                // Header with RAKIB AI label and Voice Speak button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CrimsonSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = CrimsonLight,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RAKIB AI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonLight
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Voice Speak Button (TTS)
                        if (message.imageUrl == null && message.text.isNotBlank()) {
                            VoiceSpeakButton(
                                isSpeaking = isSpeaking,
                                onClick = onSpeakToggle
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Copy button
                        IconButton(
                            onClick = onCopyText,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Generated Picture AI Image Card
                if (message.imageUrl != null) {
                    GeneratedImageCard(
                        imageUrl = message.imageUrl,
                        onImageClick = onImageClick
                    )
                }

                // AI Response Text (with Markdown & Code Blocks)
                if (message.text.isNotBlank()) {
                    MarkdownContent(text = message.text)
                }
            }
        }

        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextMuted
            ),
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
}

@Composable
fun VoiceSpeakButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSpeaking) CrimsonPrimary.copy(alpha = 0.25f) else SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSpeaking) CrimsonLight else BorderDark
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                contentDescription = if (isSpeaking) "Stop Speaking" else "Read Aloud",
                tint = if (isSpeaking) CrimsonLight else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isSpeaking) "Speaking..." else "Listen",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSpeaking) CrimsonLight else TextSecondary
            )
            if (isSpeaking) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp * waveScale)
                        .clip(CircleShape)
                        .background(CrimsonLight)
                )
            }
        }
    }
}

@Composable
fun GeneratedImageCard(
    imageUrl: String,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onImageClick(imageUrl) }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Generated artwork",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎨 Picture AI (HD)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PurpleNeon
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onImageClick(imageUrl) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Fullscreen",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, imageUrl)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(
    text: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Parse code blocks vs regular paragraphs
    val parts = text.split("```")

    Column(modifier = modifier) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val lines = part.trim().split("\n")
                val lang = if (lines.isNotEmpty() && lines[0].length < 20 && !lines[0].contains(" ")) lines[0] else "code"
                val codeContent = if (lines.size > 1 && lang != "code") lines.drop(1).joinToString("\n") else part.trim()

                CodeBlockView(
                    language = lang,
                    code = codeContent,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(codeContent))
                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Regular formatted text
                if (part.isNotBlank()) {
                    FormattedText(text = part)
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonLight,
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { i, line ->
            var formattedLine = line
            val isBullet = formattedLine.trimStart().startsWith("- ") || formattedLine.trimStart().startsWith("* ")
            val isNumbered = Regex("^\\d+\\.\\s").containsMatchIn(formattedLine.trimStart())

            if (isBullet || isNumbered) {
                withStyle(SpanStyle(color = CrimsonLight, fontWeight = FontWeight.Bold)) {
                    append("• ")
                }
                formattedLine = formattedLine.replace(Regex("^[\\s]*[-*]\\s+"), "").replace(Regex("^[\\s]*\\d+\\.\\s+"), "")
            }

            // Bold parsing: **text**
            val boldParts = formattedLine.split("**")
            boldParts.forEachIndexed { bIndex, bPart ->
                if (bIndex % 2 == 1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(bPart)
                    }
                } else {
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(bPart)
                    }
                }
            }

            if (i < lines.size - 1) {
                append("\n")
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.5.sp,
            lineHeight = 21.sp
        ),
        modifier = modifier.padding(vertical = 2.dp)
    )
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
