package org.retrofun.project

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }

fun doInitKoin() {
    org.retrofun.project.di.initKoin()
}