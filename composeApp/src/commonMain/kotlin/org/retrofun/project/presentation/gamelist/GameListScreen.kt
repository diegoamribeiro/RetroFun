package org.retrofun.project.presentation.gamelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.retrofun.project.domain.model.Game
import org.retrofun.project.presentation.gamedetail.GameDetailScreen
import org.retrofun.project.presentation.util.rememberFilePickerLauncher

class GameListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<GameListScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current

        val picker = rememberFilePickerLauncher { name, uri ->
            screenModel.addGame(name, uri)
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("RetroKMP Games") }) },
            floatingActionButton = {
                FloatingActionButton(onClick = { picker.launch() }) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(state.games) { game ->
                            GameItem(game) {
                                navigator?.push(GameDetailScreen(game.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameItem(game: Game, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = game.name, style = MaterialTheme.typography.titleMedium)
        Text(text = game.console.name, style = MaterialTheme.typography.bodySmall)
    }
}
