package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiInlineData
import com.example.data.api.GeminiPart
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSessionEntity
import com.example.data.model.AiModel
import com.example.data.model.AttachmentItem
import com.example.data.model.AttachmentType
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.ImageAspectRatio
import com.example.data.voice.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val chatDao = database.chatDao()
    val ttsManager = TtsManager(application)

    private val _currentSessionId = MutableStateFlow<String>(UUID.randomUUID().toString())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _selectedModel = MutableStateFlow<AiModel>(AiModel.GEMINI_FLASH)
    val selectedModel: StateFlow<AiModel> = _selectedModel.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow<ImageAspectRatio>(ImageAspectRatio.SQUARE_1_1)
    val selectedAspectRatio: StateFlow<ImageAspectRatio> = _selectedAspectRatio.asStateFlow()

    private val _autoVoiceRead = MutableStateFlow<Boolean>(false)
    val autoVoiceRead: StateFlow<Boolean> = _autoVoiceRead.asStateFlow()

    private val _inputText = MutableStateFlow<String>("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _stagedAttachments = MutableStateFlow<List<AttachmentItem>>(emptyList())
    val stagedAttachments: StateFlow<List<AttachmentItem>> = _stagedAttachments.asStateFlow()

    private val _isGenerating = MutableStateFlow<Boolean>(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorToast = MutableStateFlow<String?>(null)
    val errorToast: StateFlow<String?> = _errorToast.asStateFlow()

    private val _fullScreenImage = MutableStateFlow<String?>(null)
    val fullScreenImage: StateFlow<String?> = _fullScreenImage.asStateFlow()

    val speakingMessageId: StateFlow<String?> = ttsManager.speakingMessageId

    // All chat sessions from Room DB
    val sessions: StateFlow<List<ChatSession>> = chatDao.getAllSessions()
        .combine(MutableStateFlow(Unit)) { entities, _ ->
            entities.map {
                ChatSession(
                    id = it.id,
                    title = it.title,
                    updatedAt = it.updatedAt,
                    modelId = it.modelId
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active messages from Room DB for current session
    private val _dbMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _dbMessages.asStateFlow()

    init {
        viewModelScope.launch {
            _currentSessionId.collect { sessionId ->
                chatDao.getMessagesForSession(sessionId).collect { entities ->
                    _dbMessages.value = entities.map { entity ->
                        val attachmentsList = deserializeAttachments(entity.attachmentJson)
                        ChatMessage(
                            id = entity.id,
                            sessionId = entity.sessionId,
                            role = entity.role,
                            text = entity.text,
                            timestamp = entity.timestamp,
                            imageUrl = entity.imageUrl,
                            attachments = attachmentsList
                        )
                    }
                }
            }
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun selectModel(model: AiModel) {
        _selectedModel.value = model
        if (model == AiModel.GEMINI_VOICE) {
            _autoVoiceRead.value = true
        }
    }

    fun selectAspectRatio(ratio: ImageAspectRatio) {
        _selectedAspectRatio.value = ratio
    }

    fun toggleAutoVoiceRead() {
        _autoVoiceRead.value = !_autoVoiceRead.value
        if (!_autoVoiceRead.value) {
            ttsManager.stop()
        }
    }

    fun setVoiceRate(rate: Float) {
        ttsManager.setSpeechRate(rate)
    }

    fun setVoicePitch(pitch: Float) {
        ttsManager.setSpeechPitch(pitch)
    }

    fun showFullScreenImage(url: String?) {
        _fullScreenImage.value = url
    }

    fun clearErrorToast() {
        _errorToast.value = null
    }

    fun addAttachmentUri(uri: Uri, context: Context, forceType: AttachmentType? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var fileName = "attachment"
                var fileSize: Long = 0
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val type = forceType ?: when {
                    mimeType.startsWith("image/") -> AttachmentType.IMAGE
                    mimeType.startsWith("video/") -> AttachmentType.VIDEO
                    mimeType.startsWith("audio/") -> AttachmentType.AUDIO
                    else -> AttachmentType.DOCUMENT
                }

                val item = AttachmentItem(
                    uri = uri,
                    name = fileName,
                    mimeType = mimeType,
                    sizeBytes = fileSize,
                    type = type
                )

                _stagedAttachments.value = _stagedAttachments.value + item
            } catch (e: Exception) {
                _errorToast.value = "Failed to load attachment: ${e.localizedMessage}"
            }
        }
    }

    fun removeAttachment(item: AttachmentItem) {
        _stagedAttachments.value = _stagedAttachments.value.filter { it != item }
    }

    fun clearAttachments() {
        _stagedAttachments.value = emptyList()
    }

    fun startNewChat() {
        ttsManager.stop()
        _currentSessionId.value = UUID.randomUUID().toString()
        _stagedAttachments.value = emptyList()
        _inputText.value = ""
    }

    fun loadSession(sessionId: String) {
        ttsManager.stop()
        _currentSessionId.value = sessionId
        _stagedAttachments.value = emptyList()
        _inputText.value = ""
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteMessagesForSession(sessionId)
            chatDao.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                startNewChat()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            ttsManager.stop()
            chatDao.clearAllMessages()
            chatDao.clearAllSessions()
            startNewChat()
        }
    }

    fun speakMessage(messageId: String, text: String) {
        ttsManager.speak(text, messageId)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val currentAttachments = _stagedAttachments.value
        val model = _selectedModel.value
        val sessionId = _currentSessionId.value

        if (text.isBlank() && currentAttachments.isEmpty()) return

        // Clear input and attachments
        _inputText.value = ""
        _stagedAttachments.value = emptyList()
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val userMsgId = UUID.randomUUID().toString()
            val sessionTitle = if (text.isNotBlank()) {
                if (text.length > 30) text.take(30) + "..." else text
            } else {
                "Attachment (${currentAttachments.size})"
            }

            // Save/update session in DB
            chatDao.insertSession(
                ChatSessionEntity(
                    id = sessionId,
                    title = sessionTitle,
                    updatedAt = System.currentTimeMillis(),
                    modelId = model.id
                )
            )

            // Save user message in DB
            val attachmentJson = serializeAttachments(currentAttachments)
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = userMsgId,
                    sessionId = sessionId,
                    role = "user",
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    attachmentJson = attachmentJson
                )
            )

            if (model == AiModel.PICTURE_AI) {
                handlePictureGeneration(sessionId, text)
            } else {
                handleGeminiTextGeneration(sessionId, text, currentAttachments, model)
            }
        }
    }

    private suspend fun handlePictureGeneration(sessionId: String, prompt: String) {
        val nsfwRegex = Regex("\\b(sex|sexy|nude|naked|porn|nsfw|boobs|erotic|ন্যংটা|উলঙ্গ|সেক্সি)\\b", RegexOption.IGNORE_CASE)
        if (nsfwRegex.containsMatchIn(prompt)) {
            val errorMsgId = UUID.randomUUID().toString()
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = errorMsgId,
                    sessionId = sessionId,
                    role = "model",
                    text = "দুঃখিত, এই ধরনের ছবি তৈরি করা নিষেধ। (Sorry, generating inappropriate content is restricted.)",
                    timestamp = System.currentTimeMillis()
                )
            )
            _isGenerating.value = false
            return
        }

        val imageUrl = GeminiApiClient.buildPictureAiUrl(prompt, _selectedAspectRatio.value)
        val aiMsgId = UUID.randomUUID().toString()

        chatDao.insertMessage(
            ChatMessageEntity(
                id = aiMsgId,
                sessionId = sessionId,
                role = "model",
                text = "✨ Image generated for: \"$prompt\"",
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUrl
            )
        )

        _isGenerating.value = false

        if (_autoVoiceRead.value) {
            withContext(Dispatchers.Main) {
                ttsManager.speak("Here is your generated image for $prompt", aiMsgId)
            }
        }
    }

    private suspend fun handleGeminiTextGeneration(
        sessionId: String,
        prompt: String,
        attachments: List<AttachmentItem>,
        model: AiModel
    ) {
        val context = getApplication<Application>()
        val partsList = mutableListOf<GeminiPart>()

        // Process attachments to base64
        for (att in attachments) {
            val base64Data = GeminiApiClient.uriToBase64(context, att.uri, att.mimeType)
            if (base64Data != null) {
                partsList.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = att.mimeType,
                            data = base64Data
                        )
                    )
                )
            }
        }

        if (prompt.isNotBlank()) {
            partsList.add(GeminiPart(text = prompt))
        }

        // Build conversation history from current session
        val pastMessages = _dbMessages.value
        val historyContents = mutableListOf<GeminiContent>()

        // Take last 8 messages for context window management
        val recentHistory = pastMessages.takeLast(8)
        for (msg in recentHistory) {
            if (msg.role == "user" && msg.text.isNotBlank()) {
                historyContents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            } else if (msg.role == "model" && msg.text.isNotBlank() && msg.imageUrl == null) {
                historyContents.add(
                    GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // Add current turn
        historyContents.add(
            GeminiContent(
                role = "user",
                parts = partsList
            )
        )

        val targetModel = if (model == AiModel.GEMINI_VOICE) "gemini-3.5-flash" else model.id
        val result = GeminiApiClient.executeGenerateContent(
            modelName = targetModel,
            history = historyContents
        )

        val aiMsgId = UUID.randomUUID().toString()

        result.onSuccess { aiResponseText ->
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = aiMsgId,
                    sessionId = sessionId,
                    role = "model",
                    text = aiResponseText,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Auto read aloud if voice model or autoVoiceRead is enabled
            if (_autoVoiceRead.value || model == AiModel.GEMINI_VOICE) {
                withContext(Dispatchers.Main) {
                    ttsManager.speak(aiResponseText, aiMsgId)
                }
            }
        }.onFailure { error ->
            val errorText = "⚠️ Error: ${error.localizedMessage ?: "Failed to generate AI response."}\n\nPlease check your internet connection or Gemini API key."
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = aiMsgId,
                    sessionId = sessionId,
                    role = "model",
                    text = errorText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _isGenerating.value = false
    }

    private fun serializeAttachments(attachments: List<AttachmentItem>): String? {
        if (attachments.isEmpty()) return null
        val array = JSONArray()
        for (att in attachments) {
            val obj = JSONObject().apply {
                put("uri", att.uri.toString())
                put("name", att.name)
                put("mimeType", att.mimeType)
                put("sizeBytes", att.sizeBytes)
                put("type", att.type.name)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeAttachments(json: String?): List<AttachmentItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<AttachmentItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AttachmentItem(
                        uri = Uri.parse(obj.getString("uri")),
                        name = obj.getString("name"),
                        mimeType = obj.getString("mimeType"),
                        sizeBytes = obj.optLong("sizeBytes", 0),
                        type = AttachmentType.valueOf(obj.optString("type", AttachmentType.IMAGE.name))
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
