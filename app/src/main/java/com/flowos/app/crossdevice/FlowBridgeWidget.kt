package com.flowos.app.crossdevice

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class FlowBridgeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isPcConnected = RealCrossDeviceConnectionState.isConnected

        provideContent {
            GlanceTheme {
                WidgetContent(isPcConnected)
            }
        }
    }

    @Composable
    private fun WidgetContent(isConnected: Boolean) {
        val bgColor = if (isConnected) Color(0xFF0D1117) else Color(0xFF161B22)
        val textColor = Color(0xFFF0F6FC)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(ColorProvider(day = bgColor, night = bgColor)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FLOW BRIDGE",
                style = TextStyle(
                    color = ColorProvider(day = textColor, night = textColor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (isConnected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val green = Color(0xFF10B981)
                    Text(
                        text = "● PC CONNECTED",
                        style = TextStyle(
                            color = ColorProvider(day = green, night = green),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    val subColor = Color(0xFF8B949E)
                    Text(
                        text = "Recent: Presentation.pptx",
                        style = TextStyle(
                            color = ColorProvider(day = subColor, night = subColor),
                            fontSize = 10.sp
                        )
                    )
                }
            } else {
                val red = Color(0xFFEF4444)
                Text(
                    text = "PC NOT CONNECTED",
                    style = TextStyle(
                        color = ColorProvider(day = red, night = red),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

object RealCrossDeviceConnectionState {
    @Volatile
    var isConnected: Boolean = false
}

class FlowBridgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FlowBridgeWidget()
}

class ConnectActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Connect action callback
    }
}
