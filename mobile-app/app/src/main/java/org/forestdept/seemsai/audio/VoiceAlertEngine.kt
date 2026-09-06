package org.forestdept.seemsai.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Bilingual Voice Alert Engine (English / Hindi / Auto).
 */
class VoiceAlertEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    companion object {
        const val MSG_EN = "Warning. Elephant detected near N H thirty-three. Please slow down to 20 kilometers per hour."
        const val MSG_HI = "सावधान। एन एच तैंतीस के पास हाथी की गतिविधि डिटेक्ट हुई है। कृपया वाहन की गति कम करें।"
    }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)
        }
    }

    fun speakWarning(languageMode: String = "AUTO", customText: String? = null) {
        if (!isInitialized || tts == null) return

        try {
            when (languageMode.uppercase(Locale.ROOT)) {
                "HINDI" -> {
                    tts?.language = Locale("hi", "IN")
                    tts?.speak(customText ?: MSG_HI, TextToSpeech.QUEUE_FLUSH, null, "SEEMS_VOICE_HI")
                }
                "ENGLISH" -> {
                    tts?.language = Locale.US
                    tts?.speak(customText ?: MSG_EN, TextToSpeech.QUEUE_FLUSH, null, "SEEMS_VOICE_EN")
                }
                else -> { // AUTO: English followed by Hindi if supported
                    tts?.language = Locale.US
                    tts?.speak(customText ?: MSG_EN, TextToSpeech.QUEUE_FLUSH, null, "SEEMS_VOICE_EN")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
