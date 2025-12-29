@file:OptIn(ExperimentalForeignApi::class)

package org.retrofun.project.di

import kotlinx.cinterop.*
import org.koin.dsl.module
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.repository.RomLoader
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.emulation.VideoFrame
import org.retrofun.project.domain.emulation.ControllerState
import platform.Foundation.*
import platform.posix.memcpy

class IosRomLoader : RomLoader {
    override suspend fun loadRom(game: Game): ByteArray {
        val path = game.romPath
        val url = if (path.startsWith("file://")) {
            NSURL.URLWithString(path)
        } else {
            NSBundle.mainBundle.URLForResource(path.removeSuffix(".nes").removeSuffix(".gen"), withExtension = null)
        }

        val nsData = url?.let { NSData.dataWithContentsOfURL(it) }
            ?: throw IllegalStateException("Could not load ROM at path: $path")

        val length = nsData.length.toInt()
        val byteArray = ByteArray(length)
        if (length > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }
        return byteArray
    }
}

// STUB TEMPORÁRIO - C++ bridge ainda não funcional no iOS
class IosEmulatorEngine : EmulatorEngine {
    override fun init(romBytes: ByteArray) {
        println("iOS: Emulator init called with ${romBytes.size} bytes (STUB)")
    }

    override fun reset() {
        println("iOS: Emulator reset (STUB)")
    }

    override fun setControllerState(state: ControllerState) {
        // Stub - não faz nada
    }

    override fun runFrame(): VideoFrame {
        // Retorna frame preto 256x240
        return VideoFrame(256, 240, IntArray(256 * 240) { 0xFF000000.toInt() })
    }

    override fun release() {
        println("iOS: Emulator release (STUB)")
    }
}

actual val platformModule = module {
    single<RomLoader> { IosRomLoader() }
    factory<EmulatorEngine> { IosEmulatorEngine() }
}
