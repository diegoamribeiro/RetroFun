package org.retrofun.project.presentation.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidFilePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>
) : FilePickerLauncher {
    override fun launch() {
        launcher.launch("*/*")
    }
}

@Composable
actual fun rememberFilePickerLauncher(onFilePicked: (String, String) -> Unit): FilePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission for future reads
            try {
                val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                // Ignore
            }
            
            // Get filename
            var name = "Unknown Game"
            try {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val index = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = c.getString(index)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            println("FilePicker: URI selected: $it")
            println("FilePicker: Resolved name: $name")
            
            onFilePicked(name, it.toString())
        }
    }
    
    return remember(launcher) {
        AndroidFilePickerLauncher(launcher)
    }
}
