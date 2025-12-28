package org.retrofun.project.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

class AudioPlayer {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100 // LaiNES standard, though NES native is slightly different
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO // NES is Mono (mostly)
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        )

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBufferSize) // Use minimum buffer for lower latency
                .build()

            audioTrack?.play()
            Log.d("AudioPlayer", "AudioTrack initialized and playing")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to initialize AudioTrack: ${e.message}")
        }
    }

    @Volatile
    private var isReleased = false

    fun write(samples: ShortArray, count: Int) {
        if (isReleased) return
        val track = audioTrack ?: return

        try {
            if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                track.play()
            }
            track.write(samples, 0, count)
        } catch (e: IllegalStateException) {
            // Track might have been released concurrently
            Log.e("AudioPlayer", "Error writing audio: ${e.message}")
        }
    }

    fun release() {
        if (isReleased) return
        isReleased = true
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error releasing AudioTrack: ${e.message}")
        }
        audioTrack = null
    }
}
