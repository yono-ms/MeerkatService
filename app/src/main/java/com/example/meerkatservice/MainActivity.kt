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
import com.example.meerkatservice.ui.screens.TabScreen
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.trace("onCreate {}", savedInstanceState)
        loggerTest()
        enableEdgeToEdge()
        setContent {
            MeerkatServiceTheme {
                TabScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logger.trace("onStart")
    }

    override fun onResume() {
        super.onResume()
        logger.trace("onResume")
    }

    override fun onPause() {
        super.onPause()
        logger.trace("onPause")
    }

    override fun onStop() {
        super.onStop()
        logger.trace("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.trace("onDestroy")
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
