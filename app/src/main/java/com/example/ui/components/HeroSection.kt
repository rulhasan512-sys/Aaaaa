package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeroBackgroundGlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-center electric blue radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.25f),
                        ElectricBlue.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.15f),
                    radius = width * 0.7f * pulseScale
                )
            )

            // Center neon crimson radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CrimsonPrimary.copy(alpha = 0.20f),
                        CrimsonLight.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = width * 0.65f * pulseScale
                )
            )

            // Curved concentric glowing decorative arcs
            val arcCenter = Offset(width * 0.5f, height * 0.52f)
            val baseRadius = width * 0.45f

            drawCircle(
                color = CrimsonPrimary.copy(alpha = 0.2f),
                radius = baseRadius * 1.05f,
                center = arcCenter,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = CrimsonLight.copy(alpha = 0.12f),
                radius = baseRadius * 1.18f,
                center = arcCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = baseRadius * 1.32f,
                center = arcCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSection(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing App Icon / Badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CrimsonPrimary.copy(alpha = 0.35f), SurfaceCard)
                    )
                )
                .border(1.5.dp, CrimsonPrimary.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "RAKIB AI",
                tint = CrimsonLight,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title: Welcome to RAKIB AI
        val welcomeText = buildAnnotatedString {
            append("Welcome to ")
            withStyle(
                style = SpanStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(CrimsonLight, CrimsonPrimary, Color.White)
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic
                )
            ) {
                append("RAKIB AI")
            }
        }

        Text(
            text = welcomeText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Experience the most powerful AI for text, voice reading, photo, file & video analysis, and image generation.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Suggestion Chips
        Text(
            text = "TRY ASKING",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PromptSuggestionChip(
                icon = Icons.Default.Image,
                iconColor = Color(0xFFA855F7),
                text = "🎨 Picture AI: Futuristic BMW sports car on neon street",
                onClick = { onPromptSelected("A stunning BMW M5 racing on a neon cyberpunk street, 4k photorealistic") }
            )
            PromptSuggestionChip(
                icon = Icons.Default.Mic,
                iconColor = Color(0xFF38BDF8),
                text = "🎙️ Voice AI: আমাকে বাংলায় মহাবিশ্বের সৃষ্টি সম্পর্কে বলো",
                onClick = { onPromptSelected("আমাকে বাংলায় মহাবিশ্বের সৃষ্টি এবং বিগ ব্যাং সম্পর্কে বিস্তারিত বুঝিয়ে বলো।") }
            )
            PromptSuggestionChip(
                icon = Icons.Default.UploadFile,
                iconColor = Color(0xFF10B981),
                text = "📎 Multimodal: ছবি ও ফাইল বিশ্লেষণ করো",
                onClick = { onPromptSelected("ছবি বা ফাইলের গুরুত্বপূর্ণ বিষয়গুলো বিশদভাবে বিশ্লেষণ করে দাও।") }
            )
            PromptSuggestionChip(
                icon = Icons.Default.Code,
                iconColor = CrimsonLight,
                text = "💻 Code: Write a clean Kotlin coroutine pipeline",
                onClick = { onPromptSelected("Show me a clean, production-ready Kotlin Coroutine & Flow implementation with error handling in Android.") }
            )
        }
    }
}

@Composable
fun PromptSuggestionChip(
    icon: ImageVector,
    iconColor: Color,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
