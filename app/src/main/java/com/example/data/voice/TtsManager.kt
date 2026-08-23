package com.example.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                _isTtsReady.value = true
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speakingMessageId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }
                })
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch
        tts?.setPitch(pitch)
    }

    fun speak(text: String, messageId: String) {
        if (!isInitialized || tts == null) return

        // If already speaking this message, stop it
        if (_speakingMessageId.value == messageId) {
            stop()
            return
        }

        // Clean markdown and symbols for pleasant speech
        val cleanedText = cleanMarkdownForSpeech(text)
        if (cleanedText.isBlank()) return

        // Set locale depending on script detected (Bengali vs English/Other)
        val hasBengali = cleanedText.any { it in '\u0980'..'\u09FF' }
        if (hasBengali) {
            val bengaliLocale = Locale("bn", "BD")
            val result = tts?.setLanguage(bengaliLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("bn"))
            }
        } else {
            tts?.setLanguage(Locale.US)
        }

        _speakingMessageId.value = messageId
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        tts?.stop()
        _speakingMessageId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), " Code snippet provided. ")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove images & links syntax: [title](url) -> title
            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
            // Remove bold/italics symbols
            .replace(Regex("[*_~#]"), "")
            // Clean bullets and numbers
            .replace(Regex("^[\\s]*[-*+]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("^[\\s]*\\d+\\.\\s+", RegexOption.MULTILINE), "")
            .trim()
    }
}
