package com.flowos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.di.AppContainer
import com.flowos.app.ui.FlowOSApp
import com.flowos.app.ui.theme.FlowOSTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer
    private val flowSnapTriggered = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = (application as FlowOSApplication).container
        
        handleIntent(intent)

        setContent {
            val themeMode by container.settingsStore.themeMode.collectAsStateWithLifecycle(
                initialValue = com.flowos.app.settings.ThemeMode.SYSTEM_DEFAULT
            )

            FlowOSTheme(themeMode = themeMode) {
                FlowOSApp(
                    container = container,
                    flowSnapAction = if (flowSnapTriggered.value) {
                        flowSnapTriggered.value = false
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
        if (intent?.action == "com.flowos.app.ACTION_FLOW_SNAP") {
            flowSnapTriggered.value = true
        }
    }
}
