package com.example.meerkatservice.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meerkatservice.LocationTrackingViewModel
import com.example.meerkatservice.logger
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    var permissionNotification by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    var permissionCoarseLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionFineLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.forEach { (permission, isGranted) ->
            logger.debug("$permission $isGranted")
            when (permission) {
                android.Manifest.permission.POST_NOTIFICATIONS -> permissionNotification = isGranted
                android.Manifest.permission.ACCESS_COARSE_LOCATION -> permissionCoarseLocation = isGranted
                android.Manifest.permission.ACCESS_FINE_LOCATION -> permissionFineLocation = isGranted
                else -> logger.error("unknown permission.")
            }
        }
    }

    Column {
        Text(text = "Permission")
        Text(text = "Notification $permissionNotification")
        Text(text = "CourseLocation $permissionCoarseLocation")
        Text(text = "FineLocation $permissionFineLocation")
        if (permissionNotification && permissionCoarseLocation && permissionFineLocation) {
            LocationContent()
        } else {
            Column {
                Button(onClick = {
                    val permissionsToRequest = arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    permissionLauncher.launch(permissionsToRequest)
                }) {
                    Text(text = "Get Permission")
                }
            }
        }
    }
}

@Composable
fun LocationContent(viewModel: LocationTrackingViewModel = viewModel()) {

    val context = LocalContext.current

    val isBound by viewModel.isServiceBound.collectAsStateWithLifecycle()
    val counter by viewModel.serviceCounter.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.bindToService(context)
        onDispose {
            viewModel.unbindFromService(context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "LocationScreen is bound")
        Text(text = "counter=$counter")
        if (isBound) {
            Button(onClick = {
                viewModel.callServiceMethod()
            }) {
                Text(text = "increment")
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    MeerkatServiceTheme {
        LocationScreen()
    }
}
