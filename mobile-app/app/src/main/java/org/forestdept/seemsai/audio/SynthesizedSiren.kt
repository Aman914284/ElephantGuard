package org.forestdept.seemsai.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Synthesized Acoustic Siren Engine.
 * Generates dynamic dual-frequency modulated warble (800 Hz - 1300 Hz) directly
 * via native Android PCM AudioTrack, eliminating external MP3 dependencies.
 */
class SynthesizedSiren {

    private var audioTrack: AudioTrack? = null
    private var sirenJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    fun isSirenActive(): Boolean = isPlaying

    @Synchronized
    fun playEmergencyTone(durationMs: Long = 3000L) {
        if (isPlaying) return
        isPlaying = true

        sirenJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                val bufferSize = sampleRate / 10 // 100ms chunks
                val audioBuffer = ShortArray(bufferSize)
                val startTime = System.currentTimeMillis()
                var phase = 0.0

                while (isActive && isPlaying && (System.currentTimeMillis() - startTime < durationMs)) {
                    val progress = (System.currentTimeMillis() - startTime) / 1000.0
                    // Modulate frequency between 750 Hz and 1350 Hz with 2.5 Hz LFO
                    val lfo = sin(2.0 * Math.PI * 2.5 * progress)
                    val freq = 1050.0 + (300.0 * lfo)

                    for (i in 0 until bufferSize) {
                        val sample = sin(phase) * 30000.0
                        audioBuffer[i] = sample.toInt().toShort()
                        phase += 2.0 * Math.PI * freq / sampleRate
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                    }
                    track.write(audioBuffer, 0, bufferSize)
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPlaying = false
                audioTrack = null
            }
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        sirenJob?.cancel()
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore on teardown
        }
        audioTrack = null
    }
}
