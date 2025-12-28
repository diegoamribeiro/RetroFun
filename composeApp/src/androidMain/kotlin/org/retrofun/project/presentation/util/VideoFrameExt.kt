package org.retrofun.project.presentation.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.retrofun.project.domain.emulation.VideoFrame

actual fun VideoFrame.toImageBitmap(): ImageBitmap? {
    if (pixels.isEmpty()) return null
    // Create Android Bitmap
    // Note: Creating a new Bitmap every frame is expensive (garbage). 
    // Optimization would be to reuse a mutable bitmap, but for prototype this is fine.
    // Ideally VideoFrame should wrap a reused native buffer or handle.
    
    return try {
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
         e.printStackTrace()
         null
    }
}
