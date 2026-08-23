package com.example.data.model

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AiModel(
    val id: String,
    val displayName: String,
    val description: String,
    val badge: String,
    val badgeColor: Color,
    val iconColor: Color
) {
    GEMINI_FLASH(
        id = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        description = "Most stable & smart",
        badge = "Default",
        badgeColor = Color(0x33FF0033),
        iconColor = Color(0xFFFF3366)
    ),
    GEMINI_LITE(
        id = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Lite",
        description = "Lightning fast responses",
        badge = "Fast",
        badgeColor = Color(0x3310B981),
        iconColor = Color(0xFF10B981)
    ),
    GEMINI_VOICE(
        id = "gemini-3.5-flash",
        displayName = "Gemini Voice AI",
        description = "Voice model (Reads all answers aloud)",
        badge = "Voice",
        badgeColor = Color(0x331488FC),
        iconColor = Color(0xFF38BDF8)
    ),
    PICTURE_AI(
        id = "art-gen-sys",
        displayName = "Picture AI",
        description = "Generate beautiful artwork",
        badge = "Art",
        badgeColor = Color(0x33A855F7),
        iconColor = Color(0xFFA855F7)
    );

    fun getIcon(): ImageVector {
        return when (this) {
            GEMINI_FLASH -> Icons.Default.Bolt
            GEMINI_LITE -> Icons.Default.AutoAwesome
            GEMINI_VOICE -> Icons.Default.RecordVoiceOver
            PICTURE_AI -> Icons.Default.Image
        }
    }
}

enum class ImageAspectRatio(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int
) {
    SQUARE_1_1("1:1", "1:1 Square", 1024, 1024),
    YOUTUBE_16_9("16:9", "16:9 YouTube", 1344, 768),
    TIKTOK_9_16("9:16", "9:16 TikTok", 768, 1344),
    STANDARD_4_3("4:3", "4:3 Standard", 1152, 896),
    PORTRAIT_3_4("3:4", "3:4 Portrait", 896, 1152)
}

enum class AttachmentType {
    IMAGE,
    VIDEO,
    DOCUMENT,
    AUDIO
}

data class AttachmentItem(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long = 0,
    val type: AttachmentType = AttachmentType.IMAGE,
    val base64Data: String? = null
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val attachments: List<AttachmentItem> = emptyList(),
    val isSpeaking: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatSession(
    val id: String,
    val title: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val modelId: String = AiModel.GEMINI_FLASH.id
)
