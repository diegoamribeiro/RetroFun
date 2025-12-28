package org.retrofun.project.data.loader

import android.content.Context
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.model.RomSourceType
import org.retrofun.project.domain.repository.RomLoader
import java.io.IOException

class AndroidRomLoader(
    private val context: Context
) : RomLoader {
    override suspend fun loadRom(game: Game): ByteArray {
        return when (game.romSourceType) {
            RomSourceType.INTERNAL -> {
                try {
                    context.assets.open(game.romPath).use { it.readBytes() }
                } catch (e: IOException) {
                    e.printStackTrace()
                    ByteArray(0)
                }
            }
            RomSourceType.USER_FILE -> {
                try {
                    val uri = android.net.Uri.parse(game.romPath)
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IOException("Could not open input stream for $uri")
                } catch (e: Exception) {
                    e.printStackTrace()
                    ByteArray(0)
                }
            }
        }
    }
}
