package com.flowos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.di.AppContainer
import com.flowos.app.settings.ThemeMode
import com.flowos.app.ui.FlowOSApp
import com.flowos.app.ui.theme.FlowOSTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer
    private val flowSnapTriggered = mutableStateOf(false)
    private val flowPulseTriggered = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = (application as FlowOSApplication).container
        
        handleIntent(intent)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeMode by container.settingsStore.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM_DEFAULT
            )

            FlowOSTheme(themeMode = themeMode) {
                FlowOSApp(
                    container = container,
                    windowSizeClass = windowSizeClass,
                    flowSnapAction = if (flowSnapTriggered.value) {
                        flowSnapTriggered.value = false
                        true
                    } else false,
                    flowPulseAction = if (flowPulseTriggered.value) {
                        flowPulseTriggered.value = false
                        true
                    } else false
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "com.flowos.app.ACTION_FLOW_SNAP" -> flowSnapTriggered.value = true
            "com.flowos.app.ACTION_FLOW_PULSE" -> flowPulseTriggered.value = true
        }
    }
}
