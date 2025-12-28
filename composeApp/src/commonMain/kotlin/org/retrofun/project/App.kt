package org.retrofun.project

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.retrofun.project.presentation.gamelist.GameListScreen

@Composable
@Preview
fun App() {
    KoinContext {
        Navigator(GameListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}