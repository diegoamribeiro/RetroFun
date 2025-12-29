@file:OptIn(ExperimentalForeignApi::class)

package org.retrofun.project.data.audio

import kotlinx.cinterop.*
import platform.AVFAudio.*
import platform.Foundation.NSError
import platform.posix.usleep
import org.retrofun.project.presentation.gameplayer.AudioPlayer as AudioPlayerInterface
import kotlin.concurrent.AtomicInt

class AudioPlayer : AudioPlayerInterface {
    
    private var audioEngine: AVAudioEngine? = null
    private var sourceNode: AVAudioSourceNode? = null
    private val sampleRate = 48000.0
    
    // Ring Buffer Constants
    private val bufferSize = 4096 * 4 
    private val buffer = FloatArray(bufferSize)
    
    private val writePos = AtomicInt(0)
    private val readPos = AtomicInt(0)
    
    private var isRunning = false
    
    init {
        initAudioEngine()
    }
    
    private fun initAudioEngine() {
        try {
            audioEngine = AVAudioEngine()
            
            val session = platform.AVFAudio.AVAudioSession.sharedInstance()
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                session.setCategory(AVAudioSessionCategoryPlayback, error = errorPtr.ptr)
                session.setActive(true, error = errorPtr.ptr)
            }
            
            val format = AVAudioFormat(
                commonFormat = AVAudioPCMFormatFloat32,
                sampleRate = sampleRate,
                channels = 1u,
                interleaved = false
            ) ?: return
            
            // Raw Pointer Implementation to avoid AudioBuffer type resolution issues
            val renderBlock: AVAudioSourceNodeRenderBlock = { _, _, frameCount, audioBufferListPtr ->
                 if (audioBufferListPtr != null) {
                     val ptr = audioBufferListPtr!!
                     
                     // Use raw address arithmetic to avoid CPointer nullability issues
                     // Suppress deprecation for toLong()/toCPointer() if needed, but it's the safest way to just "add 16"
                     val baseAddr = ptr.toLong()
                     val mDataAddr = baseAddr + 16L
                     
                     // Convert back to pointer (CPointer<ByteVar>)
                     // mDataAddr points to the `void* mData` field.
                     val mDataPtrLoc = mDataAddr.toCPointer<ByteVar>()
                     
                     if (mDataPtrLoc != null) {
                         // Cast to Float**
                         val mDataPtrPtr = mDataPtrLoc.reinterpret<CPointerVar<FloatVar>>()
                         
                         // Read value
                         val outputData = mDataPtrPtr.pointed.value
                         
                         if (outputData != null) {
                             var framesWritten = 0u
                             val framesToRead = frameCount
                             
                             var rp = readPos.value
                             val wp = writePos.value
                             
                             for (i in 0u until framesToRead) {
                                 if (rp < wp) {
                                     val sample = buffer[rp % bufferSize]
                                     outputData[i.toInt()] = sample
                                     framesWritten++
                                     rp++
                                 } else {
                                     outputData[i.toInt()] = 0.0f
                                 }
                             }
                             
                             readPos.value = rp 
                         }
                     }
                 }
                 0 // noErr
            }
            
            sourceNode = AVAudioSourceNode(renderBlock)
            val engine = audioEngine ?: return
            val src = sourceNode ?: return
            
            engine.attachNode(src)
            engine.connect(src, engine.mainMixerNode, format)
            
            var error: NSError? = null
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                engine.startAndReturnError(errorPtr.ptr)
            }
            
            isRunning = true
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun write(samples: FloatArray) {
        if (!isRunning) return
        
        var offset = 0
        var remaining = samples.size
        
        while (remaining > 0) {
            val wp = writePos.value
            val rp = readPos.value
            val used = wp - rp
            val free = bufferSize - used
            
            if (free <= 0) {
                // Buffer full - block
                usleep(1000u)
                continue
            }
            
            val toWrite = minOf(free, remaining)
            
            for (i in 0 until toWrite) {
                buffer[(wp + i) % bufferSize] = samples[offset + i]
            }
            
            writePos.addAndGet(toWrite) // Commit write
            
            offset += toWrite
            remaining -= toWrite
        }
    }
    
    override fun release() {
        isRunning = false
        audioEngine?.stop()
        audioEngine = null
        sourceNode = null
    }
}

fun minOf(a: Int, b: Int): Int = if (a <= b) a else b
