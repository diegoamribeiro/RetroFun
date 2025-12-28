package org.retrofun.project.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject

class IosFilePickerLauncher(
    private val onFilePicked: (String, String) -> Unit
) : FilePickerLauncher {
    
    private val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            url?.let {
                // Ensure we have access rights (security scoped resource)
                val isAccessing = it.startAccessingSecurityScopedResource()
                
                try {
                    val path = it.absoluteString // Use absoluteString for file:// URI logic in IosRomLoader if needed, or path
                    // Note: IosRomLoader checks for "file://" prefix.
                    // it.path gives "/private/var/..."
                    // it.absoluteString gives "file:///private/var/..."
                    
                    val name = it.lastPathComponent ?: "unknown"
                    
                    // We need to pass a string that IosRomLoader can handle.
                    // If IosRomLoader expects a file URI string, pass absoluteString.
                    onFilePicked(it.absoluteString ?: "", name)
                    
                } finally {
                    if (isAccessing) {
                        it.stopAccessingSecurityScopedResource()
                    }
                }
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            // No-op
        }
    }

    override fun launch() {
        val contentTypes = listOf("public.item", "public.data", "public.content")
        val picker = UIDocumentPickerViewController(documentTypes = contentTypes, inMode = UIDocumentPickerMode.UIDocumentPickerModeImport)
        picker.delegate = delegate
        picker.modalPresentationStyle = UIModalPresentationFormSheet
        
        val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootController?.presentViewController(picker, animated = true, completion = null)
    }
}

@Composable
actual fun rememberFilePickerLauncher(onFilePicked: (String, String) -> Unit): FilePickerLauncher {
    return remember { IosFilePickerLauncher(onFilePicked) }
}
