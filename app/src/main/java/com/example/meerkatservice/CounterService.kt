package com.example.meerkatservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CounterService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "CounterChannelId"
        private const val NOTIFICATION_CHANNEL_NAME = "CounterChannelName"
        private const val NOTIFICATION_ID = 2

        /**
         * フォアグラウンドサービスが動いているかどうかをCompose関数から確認する
         */
        var isRunning = false
    }

    inner class LocalBinder : Binder() {
        fun getService(): CounterService = this@CounterService
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
            .setContentTitle("Counter Service")
            .setContentText("Counter Service is running in the foreground.")
            .setSmallIcon(R.drawable.baseline_thumb_up_24) // ★必ず適切なアイコンリソースを指定してください
            .setContentIntent(pendingIntent) // 通知タップ時のアクション
            .setOngoing(true) // ユーザーがスワイプで消せないようにする
            .build()
        return notification
    }

    val counterFlow = MutableStateFlow(0)

    override fun onCreate() {
        super.onCreate()
        logger.trace("onCreate")
        serviceScope.launch {
            runCatching {
                while (true) {
                    logger.debug("counterFlow=${counterFlow.value}")
                    delay(3_000)
                    counterFlow.value++
                }
            }.onFailure {
                logger.error(it.localizedMessage)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.trace("onStartCommand intent={}, flags={} startId={}", intent, flags, startId)
        return START_NOT_STICKY
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
