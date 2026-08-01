package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(context: Context, private val onSpeakingStateChanged: (Boolean) -> Unit) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                // Make voice 10000x cute: raise pitch to 1.3f and rate to 1.05f for pleasant persona
                tts?.setPitch(1.3f)
                tts?.setSpeechRate(1.02f)
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }
            })
        } else {
            Log.e("TTSManager", "TTS Initialization failed")
        }
    }

    fun speak(text: String, enabled: Boolean = true) {
        if (!enabled || !isInitialized) return
        stop()
        val cleanedText = text.replace(Regex("[*#_~`]"), "")
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "UMANG_TTS_" + System.currentTimeMillis())
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            onSpeakingStateChanged(false)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
