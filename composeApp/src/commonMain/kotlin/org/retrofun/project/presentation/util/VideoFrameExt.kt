package org.retrofun.project.presentation.util

import androidx.compose.ui.graphics.ImageBitmap
import org.retrofun.project.domain.emulation.VideoFrame

expect fun VideoFrame.toImageBitmap(): ImageBitmap?
