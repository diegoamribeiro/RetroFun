package org.retrofun.project.data.emulation

import org.retrofun.project.data.audio.AudioPlayer

import org.retrofun.project.domain.emulation.ControllerState
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.emulation.VideoFrame

class AndroidEmulatorEngine : EmulatorEngine {

    private val audioPlayer = AudioPlayer()
    private val audioBuffer = ShortArray(4096) // Buffer for one frame's worth of audio (usually ~735 samples, but safe margin)

    companion object {
        init {
            try {
                System.loadLibrary("retrofun")
            } catch (e: UnsatisfiedLinkError) {
                println("Failed to load native library: ${e.message}")
            }
        }
    }

    // Native functions
    private external fun initNative(romBytes: ByteArray)
    private external fun runFrameNative(): IntArray? // Returns pixel array or null
    private external fun setControllerStateNative(up: Boolean, down: Boolean, left: Boolean, right: Boolean, a: Boolean, b: Boolean, start: Boolean, select: Boolean)
    private external fun getAudioSamplesNative(outBuffer: ShortArray): Int
    private external fun resetNative()
    // external fun releaseNative()

    override fun init(romBytes: ByteArray) {
        println("AndroidEmulatorEngine: init called with ${romBytes.size} bytes")
        initNative(romBytes)
    }

    override fun reset() {
        println("AndroidEmulatorEngine: reset called")
        resetNative()
    }

    override fun setControllerState(state: ControllerState) {
        setControllerStateNative(state.up, state.down, state.left, state.right, state.a, state.b, state.start, state.select)
    }

    @Volatile
    private var isReleased = false

    override fun runFrame(): VideoFrame {
        if (isReleased) {
            return VideoFrame(256, 240, IntArray(256 * 240))
        }

        val width = 256
        val height = 240
        
        // Call native
        val pixels = runFrameNative()
        if (pixels != null && pixels.isNotEmpty()) {
            // Fetch Audio
            val sampleCount = getAudioSamples(audioBuffer)
            if (sampleCount > 0) {
                audioPlayer.write(audioBuffer, sampleCount)
            }
            
            return VideoFrame(width, height, pixels)
        }
        
        // Fallback
        val fallbackPixels = IntArray(width * height) { 0xFF000000.toInt() } 
        return VideoFrame(width, height, fallbackPixels)
    }

    override fun release() {
        if (isReleased) return
        isReleased = true
        // nativeRelease()
        audioPlayer.release()
        println("AndroidEmulatorEngine: release called")
    }

    // Helper for Audio (to be called by ScreenModel or internal loop)
    fun getAudioSamples(buffer: ShortArray): Int {
        return getAudioSamplesNative(buffer)
    }
}
