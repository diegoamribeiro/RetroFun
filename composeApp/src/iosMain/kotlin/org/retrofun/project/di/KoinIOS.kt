@file:OptIn(ExperimentalForeignApi::class)

package org.retrofun.project.di

import kotlinx.cinterop.*
import org.koin.dsl.module
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.repository.RomLoader
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.emulation.KotlinNesEngine
import platform.Foundation.*
import platform.posix.memcpy

class IosRomLoader : RomLoader {
    override suspend fun loadRom(game: Game): ByteArray {
        println("IosRomLoader: Loading ROM for game: ${game.name}")
        println("IosRomLoader: game.romPath = ${game.romPath}")
        
        // game.name contains file:// URL but may be missing extension
        // game.romPath contains the complete filename with extension
        // We need to combine them: use directory from game.name + filename from game.romPath
        
        val url = if (game.name.startsWith("file://")) {
            println("IosRomLoader: Reconstructing file:// URL with proper extension")
            
            // Extract directory from game.name and combine with game.romPath
            val urlString = game.name.substringBeforeLast("/") + "/" + game.romPath.encodeURLComponent()
            println("IosRomLoader: Constructed URL string: $urlString")
            
            NSURL.URLWithString(urlString)
        } else {
            println("IosRomLoader: Loading from bundle: ${game.romPath}")
            NSBundle.mainBundle.URLForResource(
                game.romPath.removeSuffix(".nes").removeSuffix(".gen"), 
                withExtension = null
            )
        }

        println("IosRomLoader: URL = $url")
        
        val nsData = url?.let { NSData.dataWithContentsOfURL(it) }
            ?: throw IllegalStateException("Could not load ROM at URL: $url (original path: ${game.name})")

        println("IosRomLoader: ROM data loaded, size: ${nsData.length} bytes")
        
        val length = nsData.length.toInt()
        val byteArray = ByteArray(length)
        if (length > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }
        
        println("IosRomLoader: Returning ByteArray of size ${byteArray.size}")
        return byteArray
    }
    
    private fun String.encodeURLComponent(): String {
        return this.replace(" ", "%20")
            .replace("(", "%28")
            .replace(")", "%29")
    }
}

actual val platformModule = module {
    single<RomLoader> { IosRomLoader() }
    factory<EmulatorEngine> { KotlinNesEngine() }
    single<org.retrofun.project.presentation.gameplayer.AudioPlayer> { org.retrofun.project.data.audio.AudioPlayer() }
}
