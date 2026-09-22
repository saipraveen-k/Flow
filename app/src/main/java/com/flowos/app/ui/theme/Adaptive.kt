package com.flowos.app.ui.theme

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive layout helper.
 */
object FlowAdaptive {
    @Composable
    fun maxContentWidth(widthSizeClass: WindowWidthSizeClass): Dp = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Dp.Unspecified
        WindowWidthSizeClass.Medium -> 600.dp
        else -> 840.dp
    }

    @Composable
    fun gridColumns(widthSizeClass: WindowWidthSizeClass): Int = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }
}
