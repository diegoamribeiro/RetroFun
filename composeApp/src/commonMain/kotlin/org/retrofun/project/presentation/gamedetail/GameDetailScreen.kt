package org.retrofun.project.presentation.gamedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.retrofun.project.presentation.gameplayer.GamePlayerScreen

data class GameDetailScreen(val gameId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<GameDetailScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current

        LaunchedEffect(gameId) {
            screenModel.loadGame(gameId)
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text(state.game?.name ?: "Loading...") }) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    state.game?.let { game ->
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Console: ${game.console.name}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ROM: ${game.romPath}", style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(onClick = {
                                navigator?.push(GamePlayerScreen(game.id))
                            }) {
                                Text("JOGAR AGORA")
                            }
                        }
                    }
                }
            }
        }
    }
}
