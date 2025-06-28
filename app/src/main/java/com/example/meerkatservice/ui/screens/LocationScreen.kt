package com.example.meerkatservice.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meerkatservice.LocationTrackingService
import com.example.meerkatservice.LocationTrackingViewModel
import com.example.meerkatservice.logger
import com.example.meerkatservice.ui.dialogs.RationalDialog
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun LocationScreen() {

    val context = LocalContext.current

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val initialPermissionsState = remember(permissionsToRequest) {
        permissionsToRequest.associateWith {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    var permissionsGrantedState by remember { mutableStateOf(initialPermissionsState) }

    val shouldShowRationalState = remember { mutableStateMapOf<String, Boolean>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val updatedPermissionsStatus = permissionsGrantedState.toMutableMap()
        result.forEach { (permission, isGranted) ->
            logger.debug("$permission $isGranted")
            updatedPermissionsStatus[permission] = isGranted
            if (!isGranted) {
                val activity = context as Activity
                shouldShowRationalState[permission] = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                logger.info("shouldShowRationalState $permission $shouldShowRationalState")
            } else {
                shouldShowRationalState[permission] = false
            }
        }
        permissionsGrantedState = updatedPermissionsStatus
    }

    val allPermissionsGranted = permissionsGrantedState.all { it.value }
    val anyShouldShowRational = shouldShowRationalState.any { it.value }

    var serviceIsAlive by rememberSaveable { mutableStateOf(LocationTrackingService.isRunning) }

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Permissions Status", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            permissionsGrantedState.forEach { (permission, isGranted) ->
                Row {
                    val title = permission.substringAfterLast('.', missingDelimiterValue = "unknown")
                    Text(text = title)
                    Spacer(modifier = Modifier.weight(1F))
                    Text(text = if (isGranted) "Granted" else "Denied")
                }
            }
        }
        if (allPermissionsGranted) {
            if (serviceIsAlive) {
                LocationContent { serviceIsAlive = false}
            } else {
                Button(onClick = {
                    serviceIsAlive = true
                }) {
                    Text(text = "Start Foreground Service")
                }
            }
        } else {
            Column {
                Button(onClick = {
                    permissionLauncher.launch(permissionsToRequest)
                }) {
                    Text(text = "Get Permission")
                }
                Button(onClick = {
                    openAppSettings(context)
                }) {
                    Text(text = "Application Setting")
                }
            }
        }
    }
    if (anyShouldShowRational) {
        RationalDialog {
            shouldShowRationalState.clear()
        }
    }
}

fun openAppSettings(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).also { intent ->
        context.startActivity(intent)
    }
}

@Composable
fun LocationContent(viewModel: LocationTrackingViewModel = viewModel(), onStop: () -> Unit) {

    val context = LocalContext.current

    val isBound by viewModel.isServiceBound.collectAsStateWithLifecycle()
    val counter by viewModel.serviceCounter.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()

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
        Text(text = "LocationScreen is bound $isBound")
        if (isBound) {
            Text(text = "counter=$counter")
            currentLocation?.let { location ->
                Text("Latitude: ${location.latitude}")
                Text("Longitude: ${location.longitude}")
                Text("Accuracy: ${location.accuracy}m")
                Text("Provider: ${location.provider}")
                Text("Timestamp: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(location.time))}")
            } ?: run {
                Text("Acquiring location...")
            }
            locationError?.let { error ->
                Text(text = error)
            }
            Button(onClick = {
                viewModel.callServiceMethod()
            }) {
                Text(text = "increment")
            }
            Button(onClick = {
                viewModel.unbindFromService(context)
                viewModel.stopService(context)
                onStop()
            }) {
                Text(text = "Stop Service")
            }
        } else {
            Button(onClick = {
                viewModel.bindToService(context)
            }) {
                Text(text = "Start Service")
            }
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
