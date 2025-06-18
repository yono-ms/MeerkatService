package com.example.meerkatservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.meerkatservice.ui.screens.LoadingScreen
import com.example.meerkatservice.ui.screens.MainScreen
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainActivity : ComponentActivity() {

    private var myService: LocationTrackingService? = null
    private var isBound = MutableStateFlow(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            logger.trace("onServiceConnected")
            val binder = service as LocationTrackingService.LocalBinder
            myService = binder.getService()
            isBound.value = true
            // サービスに接続された後の処理
            // 例: myService?.doSomething()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            logger.trace("onServiceDisconnected")
            isBound.value = false
            myService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.trace("onCreate {}", savedInstanceState)
        loggerTest()
        enableEdgeToEdge()
        setContent {
            MeerkatServiceTheme {
                val isBoundService by isBound.collectAsState()
                if (isBoundService) {
                    MainScreen()
                } else {
                    LoadingScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logger.trace("onStart")
        // サービスを開始 (まだ開始されていない場合)
        checkAndStart()
    }

    private fun startLocationDependentFeature(isPrecise: Boolean = true) {
        Intent(this, LocationTrackingService::class.java).also { intent ->
            // フォアグラウンドサービスとして開始する必要がある場合
            startForegroundService(intent)
            // その後バインド
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    private fun checkAndStart() {
        logger.trace("checkAndStart")
        startLocationDependentFeature()
    }

    override fun onStop() {
        super.onStop()
        logger.trace("onStop")
        if (isBound.value) {
            unbindService(connection)
            isBound.value = false
            myService = null // 参照をクリア
        }
    }

    private fun loggerTest() {
        logger.trace("Logger TEST")
        logger.debug("Logger TEST")
        logger.info("Logger TEST")
        logger.warn("Logger TEST")
        logger.error("Logger TEST")
    }
}

val logger: Logger by lazy { LoggerFactory.getLogger("MeerkatS") }
