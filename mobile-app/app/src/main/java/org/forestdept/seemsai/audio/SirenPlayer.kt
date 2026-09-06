package org.forestdept.seemsai.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * High-Decibel Emergency Wildlife Warning Siren Synthesizer.
 * Generates an alternating 800 Hz - 1400 Hz sweep tone using AudioTrack.
 * Automatically sustains sounding for 50 seconds upon activation.
 */
class SirenPlayer {

    private val _isSirenPlaying = MutableStateFlow(false)
    val isSirenPlaying: StateFlow<Boolean> = _isSirenPlaying.asStateFlow()

    private val _sirenCountdown = MutableStateFlow(0)
    val sirenCountdown: StateFlow<Int> = _sirenCountdown.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var sirenJob: Job? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Starts the emergency siren and sustains it for [durationSeconds] (default 10s).
     */
    fun startSiren(durationSeconds: Int = 10) {
        _sirenCountdown.value = durationSeconds
        if (_isSirenPlaying.value) {
            // Siren is already actively playing: refresh duration timer
            return
        }
        _isSirenPlaying.value = true

        // Timer Job: Decrements countdown every 1 second and auto-stops after duration
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _sirenCountdown.value > 0 && _isSirenPlaying.value) {
                delay(1000L)
                val remaining = _sirenCountdown.value - 1
                _sirenCountdown.value = remaining.coerceAtLeast(0)
                if (remaining <= 0) {
                    stopSiren()
                    break
                }
            }
        }

        // Synthesizer Audio Job
        sirenJob?.cancel()
        sirenJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val bufferSize = sampleRate / 10 // 100ms chunks
                val buffer = ShortArray(bufferSize)
                var currentFreq = 800.0
                var freqDirection = 1
                var phase = 0.0

                while (isActive && _isSirenPlaying.value) {
                    for (i in 0 until bufferSize) {
                        val angle = 2.0 * Math.PI * currentFreq / sampleRate
                        phase += angle
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        buffer[i] = (sin(phase) * 30000.0).toInt().toShort()

                        // Sweep frequency between 800Hz and 1400Hz
                        currentFreq += freqDirection * 0.05
                        if (currentFreq >= 1400.0) {
                            freqDirection = -1
                        } else if (currentFreq <= 800.0) {
                            freqDirection = 1
                        }
                    }
                    audioTrack?.write(buffer, 0, bufferSize)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSirenInternal()
            }
        }
    }

    fun stopSiren() {
        _isSirenPlaying.value = false
        _sirenCountdown.value = 0
        timerJob?.cancel()
        timerJob = null
        sirenJob?.cancel()
        sirenJob = null
        stopSirenInternal()
    }

    private fun stopSirenInternal() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
