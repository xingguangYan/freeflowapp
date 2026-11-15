package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.BridgeScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.PackagingScreen
import com.example.ui.screens.PlatformsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.FreeFlowTheme
import com.example.ui.viewmodel.FreeFlowViewModel

object Routes {
    const val STUDIO = "studio"
    const val PLATFORMS = "platforms"
    const val BRIDGE = "bridge"
    const val PACKAGING = "packaging"
    const val EDITOR = "editor/{draftId}"
    fun editorRoute(draftId: Long): String = "editor/$draftId"
}

private data class TopTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topTabs = listOf(
    TopTab(Routes.STUDIO, R.string.tab_studio, Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_studio"),
    TopTab(Routes.PLATFORMS, R.string.tab_platforms, Icons.Filled.Hub, Icons.Outlined.Hub, "nav_platforms"),
    TopTab(Routes.BRIDGE, R.string.tab_bridge, Icons.Filled.Lan, Icons.Outlined.Lan, "nav_bridge"),
    TopTab(Routes.PACKAGING, R.string.tab_packaging, Icons.Filled.Build, Icons.Outlined.Build, "nav_packaging")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeFlowTheme {
                FreeFlowApp()
            }
        }
    }
}

@Composable
fun FreeFlowApp(viewModel: FreeFlowViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTopChrome = topTabs.any { it.route == currentRoute }

    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    val tasks by viewModel.publishTasks.collectAsStateWithLifecycle()
    val bridgeConfig by viewModel.bridgeConfig.collectAsStateWithLifecycle()
    val defaultPlatforms by viewModel.defaultPlatformIds.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val desktopAccounts by viewModel.desktopAccounts.collectAsStateWithLifecycle()
    val isSyncingAccounts by viewModel.isSyncingAccounts.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopChrome && !useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopChrome && useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.STUDIO,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.STUDIO) {
                        StudioScreen(
                            drafts = drafts,
                            connectionState = connectionState,
                            onCreateDraft = { navController.navigate(Routes.editorRoute(0L)) },
                            onLoadTemplate = {
                                viewModel.insertStarterMarkdownTemplate { createdId ->
                                    navController.navigate(Routes.editorRoute(createdId))
                                }
                            },
                            onOpenDraft = { id -> navController.navigate(Routes.editorRoute(id)) },
                            onDeleteDraft = { id -> viewModel.deleteDraft(id) }
                        )
                    }

                    composable(
                        route = Routes.EDITOR,
                        arguments = listOf(navArgument("draftId") { type = NavType.LongType })
                    ) { entry ->
                        val draftId = entry.arguments?.getLong("draftId") ?: 0L
                        val existingDraft by viewModel.observeDraft(draftId)
                            .collectAsStateWithLifecycle(initialValue = null)

                        EditorScreen(
                            existingDraft = if (draftId == 0L) null else existingDraft,
                            defaultPlatformIds = defaultPlatforms,
                            onBack = { navController.popBackStack() },
                            onSaveDraft = { draft ->
                                viewModel.saveDraft(draft) { savedId ->
                                    if (draftId == 0L) {
                                        navController.navigate(Routes.editorRoute(savedId)) {
                                            popUpTo(Routes.EDITOR) { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onDispatchPublish = { draft ->
                                viewModel.publishDraftToDesktop(draft)
                            }
                        )
                    }

                    composable(Routes.PLATFORMS) {
                        PlatformsScreen(
                            defaultPlatformIds = defaultPlatforms,
                            desktopAccounts = desktopAccounts,
                            onToggleDefaultPlatform = { viewModel.toggleDefaultPlatform(it) },
                            onApplyPreset = { viewModel.applyDefaultPreset(it) }
                        )
                    }

                    composable(Routes.BRIDGE) {
                        BridgeScreen(
                            bridgeConfig = bridgeConfig,
                            connectionState = connectionState,
                            isSyncingAccounts = isSyncingAccounts,
                            publishTasks = tasks,
                            onSaveConfig = { host, token -> viewModel.saveBridgeConfig(host, token) },
                            onPingDesktop = { host, token -> viewModel.pingDesktop(host, token) },
                            onSyncAccounts = { viewModel.syncDesktopAccounts() },
                            onRetryTask = { task -> viewModel.retryTask(task) },
                            onDeleteTask = { id -> viewModel.deleteTask(id) }
                        )
                    }

                    composable(Routes.PACKAGING) {
                        PackagingScreen(
                            onShowMessage = { msg -> viewModel.showMessage(msg) }
                        )
                    }
                }
            }
        }
    }
}
