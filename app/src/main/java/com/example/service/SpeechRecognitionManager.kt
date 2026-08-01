package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognitionManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onErrorMsg: (String) -> Unit,
    private val onRmsAudioLevelChanged: (Float) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isWakeWordMode = false

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onListeningStateChanged(true)
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB (-2 to 10 dB average) to 0.0 - 1.0 range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onRmsAudioLevelChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        onListeningStateChanged(false)
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        onListeningStateChanged(false)
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, please try speaking again!"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out, tap mic to try again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record Audio permission required!"
                            else -> "Voice input error ($error)."
                        }
                        // If wake word listening mode is active, automatically restart listening loop quietly
                        if (isWakeWordMode && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                            startWakeWordListening()
                        } else {
                            onErrorMsg(msg)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        onListeningStateChanged(false)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val spokenText = matches[0]
                            if (isWakeWordMode) {
                                val lower = spokenText.lowercase()
                                if (lower.contains("umang") || lower.contains("hey umang") || lower.contains("ok umang")) {
                                    isWakeWordMode = false
                                    onResult(spokenText)
                                } else {
                                    // Resume wake word loop
                                    startWakeWordListening()
                                }
                            } else {
                                onResult(spokenText)
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partial = matches[0]
                            if (isWakeWordMode) {
                                val lower = partial.lowercase()
                                if (lower.contains("umang") || lower.contains("hey umang")) {
                                    isWakeWordMode = false
                                    onResult(partial)
                                }
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (isListening) return
        isWakeWordMode = false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Privacy-first local speech recognition!
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onErrorMsg("Failed to start voice listener: ${e.localizedMessage}")
        }
    }

    fun startWakeWordListening() {
        if (isListening) return
        isWakeWordMode = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "Wake word error: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        isWakeWordMode = false
        speechRecognizer?.stopListening()
        isListening = false
        onListeningStateChanged(false)
    }

    fun destroy() {
        speechRecognizer?.destroy()
    }
}
