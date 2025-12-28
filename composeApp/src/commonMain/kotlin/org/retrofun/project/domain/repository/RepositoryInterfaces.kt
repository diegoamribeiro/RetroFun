package org.retrofun.project.domain.repository

import org.retrofun.project.domain.model.Game

interface GameRepository {
    suspend fun getAllGames(): List<Game>
    suspend fun getGameById(id: String): Game?
    suspend fun addGame(game: Game)
}

interface RomLoader {
    suspend fun loadRom(game: Game): ByteArray
}
