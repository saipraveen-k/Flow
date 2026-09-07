package com.flowos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flowos.app.di.AppContainer
import com.flowos.app.ui.FlowOSApp
import com.flowos.app.ui.theme.FlowOSTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = (application as FlowOSApplication).container
        setContent {
            FlowOSTheme {
                FlowOSApp(container = container)
            }
        }
    }
}
