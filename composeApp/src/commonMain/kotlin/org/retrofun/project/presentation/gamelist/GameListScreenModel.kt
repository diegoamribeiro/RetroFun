package org.retrofun.project.presentation.gamelist

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.retrofun.project.domain.model.ConsoleType
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.model.RomSourceType
import org.retrofun.project.domain.repository.GameRepository

data class GameListUiState(
    val isLoading: Boolean = false,
    val games: List<Game> = emptyList()
)

class GameListScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(GameListUiState(isLoading = true))
    val state: StateFlow<GameListUiState> = _state.asStateFlow()

    init {
        loadGames()
    }

    fun addGame(name: String, path: String) {
        screenModelScope.launch {
            println("GameListScreenModel: Adding game: name=$name, path=$path")
            
            // Try to deduce extension from name first, as URI might not have it clear
            val extension = name.lowercase().substringAfterLast('.', "")
            val console = when {
                extension in listOf("sfc", "smc") -> ConsoleType.SNES
                extension == "nes" -> ConsoleType.NES
                // Fallback or explicit check
                else -> ConsoleType.NES 
            }
            
            println("GameListScreenModel: Detected console: $console")
            
            val newGame = Game(
                id = path.hashCode().toString(),
                name = name.removeSuffix(".$extension"),
                romPath = path,
                console = console,
                romSourceType = RomSourceType.USER_FILE
            )
            repository.addGame(newGame)
            loadGames()
        }
    }
    
    private fun loadGames() {
        screenModelScope.launch {
            val games = repository.getAllGames()
            _state.value = GameListUiState(isLoading = false, games = games)
        }
    }
}
