package org.retrofun.project.presentation.gamedetail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.repository.GameRepository

data class GameDetailUiState(
    val isLoading: Boolean = false,
    val game: Game? = null
)

class GameDetailScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(GameDetailUiState(isLoading = true))
    val state: StateFlow<GameDetailUiState> = _state.asStateFlow()

    fun loadGame(gameId: String) {
        screenModelScope.launch {
            val game = repository.getGameById(gameId)
            _state.value = GameDetailUiState(isLoading = false, game = game)
        }
    }
}
