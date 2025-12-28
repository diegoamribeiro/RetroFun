package org.retrofun.project

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.retrofun.project.presentation.gamelist.GameListScreen

@Composable
@Preview
fun App() {
    KoinContext {
        androidx.compose.material3.MaterialTheme {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                Navigator(GameListScreen()) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}