package org.retrofun.project.presentation.gameplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.retrofun.project.presentation.gameplayer.components.VirtualController
import org.retrofun.project.presentation.util.toImageBitmap

data class GamePlayerScreen(val gameId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<GamePlayerScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current

        LaunchedEffect(gameId) {
            screenModel.loadGame(gameId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.game?.name ?: "Game", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                             Text("<", color = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = screenModel::resetGame) {
                            Text("R", color = Color.White)
                        }
                        IconButton(onClick = screenModel::togglePause) {
                             Text(
                                 if (state.isRunning) "||" else ">",
                                 color = Color.White
                             )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f)
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
                println("GamePlayerScreen: Recomposing - currentFrame is ${if (state.currentFrame != null) "NOT NULL" else "NULL"}")
                
                state.currentFrame?.toImageBitmap()?.let { bitmap ->
                    println("GamePlayerScreen: Drawing Image - bitmap=${bitmap.width}x${bitmap.height}")
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Game Screen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } ?: println("GamePlayerScreen: toImageBitmap() returned NULL or currentFrame is NULL")
                
                VirtualController(onInputUpdate = screenModel::onControllerInput)
                
                if (state.game == null) {
                     Text(
                        text = "Loading...",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                
                if (!state.isRunning && state.game != null) {
                    Text(
                        text = "PAUSED",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Yellow,
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
        }
    }
}
