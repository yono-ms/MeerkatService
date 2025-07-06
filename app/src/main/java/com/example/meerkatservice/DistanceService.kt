package com.example.meerkatservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.meerkatservice.database.LocationEntity
import com.example.meerkatservice.database.MyDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DistanceService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "DistanceChannelId"
        private const val NOTIFICATION_CHANNEL_NAME = "Distance Channel"
        private const val NOTIFICATION_ID = 3

        /**
         * フォアグラウンドサービスが動いているかどうかをCompose関数から確認する
         */
        var isRunning = false
    }

    inner class LocalBinder : Binder() {
        fun getService(): DistanceService = this@DistanceService
    }

    private val binder = LocalBinder()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 位置情報リクエストの設定
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        TimeUnit.SECONDS.toMillis(10)
    )
        .setWaitForAccurateLocation(false) // 状況に応じてtrueにする場合もある
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)) // 最短更新間隔
        .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(20)) // 最大遅延時間
        .build()

    private fun prepareNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun preparePendingIntent(): PendingIntent? {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            this,
            1, // requestCode
            notificationIntent,
            pendingIntentFlags
        )
        return pendingIntent
    }

    private fun prepareNotification(pendingIntent: PendingIntent?): Notification {
        val notification: Notification = NotificationCompat.Builder(this,
            NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("Distance Service")
            .setContentText("Distance Service is running in the foreground.")
            .setSmallIcon(R.drawable.baseline_location_on_24) // ★必ず適切なアイコンリソースを指定してください
            .setContentIntent(pendingIntent) // 通知タップ時のアクション
            .setOngoing(true) // ユーザーがスワイプで消せないようにする
            .build()
        return notification
    }

    override fun onCreate() {
        super.onCreate()
        logger.trace("onCreate")
        isRunning = true
        prepareNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.trace("onStartCommand intent={}, flags={} startId={}", intent, flags, startId)
        val pendingIntent = preparePendingIntent()
        val notification = prepareNotification(pendingIntent)
        val foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        runCatching {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundServiceType)
            logger.info("Foreground service started successfully.")
        }.onFailure {
            logger.error("onStartCommand", it)
            stopSelf()
        }

        distanceCollect()

        return START_NOT_STICKY
    }

    private fun distanceCollect() {
        startLocationUpdates()
//        runCatching {
//            serviceScope.launch {
//                while (true) {
//                    logger.debug("Distance Collector is alive.")
//                    delay(3_000)
//                }
//            }
//        }.onFailure {
//            logger.error("distanceCollect", it)
//        }
    }

    @SuppressLint("MissingPermission") // パーミッションチェックは呼び出し元で行う
    private fun startLocationUpdates() {
        logger.trace("startLocationUpdates")
        if (!::fusedLocationClient.isInitialized) {
            logger.trace("startLocationUpdates getFusedLocationProviderClient")
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        locationCallback = object : LocationCallback() {
            val dao = MyDatabase.getDatabase(applicationContext).locationDao()

            override fun onLocationResult(locationResult: LocationResult) {
                logger.trace("onLocationResult {}", locationResult)
                serviceScope.launch {
                    runCatching {
                        val entities = LocationEntity.fromLocations(locationResult.locations)
                        dao.insert(entities)
                    }.onFailure {
                        logger.error("onLocationResult", it)
                        _locationError.value = it.localizedMessage
                    }
                }
                super.onLocationResult(locationResult)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                logger.trace("onLocationAvailability {}", locationAvailability)
                super.onLocationAvailability(locationAvailability)
            }
        }

        runCatching {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // メインスレッドでコールバックを受け取る
            )
            _locationError.value = null
        }.onFailure {
            logger.error("requestLocationUpdates", it)
            when (it) {
                is SecurityException -> {
                    _locationError.value = "Location permission not granted. Cannot start updates."
                }

                else -> {
                    _locationError.value = "Error starting location updates: ${it.message}"
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        logger.trace("stopLocationUpdates")
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        logger.trace("onBind {}", intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logger.trace("onUnbind {}", intent)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        logger.trace("onDestroy")
        stopLocationUpdates()
        serviceJob.cancel()
        isRunning = false
        super.onDestroy()
    }
}
