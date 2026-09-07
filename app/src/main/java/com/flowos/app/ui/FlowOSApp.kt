package com.flowos.app.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flowos.app.di.AppContainer
import com.flowos.app.ui.screens.ActivityScreen
import com.flowos.app.ui.screens.CaptureScreen
import com.flowos.app.ui.screens.ContextScreen
import com.flowos.app.ui.screens.ExecuteScreen
import com.flowos.app.ui.screens.FlowScreen
import com.flowos.app.ui.screens.FocusScreen
import com.flowos.app.ui.screens.HomeScreen
import com.flowos.app.ui.screens.PlanScreen
import com.flowos.app.ui.screens.ProcessingScreen
import com.flowos.app.ui.screens.SettingsScreen
import com.flowos.app.ui.screens.UnderstandingScreen
import com.flowos.app.ui.screens.WorkflowScreen
import kotlinx.coroutines.launch

private data class TabSpec(val route: String, val label: String, val icon: ImageVector)

private val TOP_LEVEL_TABS = listOf(
    TabSpec(FlowDestinations.HOME, "HOME", Icons.Filled.Home),
    TabSpec(FlowDestinations.PLAN, "PLAN", Icons.Filled.CalendarMonth),
    TabSpec(FlowDestinations.FLOW, "FLOW", Icons.Filled.AccountTree),
    TabSpec(FlowDestinations.CONTEXT, "CONTEXT", Icons.Filled.ScatterPlot),
    TabSpec(FlowDestinations.ACTIVITY, "ACTIVITY", Icons.Filled.History),
)

/**
 * Navigation root. One loop builds the bottom bar (no duplicated item code),
 * the capture loop keeps a clean stack by popping intermediate destinations
 * when jumping ahead. AI mode is read at processing time from Settings.
 */
@Composable
fun FlowOSApp(container: AppContainer) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application
    val navController = rememberNavController()
    val session = remember { CaptureSession() }
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = TOP_LEVEL_TABS.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    TOP_LEVEL_TABS.forEach { tab ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FlowDestinations.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(FlowDestinations.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    key = "home",
                    factory = viewModelFactory {
                        initializer { HomeViewModel(application, container) }
                    },
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onCapture = { navController.navigate(FlowDestinations.CAPTURE) },
                    onStartFocus = { navController.navigate(FlowDestinations.FOCUS) },
                    onViewFlow = { navController.navigate(FlowDestinations.FLOW) },
                    onOpenPlan = { navController.navigate(FlowDestinations.PLAN) },
                    onOpenSettings = { navController.navigate(FlowDestinations.SETTINGS) },
                )
            }

            composable(FlowDestinations.PLAN) {
                val planViewModel: PlanViewModel = viewModel(
                    key = "plan",
                    factory = viewModelFactory {
                        initializer { PlanViewModel(application, container) }
                    },
                )
                PlanScreen(viewModel = planViewModel)
            }

            composable(FlowDestinations.FLOW) {
                val flowViewModel: FlowViewModel = viewModel(
                    key = "flow",
                    factory = viewModelFactory {
                        initializer { FlowViewModel(application, container) }
                    },
                )
                FlowScreen(
                    viewModel = flowViewModel,
                    onStartFocus = { navController.navigate(FlowDestinations.FOCUS) },
                    onCapture = { navController.navigate(FlowDestinations.CAPTURE) },
                )
            }

            composable(FlowDestinations.CONTEXT) {
                val contextViewModel: ContextGraphViewModel = viewModel(
                    key = "contextGraph",
                    factory = viewModelFactory {
                        initializer { ContextGraphViewModel(application, container) }
                    },
                )
                ContextScreen(viewModel = contextViewModel)
            }

            composable(FlowDestinations.ACTIVITY) {
                val activityViewModel: ActivityViewModel = viewModel(
                    key = "activity",
                    factory = viewModelFactory {
                        initializer { ActivityViewModel(application, container) }
                    },
                )
                ActivityScreen(viewModel = activityViewModel, onBack = { navController.popBackStack() })
            }

            // ---- Capture loop (no bottom bar) --------------------------------

            composable(FlowDestinations.CAPTURE) {
                val captureViewModel: CaptureViewModel = viewModel(
                    key = "capture",
                    factory = viewModelFactory {
                        initializer { CaptureViewModel(application, container, session) }
                    },
                )
                CaptureScreen(
                    viewModel = captureViewModel,
                    onReadyToProcess = {
                        // Pop Capture so back from Processing can't re-trigger auto-advance.
                        navController.navigate(FlowDestinations.PROCESSING) {
                            popUpTo(FlowDestinations.CAPTURE) { inclusive = true }
                        }
                    },
                )
            }

            composable(FlowDestinations.PROCESSING) {
                val processingViewModel: ProcessingViewModel = viewModel(
                    key = "processing",
                    factory = viewModelFactory {
                        initializer { ProcessingViewModel(application, container, session) }
                    },
                )
                ProcessingScreen(
                    viewModel = processingViewModel,
                    onDone = {
                        navController.navigate(FlowDestinations.UNDERSTANDING) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    },
                    onError = {
                        navController.navigate(FlowDestinations.CAPTURE) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    },
                )
            }

            composable(FlowDestinations.UNDERSTANDING) {
                UnderstandingScreen(
                    analysis = session.analysis,
                    processingLabel = session.processingMode,
                    onBuildWorkflow = { navController.navigate(FlowDestinations.WORKFLOW) },
                    onEdit = {
                        navController.navigate(FlowDestinations.CAPTURE) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    },
                )
            }

            composable(FlowDestinations.WORKFLOW) {
                val workflowViewModel: WorkflowViewModel = viewModel(
                    key = "workflow",
                    factory = viewModelFactory {
                        initializer { WorkflowViewModel(application, container, session) }
                    },
                )
                WorkflowScreen(
                    plan = session.workflowPlan,
                    dependencyCount = session.analysis?.dependencies?.size ?: 0,
                    onExecute = {
                        coroutineScope.launch {
                            val saved = workflowViewModel.persistWorkflow()
                            if (saved) {
                                navController.navigate(FlowDestinations.EXECUTE) {
                                    popUpTo(FlowDestinations.HOME)
                                }
                            } else {
                                navController.navigate(FlowDestinations.CAPTURE) {
                                    popUpTo(FlowDestinations.HOME)
                                }
                            }
                        }
                    },
                    onEdit = {
                        navController.navigate(FlowDestinations.CAPTURE) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    },
                )
            }

            composable(FlowDestinations.EXECUTE) {
                val executeViewModel: ExecuteViewModel = viewModel(
                    key = "execute",
                    factory = viewModelFactory {
                        initializer { ExecuteViewModel(application, container, session) }
                    },
                )
                ExecuteScreen(
                    viewModel = executeViewModel,
                    onDone = {
                        navController.navigate(FlowDestinations.HOME) {
                            popUpTo(FlowDestinations.HOME) { inclusive = true }
                        }
                    },
                    onEdit = {
                        navController.navigate(FlowDestinations.CAPTURE) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    },
                )
            }

            composable(FlowDestinations.FOCUS) {
                val focusViewModel: FocusViewModel = viewModel(
                    key = "focus",
                    factory = viewModelFactory {
                        initializer { FocusViewModel(application, container) }
                    },
                )
                FocusScreen(viewModel = focusViewModel, onExit = { navController.popBackStack() })
            }

            composable(FlowDestinations.SETTINGS) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    key = "settings",
                    factory = viewModelFactory {
                        initializer { SettingsViewModel(application, container) }
                    },
                )
                SettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
