package com.flowos.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.flowos.app.ui.theme.FlowAccent

/**
 * FLOWOS V3 Logo: Abstract "F" from two continuous flowing paths.
 * Communicates: INPUT -> FLOW -> OUTCOME.
 */
@Composable
fun FlowOSLogo(
    modifier: Modifier = Modifier,
    color: Color = FlowAccent
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        
        // Path 1: The main vertical flow
        val path1 = Path().apply {
            moveTo(w * 0.3f, h * 0.1f)
            lineTo(w * 0.3f, h * 0.9f)
            
            // Top horizontal branch
            moveTo(w * 0.3f, h * 0.1f)
            quadraticBezierTo(w * 0.7f, h * 0.1f, w * 0.8f, h * 0.25f)
            
            // Middle horizontal branch
            moveTo(w * 0.3f, h * 0.45f)
            quadraticBezierTo(w * 0.6f, h * 0.45f, w * 0.7f, h * 0.55f)
        }
        
        drawPath(
            path = path1,
            color = color,
            style = Stroke(width = w * 0.12f, cap = StrokeCap.Round)
        )
        
        // Path 2: Kinetic highlight line (Input to Flow)
        val path2 = Path().apply {
            moveTo(w * 0.15f, h * 0.3f)
            quadraticBezierTo(w * 0.3f, h * 0.3f, w * 0.3f, h * 0.45f)
        }
        
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.5f), color)),
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
        )
    }
}

/** Flagship app icon representation. */
@Composable
fun FlowOSIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        FlowOSLogo(modifier = Modifier.size(100.dp))
    }
}
