package org.retrofun.project.emulation

import org.retrofun.project.domain.emulation.ControllerState
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.emulation.VideoFrame
import org.retrofun.project.emulation.nes.Buttons
import org.retrofun.project.emulation.nes.Director
import org.retrofun.project.emulation.nes.SECS_PER_FRAME

/**
 * Kotlin-based NES emulator engine using ktnes library.
 * Fully multiplatform - works on both Android and iOS!
 */
class KotlinNesEngine : EmulatorEngine {
    private var director: Director? = null
    
    override fun init(romBytes: ByteArray) {
        println("KotlinNesEngine.init: ENTRY - ROM size: ${romBytes.size}")
        
        try {
            println("KotlinNesEngine: Initializing with ROM size: ${romBytes.size} bytes")
            
            // Check ROM header
            if (romBytes.size >= 4) {
                val header = "${romBytes[0].toInt().toChar()}${romBytes[1].toInt().toChar()}${romBytes[2].toInt().toChar()}"
                println("KotlinNesEngine: ROM header: $header")
            }
            
            println("KotlinNesEngine: About to create Director...")
            // Director constructor takes ROM bytes directly
            director = Director(romBytes)
            println("KotlinNesEngine: Director created successfully!")
            
            // Run a few warmup frames to allow PPU to populate the front buffer
            // The PPU uses double-buffering and only swaps buffers during vblank
            println("KotlinNesEngine: Running warmup frames...")
            repeat(3) { frameNum ->
                director?.stepSeconds(SECS_PER_FRAME)
                val buffer = director?.videoBuffer()
                if (buffer != null && buffer.size >= 5) {
                    val sample = buffer.take(5).joinToString(", ") { "0x${it.toString(16).padStart(8, '0')}" }
                    println("KotlinNesEngine: Warmup frame $frameNum - First 5 pixels: $sample")
                }
            }
            println("Kotlin NesEngine: Warmup complete!")
            
        } catch (e: Exception) {
            println("KotlinNesEngine: *** EXCEPTION during init: ${e.message} ***")
            println("KotlinNesEngine: Exception class: ${e::class.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    override fun reset() {
        try {
            println("KotlinNesEngine: Resetting...")
            director?.reset()
        } catch (e: Exception) {
            println("KotlinNesEngine: ERROR during reset: ${e.message}")
        }
    }

    override fun setControllerState(state: ControllerState) {
        try {
            val controller = director?.controller1 ?: return
            
            // Use onButtonDown/onButtonUp based on state
            if (state.up) controller.onButtonDown(Buttons.ARROW_UP) else controller.onButtonUp(Buttons.ARROW_UP)
            if (state.down) controller.onButtonDown(Buttons.ARROW_DOWN) else controller.onButtonUp(Buttons.ARROW_DOWN)
            if (state.left) controller.onButtonDown(Buttons.ARROW_LEFT) else controller.onButtonUp(Buttons.ARROW_LEFT)
            if (state.right) controller.onButtonDown(Buttons.ARROW_RIGHT) else controller.onButtonUp(Buttons.ARROW_RIGHT)
            if (state.a) controller.onButtonDown(Buttons.BUTTON_A) else controller.onButtonUp(Buttons.BUTTON_A)
            if (state.b) controller.onButtonDown(Buttons.BUTTON_B) else controller.onButtonUp(Buttons.BUTTON_B)
            if (state.start) controller.onButtonDown(Buttons.BUTTON_START) else controller.onButtonUp(Buttons.BUTTON_START)
            if (state.select) controller.onButtonDown(Buttons.BUTTON_SELECT) else controller.onButtonUp(Buttons.BUTTON_SELECT)
        } catch (e: Exception) {
            println("KotlinNesEngine: ERROR setting controller: ${e.message}")
        }
    }

    override fun runFrame(): VideoFrame {
        val width = 256
        val height = 240
        
        try {
            // Step one frame strategy:
            // director.stepSeconds might return void or cycles depending on implementation.
            // We just need to step it.
            director?.stepSeconds(SECS_PER_FRAME)
            
            // Get frame buffer from console
            val buffer = director?.videoBuffer() ?: IntArray(width * height)
            
            return VideoFrame(width, height, buffer)
        } catch (e: Exception) {
            println("KotlinNesEngine: ERROR during runFrame: ${e.message}")
            e.printStackTrace()
            return VideoFrame(width, height, IntArray(width * height) { 0xFF000000.toInt() })
        }
    }

    override fun release() {
        println("KotlinNesEngine: Releasing resources")
        director = null
    }
    
    /**
     * Get audio samples from the NES APU
     * Returns FloatArray with audio samples ready for playback
     */
    fun getAudioSamples(): FloatArray {
        return director?.audioBuffer() ?: FloatArray(0)
    }
    
    companion object {
        private var frameCount = 0
    }
}
