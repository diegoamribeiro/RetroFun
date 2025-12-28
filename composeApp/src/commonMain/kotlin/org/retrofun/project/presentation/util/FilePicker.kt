package org.retrofun.project.presentation.util

import androidx.compose.runtime.Composable

interface FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePickerLauncher(onFilePicked: (String, String) -> Unit): FilePickerLauncher
