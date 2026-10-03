package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class GameSoundEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var engineJob: Job? = null
    private var engineAudioTrack: AudioTrack? = null
    private var isPlayingEngine = false

    @Volatile
    var engineRpm: Float = 900f
    @Volatile
    var engineLoad: Float = 0.2f
    @Volatile
    var soundEnabled: Boolean = true

    private val sampleRate = 22050
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 4)

    fun startEngineSound(scope: CoroutineScope) {
        if (isPlayingEngine) return
        isPlayingEngine = true

        engineJob = scope.launch(Dispatchers.Default) {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                engineAudioTrack = track
                track.play()

                val buffer = ShortArray(1024)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0

                while (isActive && isPlayingEngine) {
                    if (!soundEnabled) {
                        Thread.sleep(50)
                        continue
                    }

                    // Map RPM to diesel fundamental frequency (idle ~30Hz to ~120Hz at 2800 RPM)
                    val freq = 28.0 + (engineRpm - 700.0) * 0.035
                    val harmonicFreq = freq * 2.0
                    val turboWhineFreq = 800.0 + (engineRpm * 0.4)

                    val load = engineLoad.coerceIn(0.1f, 1.0f)
                    val baseVol = (0.35f + load * 0.45f) * 16000f
                    val turboVol = (load * 0.15f) * 8000f

                    for (i in buffer.indices) {
                        val s1 = sin(phase1)
                        val s2 = sin(phase2) * 0.6
                        val s3 = sin(phase3) * (turboVol / 16000f)

                        val sample = ((s1 + s2) * baseVol + s3 * turboVol).toInt().coerceIn(-32767, 32767)
                        buffer[i] = sample.toShort()

                        phase1 += 2.0 * PI * freq / sampleRate
                        if (phase1 > 2.0 * PI) phase1 -= 2.0 * PI

                        phase2 += 2.0 * PI * harmonicFreq / sampleRate
                        if (phase2 > 2.0 * PI) phase2 -= 2.0 * PI

                        phase3 += 2.0 * PI * turboWhineFreq / sampleRate
                        if (phase3 > 2.0 * PI) phase3 -= 2.0 * PI
                    }

                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio track exception handled gracefully
            }
        }
    }

    fun stopEngineSound() {
        isPlayingEngine = false
        engineJob?.cancel()
        engineJob = null
        try {
            engineAudioTrack?.pause()
            engineAudioTrack?.flush()
            engineAudioTrack?.release()
        } catch (_: Exception) {}
        engineAudioTrack = null
    }

    // Melodic Bangladesh Bus Air Horn!
    fun playHorn(hornType: Int = 1) {
        if (!soundEnabled) return
        vibrate(80)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Bangladesh hydraulic & melody horn notes
                val noteDurations = when (hornType) {
                    2 -> listOf(Pair(440.0, 120), Pair(554.3, 120), Pair(659.2, 280)) // Hydraulic 3-tone
                    3 -> listOf(Pair(392.0, 90), Pair(440.0, 90), Pair(523.2, 110), Pair(659.2, 110), Pair(784.0, 320)) // Melody fanfare
                    4 -> listOf(Pair(349.2, 110), Pair(440.0, 110), Pair(523.2, 140), Pair(698.4, 350)) // Royal coach
                    else -> listOf(Pair(370.0, 220), Pair(493.8, 300)) // Classic twin-trumpet highway horn
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                for ((freq, durationMs) in noteDurations) {
                    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                    val chunk = ShortArray(numSamples)
                    var phase = 0.0
                    var overtonePhase = 0.0

                    for (i in 0 until numSamples) {
                        val envelope = if (i < 200) i / 200f else if (i > numSamples - 400) (numSamples - i) / 400f else 1.0f
                        val mainTone = sin(phase)
                        val overtone = sin(overtonePhase) * 0.4
                        val combined = ((mainTone + overtone) * 22000 * envelope).toInt().coerceIn(-32767, 32767)
                        chunk[i] = combined.toShort()

                        phase += 2.0 * PI * freq / sampleRate
                        overtonePhase += 2.0 * PI * (freq * 1.5) / sampleRate
                    }
                    track.write(chunk, 0, numSamples)
                }

                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    // Air brake release hiss ("pssshh")
    fun playAirBrakeHiss() {
        if (!soundEnabled) return
        vibrate(30)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val durationMs = 280
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val chunk = ShortArray(numSamples)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                var rng = 1234567L
                for (i in 0 until numSamples) {
                    // White noise with exponential decay envelope
                    rng = (rng * 1103515245L + 12345L) and 0x7FFFFFFF
                    val noise = (rng.toDouble() / 0x7FFFFFFF) * 2.0 - 1.0
                    val envelope = (1.0 - (i.toDouble() / numSamples))
                    chunk[i] = (noise * envelope * 18000).toInt().toShort()
                }

                track.write(chunk, 0, numSamples)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    // Indicator relay click ("tick" / "tock")
    fun playIndicatorClick(high: Boolean) {
        if (!soundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val freq = if (high) 1400.0 else 950.0
                val durationMs = 18
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val chunk = ShortArray(numSamples)
                var phase = 0.0

                for (i in 0 until numSamples) {
                    val env = 1.0 - (i.toDouble() / numSamples)
                    chunk[i] = (sin(phase) * env * 12000).toInt().toShort()
                    phase += 2.0 * PI * freq / sampleRate
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                track.write(chunk, 0, numSamples)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    // Collision crash sound
    fun playCrash() {
        vibrate(350)
        if (!soundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val durationMs = 380
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val chunk = ShortArray(numSamples)
                var rng = 987654321L

                for (i in 0 until numSamples) {
                    rng = (rng * 1664525L + 1013904223L) and 0x7FFFFFFF
                    val noise = (rng.toDouble() / 0x7FFFFFFF) * 2.0 - 1.0
                    val lowRumble = sin(2.0 * PI * 65.0 * i / sampleRate) * 0.8
                    val env = (1.0 - (i.toDouble() / numSamples)) * (1.0 - (i.toDouble() / numSamples))
                    chunk[i] = ((noise * 0.7 + lowRumble * 0.5) * env * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                track.write(chunk, 0, numSamples)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    // Passenger bell chime ("ding dong")
    fun playChime() {
        if (!soundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val notes = listOf(Pair(880.0, 160), Pair(1174.66, 260))
                val totalMs = 420
                val totalSamples = (sampleRate * (totalMs / 1000.0)).toInt()
                val chunk = ShortArray(totalSamples)

                var offset = 0
                for ((freq, dur) in notes) {
                    val noteSamples = (sampleRate * (dur / 1000.0)).toInt()
                    var phase = 0.0
                    for (i in 0 until noteSamples) {
                        val env = (1.0 - (i.toDouble() / noteSamples))
                        val s = (sin(phase) * env * 16000).toInt().toShort()
                        if (offset + i < totalSamples) {
                            chunk[offset + i] = s
                        }
                        phase += 2.0 * PI * freq / sampleRate
                    }
                    offset += noteSamples
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                track.write(chunk, 0, totalSamples)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    private fun vibrate(millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }
}
