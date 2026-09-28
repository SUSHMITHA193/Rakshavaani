package com.example.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object WavHelper {
    const val SAMPLE_RATE_16K = 16000
    const val CHANNELS_MONO = 1
    const val BITS_PER_SAMPLE_16 = 16

    fun writeWavHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        sampleRate: Long = SAMPLE_RATE_16K.toLong(),
        channels: Int = CHANNELS_MONO,
        byteRate: Long = (SAMPLE_RATE_16K * CHANNELS_MONO * BITS_PER_SAMPLE_16 / 8).toLong()
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = ((totalDataLen shr 8) and 0xffL).toByte()
        header[6] = ((totalDataLen shr 16) and 0xffL).toByte()
        header[7] = ((totalDataLen shr 24) and 0xffL).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xffL).toByte()
        header[25] = ((sampleRate shr 8) and 0xffL).toByte()
        header[26] = ((sampleRate shr 16) and 0xffL).toByte()
        header[27] = ((sampleRate shr 24) and 0xffL).toByte()
        header[28] = (byteRate and 0xffL).toByte()
        header[29] = ((byteRate shr 8) and 0xffL).toByte()
        header[30] = ((byteRate shr 16) and 0xffL).toByte()
        header[31] = ((byteRate shr 24) and 0xffL).toByte()
        header[32] = (channels * BITS_PER_SAMPLE_16 / 8).toByte() // block align
        header[33] = 0
        header[34] = BITS_PER_SAMPLE_16.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xffL).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xffL).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xffL).toByte()
        out.write(header, 0, 44)
    }

    fun updateWavHeaderSizes(wavFile: File, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        RandomAccessFile(wavFile, "rw").use { raf ->
            raf.seek(4)
            raf.write(
                byteArrayOf(
                    (totalDataLen and 0xffL).toByte(),
                    ((totalDataLen shr 8) and 0xffL).toByte(),
                    ((totalDataLen shr 16) and 0xffL).toByte(),
                    ((totalDataLen shr 24) and 0xffL).toByte()
                )
            )
            raf.seek(40)
            raf.write(
                byteArrayOf(
                    (totalAudioLen and 0xffL).toByte(),
                    ((totalAudioLen shr 8) and 0xffL).toByte(),
                    ((totalAudioLen shr 16) and 0xffL).toByte(),
                    ((totalAudioLen shr 24) and 0xffL).toByte()
                )
            )
        }
    }

    /**
     * Extracts normalized amplitude points (0.0 to 1.0) for waveform visualization.
     */
    fun extractAmplitudes(file: File, sampleCount: Int = 40): List<Float> {
        if (!file.exists() || file.length() < 44) {
            // Return synthetic realistic waveform if file is empty
            return List(sampleCount) { i ->
                val norm = i.toFloat() / sampleCount
                0.2f + 0.6f * kotlin.math.abs(sin(norm * PI * 4).toFloat())
            }
        }

        try {
            val bytes = file.readBytes()
            val pcmOffset = 44
            val pcmLength = bytes.size - pcmOffset
            if (pcmLength <= 0) return List(sampleCount) { 0.3f }

            val totalSamples = pcmLength / 2
            val step = (totalSamples / sampleCount).coerceAtLeast(1)
            val result = mutableListOf<Float>()

            for (i in 0 until sampleCount) {
                val sampleIdx = (i * step).coerceAtMost(totalSamples - 1)
                val byteIdx = pcmOffset + sampleIdx * 2
                if (byteIdx + 1 < bytes.size) {
                    val sample = (bytes[byteIdx].toInt() and 0xFF) or (bytes[byteIdx + 1].toInt() shl 8)
                    val shortVal = sample.toShort()
                    val normalized = (kotlin.math.abs(shortVal.toInt()) / 32768f).coerceIn(0.05f, 1.0f)
                    result.add(normalized)
                } else {
                    result.add(0.2f)
                }
            }
            return result
        } catch (e: Exception) {
            return List(sampleCount) { 0.3f }
        }
    }

    /**
     * Generates a realistic tactical audio demonstration WAV file with voice and acoustic noise.
     */
    fun generateTacticalDemoSample(
        context: Context,
        name: String,
        noiseType: String,
        durationSeconds: Int = 4,
        isEnhanced: Boolean = false
    ): File {
        val dir = File(context.cacheDir, "demo_samples").apply { mkdirs() }
        val targetFile = File(dir, "${name}_${if (isEnhanced) "enhanced" else "raw"}.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return targetFile
        }

        val totalSamples = SAMPLE_RATE_16K * durationSeconds
        val audioBytes = ByteArray(totalSamples * 2)
        val buffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN)

        // Generate synthetic speech format formants (F1~500Hz, F2~1500Hz, modulated speech cadence)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_16K
            val speechCadence = 0.5 * (1.0 + sin(2 * PI * 2.5 * t)) * (if (sin(2 * PI * 0.8 * t) > -0.2) 1.0 else 0.05)
            val voiceSample = (sin(2 * PI * 180 * t) * 0.4 + sin(2 * PI * 720 * t) * 0.3 + sin(2 * PI * 1800 * t) * 0.2) * speechCadence

            val noiseSample = when (noiseType.lowercase()) {
                "uav / drone", "uav" -> {
                    // Motor rotor harmonics (120Hz + 240Hz + 480Hz + blade passing turbulence)
                    sin(2 * PI * 120 * t) * 0.4 + sin(2 * PI * 240 * t) * 0.3 + sin(2 * PI * 480 * t) * 0.25 + (Math.random() * 2.0 - 1.0) * 0.2
                }
                "vehicle" -> {
                    // Low rumble 45Hz + mechanical drone
                    sin(2 * PI * 45 * t) * 0.5 + sin(2 * PI * 90 * t) * 0.3 + (Math.random() * 2.0 - 1.0) * 0.25
                }
                "wind" -> {
                    // Modulated broadband rush
                    val windGust = (0.5 + 0.5 * sin(2 * PI * 0.4 * t))
                    (Math.random() * 2.0 - 1.0) * 0.6 * windGust
                }
                "gunfire / impulsive", "gunfire" -> {
                    // Periodic sharp acoustic impulse
                    val impulseInterval = (i % (SAMPLE_RATE_16K / 2))
                    if (impulseInterval < 400) {
                        (Math.random() * 2.0 - 1.0) * 0.9
                    } else {
                        (Math.random() * 2.0 - 1.0) * 0.15
                    }
                }
                else -> {
                    // General white/pink noise
                    (Math.random() * 2.0 - 1.0) * 0.4
                }
            }

            val finalSample = if (isEnhanced) {
                // Enhanced: Speech boosted, noise suppressed by 92% (reflecting GTCRN enhancement)
                (voiceSample * 0.95 + noiseSample * 0.05).coerceIn(-1.0, 1.0)
            } else {
                // Contaminated: Speech mixed with severe acoustic interference
                (voiceSample * 0.35 + noiseSample * 0.65).coerceIn(-1.0, 1.0)
            }

            val pcmShort = (finalSample * 32767).toInt().toShort()
            buffer.putShort(pcmShort)
        }

        FileOutputStream(targetFile).use { fos ->
            writeWavHeader(
                out = fos,
                totalAudioLen = audioBytes.size.toLong(),
                totalDataLen = audioBytes.size.toLong() + 36
            )
            fos.write(audioBytes)
        }

        return targetFile
    }
}
