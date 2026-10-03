package com.example.motion.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.motion.data.SettingsRepository
import com.example.motion.engine.MotionEngine
import com.example.motion.model.MotionCueSettings
import com.example.motion.model.MotionMode
import com.example.motion.model.SimulationScenario
import com.example.motion.overlay.MotionOverlayManager
import com.example.motion.sensor.SensorDataManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Foreground Service that manages continuous vehicle sensor tracking and
 * powers the system overlay window across third-party applications.
 */
class MotionCueService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var motionEngine: MotionEngine
    private lateinit var overlayManager: MotionOverlayManager

    private var currentSettings = MotionCueSettings()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MotionCueService onCreate")

        settingsRepository = SettingsRepository.getInstance(applicationContext)
        sensorDataManager = SensorDataManager(applicationContext)
        motionEngine = MotionEngine()
        overlayManager = MotionOverlayManager(applicationContext)

        createNotificationChannel()
        startInForeground()

        // Observe settings changes
        serviceScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                currentSettings = settings
                overlayManager.updateSettings(settings)

                if (settings.isEnabled && !settings.isPaused) {
                    if (!overlayManager.isOverlayAttached) {
                        overlayManager.showOverlay(settings)
                    }
                    sensorDataManager.startListening(highPrecision = true)
                } else if (settings.isPaused) {
                    sensorDataManager.stopListening()
                } else {
                    overlayManager.hideOverlay()
                    sensorDataManager.stopListening()
                    stopSelf()
                }
                updateNotification()
            }
        }

        // Bridge Sensor Readings -> Motion Engine -> Overlay Manager
        serviceScope.launch(Dispatchers.Default) {
            sensorDataManager.rawReadingFlow.collect { raw ->
                motionEngine.processFrame(raw, currentSettings)
                val processed = motionEngine.processedMotion.value
                withContext(Dispatchers.Main) {
                    overlayManager.updateMotion(processed)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.i(TAG, "onStartCommand with action: $action")

        when (action) {
            ACTION_START -> {
                startInForeground()
                currentSettings = settingsRepository.settings.value
                if (!currentSettings.isEnabled) {
                    settingsRepository.updateSettings { it.copy(isEnabled = true, isPaused = false) }
                }
                overlayManager.showOverlay(currentSettings)
                sensorDataManager.startListening(highPrecision = true)
            }

            ACTION_STOP -> {
                settingsRepository.updateSettings { it.copy(isEnabled = false, isPaused = false) }
                stopCuesAndService()
                return START_NOT_STICKY
            }

            ACTION_TOGGLE_PAUSE -> {
                val isCurrentlyPaused = settingsRepository.settings.value.isPaused
                settingsRepository.updateSettings { it.copy(isPaused = !isCurrentlyPaused) }
            }

            ACTION_SET_SIMULATION -> {
                val scenarioName = intent?.getStringExtra(EXTRA_SIMULATION_SCENARIO)
                val scenario = scenarioName?.let {
                    try { SimulationScenario.valueOf(it) } catch (e: Exception) { null }
                }
                motionEngine.setSimulationScenario(scenario)
            }
        }

        return START_STICKY
    }

    private fun startInForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Falling back to default foreground service start: ${e.message}")
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Stop
        val stopIntent = Intent(this, MotionCueService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Action: Pause / Resume
        val toggleIntent = Intent(this, MotionCueService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                2,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                2,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val isPaused = currentSettings.isPaused
        val pauseActionTitle = if (isPaused) {
            getString(R.string.notification_action_resume)
        } else {
            getString(R.string.notification_action_pause)
        }

        val statusText = if (isPaused) {
            getString(R.string.status_paused)
        } else {
            getString(R.string.notification_content)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_qs_motion_cues)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, pauseActionTitle, togglePendingIntent)
            .addAction(0, getString(R.string.notification_action_stop), stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun stopCuesAndService() {
        overlayManager.hideOverlay()
        sensorDataManager.stopListening()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        Log.i(TAG, "MotionCueService onDestroy")
        serviceJob.cancel()
        overlayManager.hideOverlay()
        sensorDataManager.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "MotionCueService"
        const val CHANNEL_ID = "kinetic_motion_cues_channel"
        const val NOTIFICATION_ID = 4041

        const val ACTION_START = "com.example.motion.action.START"
        const val ACTION_STOP = "com.example.motion.action.STOP"
        const val ACTION_TOGGLE_PAUSE = "com.example.motion.action.TOGGLE_PAUSE"
        const val ACTION_SET_SIMULATION = "com.example.motion.action.SET_SIMULATION"
        const val EXTRA_SIMULATION_SCENARIO = "extra_simulation_scenario"

        fun startService(context: Context) {
            val intent = Intent(context, MotionCueService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MotionCueService::class.java)
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop service: ${e.message}")
            }
        }

        fun setSimulation(context: Context, scenario: SimulationScenario?) {
            val intent = Intent(context, MotionCueService::class.java).apply {
                action = ACTION_SET_SIMULATION
                putExtra(EXTRA_SIMULATION_SCENARIO, scenario?.name)
            }
            context.startService(intent)
        }
    }
}
