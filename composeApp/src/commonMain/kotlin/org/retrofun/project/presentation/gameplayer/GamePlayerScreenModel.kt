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

interface AudioPlayer {
    fun write(samples: FloatArray)
    fun release()
}

class GamePlayerScreenModel(
    private val repository: GameRepository,
    private val romLoader: RomLoader,
    private val emulatorEngine: EmulatorEngine,
    private val audioPlayer: AudioPlayer
) : ScreenModel {

    private val _state = MutableStateFlow(GamePlayerUiState())
    val state: StateFlow<GamePlayerUiState> = _state.asStateFlow()
    
    private var loopJob: Job? = null

    fun loadGame(gameId: String) {
        println(">>> GamePlayerScreenModel.loadGame() ENTRY - gameId: $gameId")
        
        if (_state.value.isRunning) {
            println(">>> GamePlayerScreenModel.loadGame() - SKIPPED (already running)")
            return // Already running
        }

        println(">>> GamePlayerScreenModel.loadGame() - launching coroutine...")
        screenModelScope.launch {
            println(">>> GamePlayerScreenModel.loadGame() - inside coroutine, getting game by ID...")
            val game = repository.getGameById(gameId)
            
            if (game == null) {
                println(">>> GamePlayerScreenModel.loadGame() - GAME NOT FOUND!")
                return@launch
            }
            
            println(">>> GamePlayerScreenModel.loadGame() - Found game: ${game.name}")
            _state.value = _state.value.copy(game = game)
            
            try {
                println("GamePlayerScreenModel: Loading ROM for game: ${game.name}")
                val romBytes = romLoader.loadRom(game)
                println("GamePlayerScreenModel: ROM loaded, size: ${romBytes.size} bytes")
                println("GamePlayerScreenModel: Calling emulatorEngine.init()...")
                emulatorEngine.init(romBytes)
                println("GamePlayerScreenModel: emulatorEngine.init() completed successfully!")
                
                // Set running state to true BEFORE starting the loop
                _state.value = _state.value.copy(isRunning = true)
                println("GamePlayerScreenModel: State set to RUNNING, now starting game loop...")
                
                startGameLoop()
            } catch (e: Exception) {
                println("GamePlayerScreenModel: *** EXCEPTION during ROM load/init: ${e.message} ***")
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
                 
                 // Get and play audio samples (if KotlinNesEngine)
                 if (emulatorEngine is org.retrofun.project.emulation.KotlinNesEngine) {
                     val audioSamples = emulatorEngine.getAudioSamples()
                     if (audioSamples.isNotEmpty()) {
                         audioPlayer.write(audioSamples)
                     }
                 }
                 
                 frameCounter++
                 // Cap frame rate roughly for prototype (e.g. 60fps ~ 16ms)
                 // delay(16) -> REMOVED: AudioTrack.write acts as the sync clock now.
                 // If we delay, we double-wait (wait for audio + wait for timer).
             }
             println("GamePlayerScreenModel: Game loop ended")
        }
    }
    
    fun togglePause() {
        println(">>> togglePause() called, currentlyRunning: ${_state.value.isRunning}")
        
        val currentlyRunning = _state.value.isRunning
        if (currentlyRunning) {
            println(">>> togglePause() - PAUSING game")
            _state.value = _state.value.copy(isRunning = false)
            loopJob?.cancel()
            // Stop audio when pausing
            try {
                audioPlayer.release()
            } catch (e: Exception) {
                println("GamePlayerScreenModel: Error releasing audio: ${e.message}")
            }
        } else {
            // Only allow unpause if a game has been loaded
            if (_state.value.game == null) {
                println(">>> togglePause() - CANNOT UNPAUSE: No game loaded!")
                return
            }
            println(">>> togglePause() - UNPAUSING game")
            _state.value = _state.value.copy(isRunning = true)
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
        println("GamePlayerScreenModel: Stopping game and releasing all resources")
        _state.value = _state.value.copy(isRunning = false)
        loopJob?.cancel()
        
        // Release audio
        try {
            audioPlayer.release()
        } catch (e: Exception) {
            println("GamePlayerScreenModel: Error releasing audio: ${e.message}")
        }
        
        emulatorEngine.release()
    }

    override fun onDispose() {
        stopGame()
        super.onDispose()
    }
}
