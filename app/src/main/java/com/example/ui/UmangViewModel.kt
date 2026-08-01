package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.GeminiRepository
import com.example.service.DeviceControlManager
import com.example.service.ParsedCommand
import com.example.service.SpeechRecognitionManager
import com.example.service.TTSManager
import com.example.service.VoiceCommandParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemAction: Boolean = false,
    val actionType: ActionType = ActionType.NONE,
    val actionDetail: String? = null
) {
    enum class Sender { USER, UMANG_AI }
    enum class ActionType { NONE, WHATSAPP, MULTITASK, BRIGHTNESS, VOLUME, LOCK, SCROLL, OWNER }
}

data class UmangUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val isWakeWordActive: Boolean = false,
    val rmsAudioLevel: Float = 0.2f,
    val ttsEnabled: Boolean = true,
    val apiKey: String = "",
    val showApiKeyDialog: Boolean = false,
    val showOwnerDialog: Boolean = false,
    val statusText: String = "Tap mic or say 'Hey Umang' to speak"
)

class UmangViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GeminiRepository()
    private val prefs = application.getSharedPreferences("umang_ai_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(UmangUiState())
    val uiState: StateFlow<UmangUiState> = _uiState.asStateFlow()

    private var ttsManager: TTSManager? = null
    private var speechManager: SpeechRecognitionManager? = null

    val ownerName = "Umang Rai" // Strictly non-editable permanent identity

    init {
        val savedKey = prefs.getString("gemini_api_key", null)
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" } ?: ""

        _uiState.update { it.copy(apiKey = savedKey) }

        ttsManager = TTSManager(application) { speaking ->
            _uiState.update { it.copy(isSpeaking = speaking) }
        }

        speechManager = SpeechRecognitionManager(
            context = application,
            onResult = { text ->
                _uiState.update { it.copy(statusText = "Processing command...") }
                processSpokenText(text)
            },
            onErrorMsg = { err ->
                _uiState.update { it.copy(statusText = err, isListening = false) }
            },
            onRmsAudioLevelChanged = { rms ->
                _uiState.update { it.copy(rmsAudioLevel = rms) }
            },
            onListeningStateChanged = { listening ->
                _uiState.update {
                    it.copy(
                        isListening = listening,
                        statusText = if (listening) "Listening now..." else "Tap mic or say 'Hey Umang'"
                    )
                }
            }
        )

        // Initial welcome message
        addMessage(
            ChatMessage(
                sender = ChatMessage.Sender.UMANG_AI,
                text = "Hello! I am Umang AI, your personal voice assistant. Created lovingly by Umang Rai! How can I control your phone or assist you today?"
            )
        )
        speakText("Hello! I am Umang AI, your personal voice assistant created by Umang Rai!")
    }

    fun startListening() {
        speechManager?.startListening()
    }

    fun stopListening() {
        speechManager?.stopListening()
    }

    fun toggleWakeWordMode() {
        val newActive = !_uiState.value.isWakeWordActive
        _uiState.update { it.copy(isWakeWordActive = newActive) }
        if (newActive) {
            speechManager?.startWakeWordListening()
            _uiState.update { it.copy(statusText = "Hands-free wake word active ('Hey Umang')") }
            speakText("Hands-free wake word listening is now active!")
        } else {
            speechManager?.stopListening()
            _uiState.update { it.copy(statusText = "Wake word listening stopped.") }
        }
    }

    fun toggleTts() {
        val newTts = !_uiState.value.ttsEnabled
        _uiState.update { it.copy(ttsEnabled = newTts) }
        if (!newTts) ttsManager?.stop()
    }

    fun setGeminiApiKey(key: String) {
        val cleaned = key.trim()
        prefs.edit().putString("gemini_api_key", cleaned).apply()
        _uiState.update { it.copy(apiKey = cleaned, showApiKeyDialog = false) }
        speakText("Gemini API key updated successfully!")
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun closeApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun openOwnerDialog() {
        _uiState.update { it.copy(showOwnerDialog = true) }
        speakText("My creator and sole owner is Umang Rai!")
    }

    fun closeOwnerDialog() {
        _uiState.update { it.copy(showOwnerDialog = false) }
    }

    fun processSpokenText(text: String, activity: Activity? = null) {
        if (text.isBlank()) return

        // Add user message
        addMessage(ChatMessage(sender = ChatMessage.Sender.USER, text = text))

        // Parse locally first for device control intents & privacy
        val command = VoiceCommandParser.parse(text)
        executeParsedCommand(command, activity)
    }

    private fun executeParsedCommand(command: ParsedCommand, activity: Activity?) {
        val context = getApplication<Application>().applicationContext

        when (command) {
            is ParsedCommand.OwnerInfo -> {
                val ownerReply = "I am Umang AI, and my sole creator and owner is Umang Rai! No one can change my owner name."
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = ownerReply,
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.OWNER,
                        actionDetail = "Owner: Umang Rai"
                    )
                )
                speakText(ownerReply)
            }

            is ParsedCommand.WhatsAppMessage -> {
                val result = DeviceControlManager.executeWhatsAppMessage(context, command.recipient, command.message)
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = "Executing WhatsApp action for ${command.recipient}: '${command.message}'",
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.WHATSAPP,
                        actionDetail = result
                    )
                )
                speakText("Sending message to ${command.recipient} on WhatsApp!")
            }

            is ParsedCommand.SplitScreenApps -> {
                val result = DeviceControlManager.launchSplitScreenMultitask(context, command.app1, command.app2)
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = "Opening ${command.app1} and ${command.app2} in split-screen multi-task view!",
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.MULTITASK,
                        actionDetail = result
                    )
                )
                speakText("Opening YouTube and Instagram together in split screen multi task mode!")
            }

            is ParsedCommand.BrightnessControl -> {
                val result = DeviceControlManager.adjustBrightness(activity, command.type, command.level)
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = result,
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.BRIGHTNESS,
                        actionDetail = result
                    )
                )
                speakText(result)
            }

            is ParsedCommand.VolumeControl -> {
                val result = DeviceControlManager.adjustVolume(context, command.type)
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = result,
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.VOLUME,
                        actionDetail = result
                    )
                )
                speakText(result)
            }

            is ParsedCommand.LockPhone -> {
                val result = DeviceControlManager.triggerLockPhone(context)
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = result,
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.LOCK,
                        actionDetail = result
                    )
                )
                speakText("Securing device and locking screen now!")
            }

            is ParsedCommand.ScrollAction -> {
                val textResult = "Simulated scroll ${command.direction.name.lowercase()} gesture!"
                addMessage(
                    ChatMessage(
                        sender = ChatMessage.Sender.UMANG_AI,
                        text = textResult,
                        isSystemAction = true,
                        actionType = ChatMessage.ActionType.SCROLL,
                        actionDetail = textResult
                    )
                )
                speakText("Scrolling ${command.direction.name.lowercase()}!")
            }

            is ParsedCommand.GeneralQuery -> {
                // Call Gemini API in background
                _uiState.update { it.copy(isProcessing = true, statusText = "Umang AI is thinking...") }
                viewModelScope.launch {
                    val aiResponse = repository.generateResponse(command.prompt, _uiState.value.apiKey)
                    _uiState.update { it.copy(isProcessing = false, statusText = "Tap mic or say 'Hey Umang'") }
                    addMessage(ChatMessage(sender = ChatMessage.Sender.UMANG_AI, text = aiResponse))
                    speakText(aiResponse)
                }
            }
        }
    }

    private fun addMessage(msg: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + msg) }
    }

    private fun speakText(text: String) {
        ttsManager?.speak(text, enabled = _uiState.value.ttsEnabled)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
        speechManager?.destroy()
    }
}
