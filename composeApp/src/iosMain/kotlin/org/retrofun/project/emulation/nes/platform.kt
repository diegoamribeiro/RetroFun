@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.retrofun.project.emulation.nes

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

actual fun currentTimeMs(): Double {
    memScoped {
        val now = alloc<timeval>()
        gettimeofday(now.ptr, null)
        return (now.tv_sec * 1000.0) + (now.tv_usec / 1000.0)
    }
}
