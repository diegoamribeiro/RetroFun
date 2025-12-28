package org.retrofun.project.data.repository

import org.retrofun.project.domain.model.ConsoleType
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.model.RomSourceType
import org.retrofun.project.domain.repository.GameRepository

class GameRepositoryImpl : GameRepository {
    private val games = mutableListOf(
        Game(
            id = "demo_nes",
            name = "Demo NES Game",
            romPath = "roms/demo_nes_game.nes",
            console = ConsoleType.NES,
            romSourceType = RomSourceType.INTERNAL
        )
    )

    override suspend fun getAllGames(): List<Game> {
        return games.toList()
    }

    override suspend fun getGameById(id: String): Game? {
        return games.find { it.id == id }
    }

    override suspend fun addGame(game: Game) {
        games.add(game)
    }
}
