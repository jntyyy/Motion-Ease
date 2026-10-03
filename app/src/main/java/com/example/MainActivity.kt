package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motion.ui.*
import com.example.ui.theme.*

enum class AppScreen {
    DASHBOARD,
    SETTINGS,
    DIAGNOSTICS,
    HELP_AND_SUPPORT
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
                var showSimulatorDialog by remember { mutableStateOf(false) }
                var showOemGuideDialog by remember { mutableStateOf(false) }
                var showQsGuideDialog by remember { mutableStateOf(false) }
                var showSafetyDisclaimerDialog by remember { mutableStateOf(false) }

                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val processedMotion by viewModel.processedMotion.collectAsStateWithLifecycle()
                val rawReading by viewModel.rawReading.collectAsStateWithLifecycle()
                val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()
                val currentScenario by viewModel.currentSimulationScenario.collectAsStateWithLifecycle()

                // Notification permission launcher for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled gracefully */ }

                // Check and request POST_NOTIFICATIONS on start if needed
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val isGranted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!isGranted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Handle system back gesture for nested screens
                if (currentScreen != AppScreen.DASHBOARD) {
                    BackHandler {
                        currentScreen = if (currentScreen == AppScreen.HELP_AND_SUPPORT) AppScreen.SETTINGS else AppScreen.DASHBOARD
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { _ ->
                    when (currentScreen) {
                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                settings = settings,
                                processedMotion = processedMotion,
                                hasOverlayPermission = hasOverlayPermission,
                                isSensorsAvailable = viewModel.hardwareStatus.isOperational,
                                currentScenario = currentScenario,
                                onToggleActive = { viewModel.toggleMotionCues() },
                                onTogglePause = { viewModel.togglePause() },
                                onRequestOverlayPermission = { requestOverlayPermission() },
                                onOpenSimulator = { showSimulatorDialog = true },
                                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                                onOpenDiagnostics = { currentScreen = AppScreen.DIAGNOSTICS },
                                onSelectMode = { newMode ->
                                    viewModel.updateSettings { it.copy(mode = newMode) }
                                },
                                onAddQuickSettingsTile = {
                                    requestAddQuickSettingsTile(onShowManualGuide = { showQsGuideDialog = true })
                                }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                                onSelectMode = { newMode ->
                                    viewModel.updateSettings { it.copy(mode = newMode) }
                                },
                                onAddQuickSettingsTile = {
                                    requestAddQuickSettingsTile(onShowManualGuide = { showQsGuideDialog = true })
                                },
                                onOpenDiagnostics = { currentScreen = AppScreen.DIAGNOSTICS },
                                onOpenOemGuide = { showOemGuideDialog = true },
                                onOpenHelpAndSupport = { currentScreen = AppScreen.HELP_AND_SUPPORT },
                                onBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.DIAGNOSTICS -> {
                            DiagnosticsScreen(
                                hardwareStatus = viewModel.hardwareStatus,
                                rawReading = rawReading,
                                processedMotion = processedMotion,
                                hasOverlayPermission = hasOverlayPermission,
                                isServiceActive = settings.isEnabled && !settings.isPaused,
                                onBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.HELP_AND_SUPPORT -> {
                            HelpAndSupportScreen(
                                onBack = { currentScreen = AppScreen.SETTINGS },
                                onOpenSafetyDisclaimer = { showSafetyDisclaimerDialog = true }
                            )
                        }
                    }

                    // Simulator Scenario Dialog
                    if (showSimulatorDialog) {
                        SimulatorDialog(
                            currentScenario = currentScenario,
                            onSelectScenario = { scenario ->
                                viewModel.setSimulationScenario(scenario)
                            },
                            onDismiss = { showSimulatorDialog = false }
                        )
                    }

                    // OEM Guidance Dialog
                    if (showOemGuideDialog) {
                        OemGuidanceDialog(
                            onDismiss = { showOemGuideDialog = false }
                        )
                    }

                    // Quick Settings Guide Dialog
                    if (showQsGuideDialog) {
                        QuickSettingsGuideDialog(
                            onDismiss = { showQsGuideDialog = false }
                        )
                    }

                    // Passenger Safety Disclaimer Dialog
                    if (showSafetyDisclaimerDialog) {
                        AlertDialog(
                            onDismissRequest = { showSafetyDisclaimerDialog = false },
                            containerColor = FlatCardSurface,
                            titleContentColor = TextPrimary,
                            textContentColor = TextSecondary,
                            title = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.safety_disclaimer_title),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.safety_disclaimer_body),
                                    lineHeight = 20.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showSafetyDisclaimerDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IosAccentBlue,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(RadiusTokens.pill)
                                ) {
                                    Text("Understood", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh overlay permission state whenever returning from Android Settings
        viewModel.refreshPermissions()
    }

    private fun requestAddQuickSettingsTile(onShowManualGuide: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val statusBarManager = getSystemService(android.app.StatusBarManager::class.java)
                val component = android.content.ComponentName(
                    this,
                    com.example.motion.service.MotionCueTileService::class.java
                )
                val icon = android.graphics.drawable.Icon.createWithResource(
                    this,
                    R.drawable.ic_qs_motion_cues
                )
                statusBarManager?.requestAddTileService(
                    component,
                    getString(R.string.tile_motion_cues),
                    icon,
                    ContextCompat.getMainExecutor(this)
                ) { /* Result handled */ }
            } catch (e: Exception) {
                onShowManualGuide()
            }
        } else {
            onShowManualGuide()
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}
