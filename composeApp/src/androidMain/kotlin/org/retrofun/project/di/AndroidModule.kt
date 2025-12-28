package org.retrofun.project.di

import org.koin.dsl.module
import org.retrofun.project.data.emulation.AndroidEmulatorEngine
import org.retrofun.project.data.loader.AndroidRomLoader
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.repository.RomLoader

actual val platformModule = module {
    single<RomLoader> { AndroidRomLoader(get()) }
    factory<EmulatorEngine> { AndroidEmulatorEngine() }
}
