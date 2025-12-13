package io.github.hziqzmin.wildguardai.ui.screens

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * This composable function creates the UI for the SOS screen,
 * featuring a large button to toggle the device flashlight.
 */
@Composable
fun SosScreen() {
    // State to track if the flashlight is on or off
    var isFlashlightOn by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    val cameraId = remember {
        getCameraIdWithFlash(cameraManager)
    }

    // Main dark green color from your chat input bar
    val mainDarkGreen = Color(0xFF3B4948)

    // The button will be bright yellow when on, and dark green when off
    val buttonColor = if (isFlashlightOn) Color.LightGray else mainDarkGreen
    val iconColor = if (isFlashlightOn) Color.White else Color.White

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                if (cameraId != null) {
                    try {
                        val newState = !isFlashlightOn
                        cameraManager.setTorchMode(cameraId, newState)
                        isFlashlightOn = newState // Only update state if successful
                    } catch (e: CameraAccessException) {
                        Log.e("SosScreen", "Failed to set torch mode", e)
                        // You could show a snackbar or toast here
                    }
                } else {
                    Log.w("SosScreen", "No camera with flash found")
                    // Handle devices without a flash
                }
            },
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Icon(
                imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = if (isFlashlightOn) "Turn Flashlight Off" else "Turn Flashlight On",
                tint = iconColor,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

/**
 * Helper function to find the first camera ID that has a flash unit.
 */
private fun getCameraIdWithFlash(cameraManager: CameraManager): String? {
    try {
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            if (hasFlash == true) {
                return id
            }
        }
    } catch (e: CameraAccessException) {
        Log.e("SosScreen", "Error getting camera characteristics", e)
    }
    return null // No flash found
}