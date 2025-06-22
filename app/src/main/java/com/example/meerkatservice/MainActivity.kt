package com.example.meerkatservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.meerkatservice.ui.screens.TabScreen
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme
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
