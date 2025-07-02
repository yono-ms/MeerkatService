package com.example.meerkatservice.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.meerkatservice.CounterService
import com.example.meerkatservice.logger
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun CounterScreen() {
    val context = LocalContext.current
    val serviceIntent = Intent(context, CounterService::class.java)

    var counterService by remember { mutableStateOf<CounterService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                logger.info("onServiceConnected")
                (service as? CounterService.LocalBinder)?.let {
                    counterService = it.getService()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                logger.info("onServiceDisconnected")
                counterService = null
            }
        }
    }

    DisposableEffect(Unit) {
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            context.unbindService(serviceConnection)
            counterService = null
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(), // 画面全体に表示
        verticalArrangement = Arrangement.Center, // 垂直方向に中央揃え
        horizontalAlignment = Alignment.CenterHorizontally // 水平方向に中央揃え
    ) {
        counterService?.let {
            val counter by it.counterFlow.collectAsState(0)
            Text(text = "COUNTER=${counter}")
        } ?: run {
            Text(text = "Connecting...")
        }
    }
}

@Preview(showBackground = true) // showBackground = true でプレビューに背景を表示
@Composable
fun CounterScreenPreview() {
    // プレビュー用にテーマを適用することもできます（オプション）
    MeerkatServiceTheme { // あなたのアプリのテーマに置き換えてください
        CounterScreen()
    }
}
