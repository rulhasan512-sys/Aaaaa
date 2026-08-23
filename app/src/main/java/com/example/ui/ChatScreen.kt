package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.data.model.AttachmentType
import com.example.ui.components.AspectRatioBottomSheet
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatSidebarDrawer
import com.example.ui.components.HeroBackgroundGlow
import com.example.ui.components.HeroSection
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.ModelSelectorBottomSheet
import com.example.ui.components.TopNavBar
import com.example.ui.components.VoiceSettingsBottomSheet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val listState = rememberLazyListState()

    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val autoVoiceRead by viewModel.autoVoiceRead.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val stagedAttachments by viewModel.stagedAttachments.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorToast by viewModel.errorToast.collectAsState()
    val fullScreenImage by viewModel.fullScreenImage.collectAsState()
    val speakingMessageId by viewModel.speakingMessageId.collectAsState()

    // Sheet states
    var showModelSelector by remember { mutableStateOf(false) }
    var showAspectRatioSelector by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var showVoiceSettings by remember { mutableStateOf(false) }

    // Scroll to bottom when messages update
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Display error toast if present
    LaunchedEffect(errorToast) {
        errorToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorToast()
        }
    }

    // Speech-to-Text Recognizer Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenResults?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                val currentText = viewModel.inputText.value
                val newText = if (currentText.isBlank()) recognizedText else "$currentText $recognizedText"
                viewModel.setInputText(newText)
            }
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachmentUri(it, context, AttachmentType.IMAGE) }
    }

    // Document Picker Launcher
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachmentUri(it, context, AttachmentType.DOCUMENT) }
    }

    // Video Picker Launcher
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachmentUri(it, context, AttachmentType.VIDEO) }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            try {
                val tempFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                val uri = Uri.fromFile(tempFile)
                viewModel.addAttachmentUri(uri, context, AttachmentType.IMAGE)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatSidebarDrawer(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSelectSession = { sessionId -> viewModel.loadSession(sessionId) },
                onNewChat = { viewModel.startNewChat() },
                onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
                onClearAllHistory = { viewModel.clearAllHistory() },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                TopNavBar(
                    selectedModel = selectedModel,
                    onOpenModelSelector = { showModelSelector = true },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNewChat = { viewModel.startNewChat() },
                    onOpenVoiceSettings = { showVoiceSettings = true },
                    autoVoiceRead = autoVoiceRead
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = inputText,
                    onInputTextChanged = { viewModel.setInputText(it) },
                    stagedAttachments = stagedAttachments,
                    onRemoveAttachment = { viewModel.removeAttachment(it) },
                    selectedModel = selectedModel,
                    onOpenModelSelector = { showModelSelector = true },
                    selectedAspectRatio = selectedAspectRatio,
                    onOpenAspectRatioSelector = { showAspectRatioSelector = true },
                    autoVoiceRead = autoVoiceRead,
                    onToggleAutoVoiceRead = { viewModel.toggleAutoVoiceRead() },
                    onOpenVoiceSettings = { showVoiceSettings = true },
                    onOpenAttachmentPicker = { showAttachmentPicker = true },
                    onStartVoiceRecognition = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to RAKIB AI...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice recognition not available on device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSend = { viewModel.sendMessage() },
                    isGenerating = isGenerating,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Futuristic Glowing Background Canvas
                HeroBackgroundGlow()

                if (messages.isEmpty()) {
                    // Empty Chat: Hero Welcome Section
                    HeroSection(
                        onPromptSelected = { prompt ->
                            viewModel.setInputText(prompt)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Active Chat History
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageBubble(
                                message = message,
                                isSpeaking = speakingMessageId == message.id,
                                onSpeakToggle = { viewModel.speakMessage(message.id, message.text) },
                                onImageClick = { url -> viewModel.showFullScreenImage(url) }
                            )
                        }

                        if (isGenerating) {
                            item(key = "generating_indicator") {
                                GeneratingBubble()
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (showModelSelector) {
        ModelSelectorBottomSheet(
            selectedModel = selectedModel,
            onSelectModel = { viewModel.selectModel(it) },
            onDismiss = { showModelSelector = false }
        )
    }

    if (showAspectRatioSelector) {
        AspectRatioBottomSheet(
            selectedRatio = selectedAspectRatio,
            onSelectRatio = { viewModel.selectAspectRatio(it) },
            onDismiss = { showAspectRatioSelector = false }
        )
    }

    if (showAttachmentPicker) {
        AttachmentBottomSheet(
            onDismiss = { showAttachmentPicker = false },
            onPickCamera = { cameraLauncher.launch(null) },
            onPickGallery = { galleryLauncher.launch("image/*") },
            onPickDocument = { documentLauncher.launch("*/*") },
            onPickVideo = { videoLauncher.launch("video/*") }
        )
    }

    if (showVoiceSettings) {
        VoiceSettingsBottomSheet(
            autoVoiceRead = autoVoiceRead,
            onToggleAutoVoiceRead = { viewModel.toggleAutoVoiceRead() },
            onSetVoiceRate = { viewModel.setVoiceRate(it) },
            onSetVoicePitch = { viewModel.setVoicePitch(it) },
            onDismiss = { showVoiceSettings = false }
        )
    }

    if (fullScreenImage != null) {
        ImageViewerDialog(
            imageUrl = fullScreenImage,
            onDismiss = { viewModel.showFullScreenImage(null) }
        )
    }
}

@Composable
fun GeneratingBubble(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CrimsonLight)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CrimsonPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "RAKIB AI is thinking...",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}
