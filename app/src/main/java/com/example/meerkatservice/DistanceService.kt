package com.example.meerkatservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        runCatching {
            serviceScope.launch {
                while (true) {
                    logger.debug("Distance Collector is alive.")
                    delay(3_000)
                }
            }
        }.onFailure {
            logger.error("distanceCollect", it)
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
        serviceJob.cancel()
        isRunning = false
        super.onDestroy()
    }
}
