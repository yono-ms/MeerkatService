package com.example.meerkatservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LocationTrackingService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "LocationTrackingChannelId"
        private const val NOTIFICATION_CHANNEL_NAME = "LocationTrackingChannelName"
        private const val NOTIFICATION_ID = 1

        /**
         * フォアグラウンドサービスが動いているかどうかをCompose関数から確認する
         */
        var isRunning = false
    }

    // Binder given to clients
    private val binder = LocalBinder()

    // 1. 独自の CoroutineScope を作成
    private val serviceJob = SupervisorJob()
    // DefaultDispatcherを使用する例。IO処理がメインならDispatchers.IOも可
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // Example data that the service might manage and expose
    private val _currentCounter = MutableStateFlow(0)
    val currentCounter: StateFlow<Int> = _currentCounter

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 位置情報リクエストの設定
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(10))
        .setWaitForAccurateLocation(false) // 状況に応じてtrueにする場合もある
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)) // 最短更新間隔
        .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(20)) // 最大遅延時間
        .build()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of MyBoundForegroundService so clients can call public methods
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        logger.trace("onCreate")
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.trace("onStartCommand")

        // 通知チャンネル
        prepareNotificationChannel()

        // 通知をタップしたときに MainActivity を開くための PendingIntent
        val pendingIntent = preparePendingIntent()

        // フォアグラウンドサービス用の通知を作成
        val notification: Notification = prepareNotification(pendingIntent)

        // フォアグラウンドサービスのタイプ (Android Q 以降で推奨)
        val foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                foregroundServiceType
            )
            logger.info("Foreground service started successfully.")
        } catch (e: Exception) {
            logger.error("Error starting foreground service: ${e.message}", e)
            // ForegroundServiceStartNotAllowedException (API 31+) や
            // RemoteServiceException (API 30以前の "Bad notification") など
            stopSelf() // エラー時はサービスを停止
        }

        // ここでバックグラウンドタスクを実行できます。
        startLocationUpdates(this)

        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission") // パーミッションチェックは呼び出し元で行う
    fun startLocationUpdates(context: Context) {
        if (!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    serviceScope.launch {
                        _currentLocation.value = location
                        _locationError.value = null // エラーがあればクリア
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    serviceScope.launch {
                        _locationError.value = "Location is currently unavailable."
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // メインスレッドでコールバックを受け取る
            )
            _locationError.value = null // 開始時にエラーをクリア
        } catch (e: SecurityException) {
            // これはパーミッションがない場合に発生する可能性があるが、
            // 呼び出し側でパーミッションチェックを行う前提
            _locationError.value = "Location permission not granted. Cannot start updates."
            // この例外は実際には呼び出し元のパーミッションチェックで防がれるべき
        } catch (e: Exception) {
            _locationError.value = "Error starting location updates: ${e.message}"
        }
    }

    fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            // _currentLocation.value = null // (オプション) 更新停止時に位置情報をクリアする場合
        }
    }

    private fun prepareNotification(pendingIntent: PendingIntent?): Notification {
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Simple Foreground Service")
            .setContentText("Service is running in the foreground.")
            .setSmallIcon(R.drawable.baseline_location_on_24) // ★必ず適切なアイコンリソースを指定してください
            .setContentIntent(pendingIntent) // 通知タップ時のアクション
            .setOngoing(true) // ユーザーがスワイプで消せないようにする
            .build()
        return notification
    }

    private fun preparePendingIntent(): PendingIntent? {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // requestCode
            notificationIntent,
            pendingIntentFlags
        )
        return pendingIntent
    }

    private fun prepareNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder {
        logger.trace("onBind {}", intent)
        return binder
    }

    override fun onDestroy() {
        logger.trace("onDestroy")
        stopLocationUpdates()
        serviceScope.cancel()
        isRunning = false
        super.onDestroy()
    }

    fun incrementCounter() {
        logger.trace("incrementCounter")
        _currentCounter.value++
    }

    fun requestStopService() {
        logger.trace("requestStopService")
        stopSelf()
    }
}
