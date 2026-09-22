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
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.flowos.app.MainActivity

class FlowBridgeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In a production app, we would use a real data source here.
        val isPcConnected = RealCrossDeviceConnectionState.isConnected

        provideContent {
            GlanceTheme {
                WidgetContent(isPcConnected)
            }
        }
    }

    @Composable
    private fun WidgetContent(isConnected: Boolean) {
        val bgColor = if (isConnected) Color(0xFF070707) else Color(0xFF101010)
        val accentColor = Color(0xFFFFD400)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(ColorProvider(day = bgColor, night = bgColor)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FLOW BRIDGE",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (isConnected) {
                StatusIndicator(Color(0xFF10B981), "SYSTEM LINKED")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "DESKTOP-VQ7R8",
                    style = TextStyle(color = ColorProvider(day = Color.Gray, night = Color.Gray), fontSize = 10.sp)
                )
            } else {
                StatusIndicator(Color(0xFFF59E0B), "DISCONNECTED")
                Spacer(modifier = GlanceModifier.height(12.dp))
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(day = accentColor, night = accentColor))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONNECT",
                        style = TextStyle(color = ColorProvider(day = Color.Black, night = Color.Black), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }
            }
        }
    }

    @Composable
    private fun StatusIndicator(color: Color, label: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(ColorProvider(day = color, night = color))
            ) {}
            Spacer(modifier = GlanceModifier.padding(horizontal = 4.dp))
            Text(
                text = label,
                style = TextStyle(color = ColorProvider(day = color, night = color), fontWeight = FontWeight.Medium, fontSize = 10.sp)
            )
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
