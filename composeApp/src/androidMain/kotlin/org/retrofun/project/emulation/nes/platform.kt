package org.retrofun.project.emulation.nes

actual fun currentTimeMs(): Double {
    return System.currentTimeMillis().toDouble()
}
