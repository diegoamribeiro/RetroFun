package org.retrofun.project.presentation.gameplayer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.retrofun.project.domain.emulation.EmulatorEngine
import org.retrofun.project.domain.emulation.VideoFrame
import org.retrofun.project.domain.model.Game
import org.retrofun.project.domain.repository.GameRepository
import org.retrofun.project.domain.repository.RomLoader

import org.retrofun.project.domain.emulation.ControllerState

data class GamePlayerUiState(
    val isRunning: Boolean = false,
    val game: Game? = null,
    val currentFrame: VideoFrame? = null,
    val controllerState: ControllerState = ControllerState()
)

class GamePlayerScreenModel(
    private val repository: GameRepository,
    private val romLoader: RomLoader,
    private val emulatorEngine: EmulatorEngine
) : ScreenModel {

    private val _state = MutableStateFlow(GamePlayerUiState())
    val state: StateFlow<GamePlayerUiState> = _state.asStateFlow()
    
    private var loopJob: Job? = null

    fun loadGame(gameId: String) {
        if (_state.value.isRunning) return // Already running

        screenModelScope.launch {
            val game = repository.getGameById(gameId) ?: return@launch
            _state.value = _state.value.copy(game = game)
            
            try {
                val romBytes = romLoader.loadRom(game)
                emulatorEngine.init(romBytes)
                startGameLoop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun startGameLoop() {
        if (loopJob?.isActive == true) return
        
        _state.value = _state.value.copy(isRunning = true)
        println("GamePlayerScreenModel: Starting game loop")
        loopJob = screenModelScope.launch(Dispatchers.Default) {
             var frameCounter = 0
             while (isActive && _state.value.isRunning) {
                 // println("GamePlayerScreenModel: requesting frame $frameCounter")
                 val frame = emulatorEngine.runFrame()
                 if (frameCounter % 60 == 0) {
                     println("GamePlayerScreenModel: Received frame $frameCounter")
                 }
                 _state.value = _state.value.copy(currentFrame = frame)
                 frameCounter++
                 // Cap frame rate roughly for prototype (e.g. 60fps ~ 16ms)
                 // delay(16) -> REMOVED: AudioTrack.write acts as the sync clock now.
                 // If we delay, we double-wait (wait for audio + wait for timer).
             }
             println("GamePlayerScreenModel: Game loop ended")
        }
    }
    
    fun togglePause() {
        val currentlyRunning = _state.value.isRunning
        if (currentlyRunning) {
            _state.value = _state.value.copy(isRunning = false)
            loopJob?.cancel()
        } else {
            startGameLoop()
        }
    }
    
    fun resetGame() {
        emulatorEngine.reset()
    }
    
    fun onControllerInput(reducer: (ControllerState) -> ControllerState) {
        val newState = reducer(_state.value.controllerState)
        _state.value = _state.value.copy(controllerState = newState)
        emulatorEngine.setControllerState(newState)
    }

    fun stopGame() {
        _state.value = _state.value.copy(isRunning = false)
        loopJob?.cancel()
        emulatorEngine.release()
    }

    override fun onDispose() {
        stopGame()
        super.onDispose()
    }
}
