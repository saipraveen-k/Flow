package com.flowos.app.ui

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.flowos.app.di.AppContainer
import com.flowos.app.ui.components.CaptureMenu
import com.flowos.app.ui.screens.*
import kotlinx.coroutines.launch

private data class TabSpec(val route: String, val label: String, val icon: ImageVector)

private val TOP_LEVEL_TABS = listOf(
    TabSpec(FlowDestinations.HOME, "HOME", Icons.Filled.Home),
    TabSpec(FlowDestinations.OUTCOMES, "OUTCOMES", Icons.Filled.AccountTree),
    TabSpec(FlowDestinations.CALENDAR, "CALENDAR", Icons.Filled.CalendarMonth),
    TabSpec(FlowDestinations.FLOW_SPACE, "SPACE", Icons.Filled.ScatterPlot),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowOSApp(container: AppContainer) {
    val application = LocalContext.current.applicationContext as Application
    val navController = rememberNavController()
    val session = remember { CaptureSession() }
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showCaptureMenu by remember { mutableStateOf(false) }
    val onboardingCompleted by container.settingsStore.onboardingCompleted.collectAsStateWithLifecycle(initialValue = null)

    val showBottomBar = TOP_LEVEL_TABS.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    if (showCaptureMenu) {
        CaptureMenu(
            onDismiss = { showCaptureMenu = false },
            onModeSelected = { mode ->
                showCaptureMenu = false
                navController.navigate(FlowDestinations.CAPTURE)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(
                    containerColor = Color(0xFF070707),
                    tonalElevation = 0.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(80.dp)
                ) {
                    TOP_LEVEL_TABS.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { 
                                Icon(
                                    imageVector = tab.icon, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(26.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = tab.label, 
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                Box(contentAlignment = Alignment.Center) {
                    // Outer glow/ring for flagship feel
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {}
                    FloatingActionButton(
                        onClick = { showCaptureMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Capture", modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FlowDestinations.SPLASH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(FlowDestinations.SPLASH) {
                SplashScreen(onAnimationFinished = {
                    if (onboardingCompleted == true) {
                        navController.navigate(FlowDestinations.HOME) {
                            popUpTo(FlowDestinations.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(FlowDestinations.ONBOARDING) {
                            popUpTo(FlowDestinations.SPLASH) { inclusive = true }
                        }
                    }
                })
            }

            composable(FlowDestinations.ONBOARDING) {
                OnboardingScreen(onFinished = {
                    coroutineScope.launch {
                        container.settingsStore.markOnboardingCompleted()
                        navController.navigate(FlowDestinations.HOME) {
                            popUpTo(FlowDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                })
            }

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
                    onViewFlow = { navController.navigate(FlowDestinations.OUTCOMES) },
                    onOpenPlan = { navController.navigate(FlowDestinations.CALENDAR) },
                    onOpenSettings = { navController.navigate(FlowDestinations.MORE) },
                    onViewAdaptation = { navController.navigate(FlowDestinations.REPLANNING) }
                )
            }

            composable(FlowDestinations.OUTCOMES) {
                val activityViewModel: ActivityViewModel = viewModel(
                    key = "activity",
                    factory = viewModelFactory {
                        initializer { ActivityViewModel(application, container) }
                    },
                )
                OutcomeListScreen(
                    viewModel = activityViewModel,
                    onOutcomeClick = { outcomeId -> 
                        navController.navigate("outcome_detail/$outcomeId") 
                    }
                )
            }

            composable("outcome_detail/{outcomeId}") {
                val flowViewModel: FlowViewModel = viewModel(
                    key = "flow",
                    factory = viewModelFactory {
                        initializer { FlowViewModel(application, container) }
                    },
                )
                OutcomeDetailScreen(
                    viewModel = flowViewModel,
                    onStartFocus = { navController.navigate(FlowDestinations.FOCUS) },
                    onCapture = { navController.navigate(FlowDestinations.CAPTURE) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(FlowDestinations.CALENDAR) {
                val planViewModel: PlanViewModel = viewModel(
                    key = "plan",
                    factory = viewModelFactory {
                        initializer { PlanViewModel(application, container) }
                    },
                )
                PlanScreen(viewModel = planViewModel)
            }

            composable(FlowDestinations.FLOW_SPACE) {
                val activityViewModel: ActivityViewModel = viewModel(
                    key = "flow_space",
                    factory = viewModelFactory {
                        initializer { ActivityViewModel(application, container) }
                    },
                )
                FlowSpaceScreen(
                    viewModel = activityViewModel,
                    onNavigateToCapture = { navController.navigate(FlowDestinations.CAPTURE) }
                )
            }

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
                        navController.navigate(FlowDestinations.PROCESSING) {
                            popUpTo(FlowDestinations.CAPTURE) { inclusive = true }
                        }
                    }
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
                    }
                )
            }

            composable(FlowDestinations.UNDERSTANDING) {
                UnderstandingScreen(
                    analysis = session.analysis,
                    outcome = session.outcome,
                    onBuildWorkflow = { navController.navigate(FlowDestinations.WORKFLOW) },
                    onEdit = {
                        navController.navigate(FlowDestinations.CAPTURE) {
                            popUpTo(FlowDestinations.HOME)
                        }
                    }
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
                    }
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
                    }
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

            composable(FlowDestinations.REPLANNING) {
                val homeViewModel: HomeViewModel = viewModel(
                    key = "home",
                    factory = viewModelFactory {
                        initializer { HomeViewModel(application, container) }
                    },
                )
                val state by homeViewModel.uiState.collectAsStateWithLifecycle()
                
                state.replanProposal?.let { proposal ->
                    ReplanningScreen(
                        proposal = proposal,
                        onAccept = { navController.popBackStack() },
                        onReject = { navController.popBackStack() }
                    )
                } ?: run {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }

            composable(FlowDestinations.MORE) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    key = "settings",
                    factory = viewModelFactory {
                        initializer { SettingsViewModel(application, container) }
                    },
                )
                MoreScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToStrategies = { navController.navigate(FlowDestinations.STRATEGIES) },
                    onNavigateToMemory = { navController.navigate(FlowDestinations.MEMORY) },
                    onNavigateToActivity = { navController.navigate(FlowDestinations.ACTIVITY) }
                )
            }

            composable(FlowDestinations.STRATEGIES) {
                val strategyViewModel: StrategyViewModel = viewModel(
                    key = "strategy",
                    factory = viewModelFactory {
                        initializer { StrategyViewModel(application, container) }
                    },
                )
                StrategyScreen(
                    viewModel = strategyViewModel,
                    onBack = { navController.popBackStack() },
                    onStrategyApplied = { navController.navigate(FlowDestinations.HOME) {
                        popUpTo(FlowDestinations.HOME) { inclusive = true }
                    } }
                )
            }

            composable(FlowDestinations.MEMORY) {
                MemoryScreen(onBack = { navController.popBackStack() })
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
        }
    }
}
