package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.CaprazcaTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GameEffect
import com.example.ui.util.SoundManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            val soundEnabled by viewModel.soundEnabled.collectAsState()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
            
            val context = LocalContext.current
            val soundManager = remember { SoundManager(context) }
            val haptic = LocalHapticFeedback.current

            LaunchedEffect(Unit) {
                viewModel.gameEffects.collect { effect ->
                    when (effect) {
                        is GameEffect.Sound -> {
                            if (soundEnabled) {
                                soundManager.playSound(effect.key)
                            }
                        }
                        is GameEffect.Vibration -> {
                            if (vibrationEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                }
            }

            CaprazcaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}
