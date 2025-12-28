package org.retrofun.project.presentation.gameplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.retrofun.project.domain.emulation.ControllerState

@Composable
fun VirtualController(
    onInputUpdate: ((ControllerState) -> ControllerState) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // D-Pad (Bottom Left)
        Box(modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 32.dp)) {
            DPad(onInputUpdate)
        }

        // Action Buttons (Bottom Right)
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 48.dp)) {
            ActionButtons(onInputUpdate)
        }
        
        // Start/Select (Bottom Center)
        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
            ControlBtn("SELECT") { down -> onInputUpdate { it.copy(select = down) } }
            Spacer(modifier = Modifier.size(24.dp))
            ControlBtn("START") { down -> onInputUpdate { it.copy(start = down) } }
        }
    }
}

@Composable
fun DPad(onInputUpdate: ((ControllerState) -> ControllerState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ControlBtn("UP") { down -> onInputUpdate { it.copy(up = down) } }
        Row {
            ControlBtn("LEFT") { down -> onInputUpdate { it.copy(left = down) } }
            Spacer(modifier = Modifier.size(48.dp))
            ControlBtn("RIGHT") { down -> onInputUpdate { it.copy(right = down) } }
        }
        ControlBtn("DOWN") { down -> onInputUpdate { it.copy(down = down) } }
    }
}

@Composable
fun ActionButtons(onInputUpdate: ((ControllerState) -> ControllerState) -> Unit) {
    Row(verticalAlignment = Alignment.Bottom) {
        ControlBtn("B") { down -> onInputUpdate { it.copy(b = down) } }
        Spacer(modifier = Modifier.size(24.dp))
        ControlBtn("A", modifier = Modifier.padding(bottom = 24.dp)) { down -> onInputUpdate { it.copy(a = down) } }
    }
}

@Composable
fun ControlBtn(
    label: String,
    modifier: Modifier = Modifier,
    onStateChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White)
    }
}
