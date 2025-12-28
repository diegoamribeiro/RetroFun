package org.retrofun.project.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosFilePickerLauncher : FilePickerLauncher {
    override fun launch() {
        // No-op for user prototype
    }
}

@Composable
actual fun rememberFilePickerLauncher(onFilePicked: (String, String) -> Unit): FilePickerLauncher {
    return remember { IosFilePickerLauncher() }
}
