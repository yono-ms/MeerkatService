package com.example.meerkatservice.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meerkatservice.LocationTrackingViewModel
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun LocationScreen(viewModel: LocationTrackingViewModel = viewModel()) {
    val context = LocalContext.current
    val isBound by viewModel.isServiceBound.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.bindToService(context)
        onDispose {
            viewModel.unbindFromService(context)
        }
    }
    if (isBound) {
        Text(text = "LocationScreen is bound")
    } else {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenBoundPreview() {
    MeerkatServiceTheme {
        LocationScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenNotBoundPreview() {
    MeerkatServiceTheme {
        LocationScreen()
    }
}
