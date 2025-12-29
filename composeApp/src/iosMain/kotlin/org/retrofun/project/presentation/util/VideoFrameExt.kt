@file:OptIn(ExperimentalForeignApi::class)

package org.retrofun.project.presentation.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.*
import org.retrofun.project.domain.emulation.VideoFrame
import platform.CoreGraphics.*
import platform.Foundation.NSData
import platform.Foundation.create
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorAlphaType

actual fun VideoFrame.toImageBitmap(): ImageBitmap? {
    if (pixels.isEmpty()) return null
    
    try {
        // Create Skia Image from pixels
        // Format: ARGB_8888, AlphaType: Opaque
        val imageInfo = ImageInfo.makeN32(width, height, ColorAlphaType.OPAQUE)
        
        val byteArray = ByteArray(width * height * 4)
        var offset = 0
        
        for (pixel in pixels) {
            // Extract RGB components (NES pixels have alpha=0, need to force alpha=255)
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            // Store in BGRA order (standard for Skia on iOS/Mac)
            byteArray[offset] = b.toByte()
            byteArray[offset + 1] = g.toByte()
            byteArray[offset + 2] = r.toByte()
            byteArray[offset + 3] = 0xFF.toByte() // Force alpha to 255 (fully opaque)
            
            offset += 4
        }
        
        val image = Image.makeRaster(imageInfo, byteArray, width * 4)
        return image.toComposeImageBitmap()
    } catch (e: Exception) {
        println("VideoFrameExt.toImageBitmap: ERROR - ${e.message}")
        e.printStackTrace()
        return null
    }
}
