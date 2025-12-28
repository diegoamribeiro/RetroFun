package org.retrofun.project.di

import org.koin.dsl.module
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.repository.RomLoader
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.emulation.VideoFrame
import org.retrofun.project.domain.emulation.ControllerState

class IosRomLoader : RomLoader {
    override suspend fun loadRom(game: Game): ByteArray {
        throw NotImplementedError("iOS RomLoader not implemented yet")
    }
}

class IosEmulatorEngine : EmulatorEngine {
    override fun init(romBytes: ByteArray) {}
    override fun reset() {}
    override fun setControllerState(state: ControllerState) {}
    override fun runFrame(): VideoFrame = VideoFrame(0, 0, IntArray(0))
    override fun release() {}
}

actual val platformModule = module {
    single<RomLoader> { IosRomLoader() }
    factory<EmulatorEngine> { IosEmulatorEngine() }
}
