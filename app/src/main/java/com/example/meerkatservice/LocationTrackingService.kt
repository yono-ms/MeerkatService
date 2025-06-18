package com.example.meerkatservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationTrackingService : Service() {

    private val TAG = "LocationTrackingService"
    private val NOTIFICATION_CHANNEL_ID = "LocationTrackingChannel"
    private val NOTIFICATION_ID = 1

    // Binder given to clients
    private val binder = LocalBinder()

    // Example data that the service might manage and expose
    private val _currentCounter = MutableStateFlow(0)
    val currentCounter: StateFlow<Int> = _currentCounter

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
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 通知をタップしたときに MainActivity を開くための PendingIntent
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // requestCode
            notificationIntent,
            pendingIntentFlags
        )

        // フォアグラウンドサービス用の通知を作成
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Simple Foreground Service")
            .setContentText("Service is running in the foreground.")
            .setSmallIcon(R.drawable.baseline_location_on_24) // ★必ず適切なアイコンリソースを指定してください
            .setContentIntent(pendingIntent) // 通知タップ時のアクション
            .setOngoing(true) // ユーザーがスワイプで消せないようにする
            .build()

        // フォアグラウンドサービスのタイプ (Android Q 以降で推奨)
        // この例では具体的なタスクがないため、 ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE を使用
        // または、特定のタスクがある場合は適切なタイプ (例: ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) を指定
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0 // Q未満ではこの引数は使われない
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                foregroundServiceType
            )
            Log.i(TAG, "Foreground service started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}", e)
            // ForegroundServiceStartNotAllowedException (API 31+) や
            // RemoteServiceException (API 30以前の "Bad notification") など
            stopSelf() // エラー時はサービスを停止
        }

        // ここでバックグラウンドタスクを実行できます。
        // この例では、ログを出力するだけの簡単な処理を行います。
//        Thread {
//            for (i in 1..100) {
//                try {
//                    Log.d(TAG, "Service is doing work: $i")
//                    Thread.sleep(2000) // 2秒待機
//                } catch (e: InterruptedException) {
//                    Thread.currentThread().interrupt() // スレッドの中断を処理
//                    Log.d(TAG, "Work thread interrupted")
//                    break
//                }
//                if (Thread.currentThread().isInterrupted) {
//                    Log.d(TAG, "Work thread was interrupted, exiting loop.")
//                    break
//                }
//            }
//            Log.d(TAG, "Service work finished.")
//            // タスク完了後、必要に応じてサービスを停止
//            // stopSelf()
//        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        logger.trace("onBind {}", intent)
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.trace("onDestroy")
    }

    private fun createNotificationChannel() {
        // Android Oreo (API 26) 以降では通知チャンネルの作成が必須
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Simple Foreground Service Channel", // ユーザーに見えるチャンネル名
                NotificationManager.IMPORTANCE_LOW // 通知の重要度 (音やバイブなし、邪魔にならないように)
            ).apply {
                description = "Channel for Simple Foreground Service"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            Log.d(TAG, "Notification channel created.")
        }
    }
}
