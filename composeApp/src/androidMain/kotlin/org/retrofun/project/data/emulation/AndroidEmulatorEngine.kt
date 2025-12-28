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
    private external fun initNative(romBytes: ByteArray, consoleType: Int)
    private external fun runFrameNative(): IntArray? // Returns pixel array or null
    private external fun setControllerStateNative(up: Boolean, down: Boolean, left: Boolean, right: Boolean, a: Boolean, b: Boolean, start: Boolean, select: Boolean)
    private external fun getAudioSamplesNative(outBuffer: ShortArray): Int
    private external fun resetNative()
    // external fun releaseNative()

    override fun init(romBytes: ByteArray) {
        // Auto-detect console based on some logic or pass it in. 
        // For now, let's assume we can intuit it, but actually the Engine interface doesn't strictly have 'consoleType'.
        // We should update the interface or just autodetection.
        // Wait, 'AndroidEmulatorEngine' implements 'EmulatorEngine'. 'EmulatorEngine.init' only takes bytes.
        // We need to change the interface 'EmulatorEngine' to accept a Game object or console type,
        // OR we detect it from the bytes headers (Magic numbers).
        // NES header: "NES"
        // Genesis header: "SEGA"
        
        // Let's implement header detection in Kotlin layer for simplicity before passing to native.
        val consoleType = detectConsole(romBytes)
        println("AndroidEmulatorEngine: init called with ${romBytes.size} bytes. Detected: $consoleType")
        
        // Configure AudioPlayer based on console
        // NES (0) is Mono, Genesis (1) is Stereo
        val isStereo = (consoleType == 1)
        audioPlayer.configure(isStereo)
        
        initNative(romBytes, consoleType)
    }
    
    private fun detectConsole(bytes: ByteArray): Int {
        if (bytes.size < 4) return 0 // Default to NES
        // Check for iNES header "NES\x1a"
        if (bytes[0] == 0x4E.toByte() && bytes[1] == 0x45.toByte() && bytes[2] == 0x53.toByte() && bytes[3] == 0x1A.toByte()) {
            return 0 // NES
        }
        // Check for Sega header (usually at 0x100)
        // "SEGA GENESIS" or "SEGA MEGA DRIVE" usually appears.
        // Simplified check: If not NES, assume GENESIS for now or add better check.
        // Let's return 1 for Genesis if not NES.
        return 1
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
