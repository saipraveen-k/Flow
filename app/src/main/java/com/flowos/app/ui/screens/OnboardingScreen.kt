package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignTokens.Spacing.Section)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboardingTransition"
            ) { targetStep ->
                val title = when (targetStep) {
                    1 -> "Your work has a flow."
                    2 -> "Reality changes."
                    else -> "Finish with proof."
                }
                
                val description = when (targetStep) {
                    1 -> "FlowOS turns goals into adaptive execution plans."
                    2 -> "FlowOS detects friction and adjusts your plan."
                    else -> "FlowOS verifies outcomes instead of simply checking boxes."
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(DesignTokens.Spacing.Large))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Small)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (step == i + 1) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (step == i + 1) FlowAccent else MaterialTheme.colorScheme.outline)
                    )
                }
            }
            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowPrimaryButton(
                text = if (step < 3) "NEXT" else "BUILD MY FLOW",
                onClick = {
                    if (step < 3) step++ else onFinished()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        }
    }
}
