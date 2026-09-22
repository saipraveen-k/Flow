package com.flowos.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.FlowDestinations
import com.flowos.app.ui.theme.DesignTokens
import com.flowos.app.ui.theme.FlowAccent

data class MoreMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String
)

private val MENU_ITEMS = listOf(
    MoreMenuItem("Focus", Icons.Filled.Timer, FlowDestinations.FOCUS, "Deep execution mode"),
    MoreMenuItem("Strategies", Icons.Filled.RocketLaunch, FlowDestinations.STRATEGIES, "Productivity patterns"),
    MoreMenuItem("Memory", Icons.Filled.Memory, FlowDestinations.MEMORY, "Work context & decisions"),
    MoreMenuItem("Flow Space", Icons.Filled.Folder, FlowDestinations.FLOW_SPACE, "Files & captures"),
    MoreMenuItem("Office Kit", Icons.Filled.Devices, FlowDestinations.OFFICE_KIT, "PC connection"),
    MoreMenuItem("Settings", Icons.Filled.Settings, FlowDestinations.SETTINGS, "App preferences"),
    MoreMenuItem("Privacy", Icons.Filled.Lock, FlowDestinations.PRIVACY, "Data management"),
    MoreMenuItem("Activity", Icons.Filled.History, FlowDestinations.ACTIVITY, "Audit trail"),
    MoreMenuItem("Diagnostics", Icons.Filled.HealthAndSafety, FlowDestinations.DIAGNOSTICS, "System health")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070707),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
        shape = RoundedCornerShape(topStart = DesignTokens.Shapes.ExtraLarge, topEnd = DesignTokens.Shapes.ExtraLarge)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = DesignTokens.Spacing.Large)
                .padding(bottom = DesignTokens.Spacing.Huge * 2)
        ) {
            Text(
                "SYSTEM TOOLS",
                style = MaterialTheme.typography.labelSmall,
                color = FlowAccent,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Small))
            Text(
                "Extended Operating Layer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(Modifier.height(DesignTokens.Spacing.Section))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MENU_ITEMS) { item ->
                    MoreMenuTile(item) { onNavigate(item.route) }
                }
            }
        }
    }
}

@Composable
private fun MoreMenuTile(
    item: MoreMenuItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF101010),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(DesignTokens.Spacing.Large)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(DesignTokens.Shapes.Small))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = FlowAccent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Color.White)
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
    }
}
