package com.example.meerkatservice.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.meerkatservice.extensions.openAppSettings
import com.example.meerkatservice.logger
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun PermissionContainer(
    permissionsToRequest: Array<String> = arrayOf(),
    rational: @Composable (((permissions: List<String>, onClick: ()-> Unit) -> Unit))? = null,
    degrade: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)
) {

    var progress by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val initialPermissionsState = remember(permissionsToRequest) {
        permissionsToRequest.associateWith {
            // 4. Permission already granted to your app?
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    var permissionsGrantedState by remember { mutableStateOf(initialPermissionsState) }
    val shouldShowRationalState = remember { mutableStateMapOf<String, Boolean>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // 7. Does the user grant permission to your app?
        val updatedPermissionsStatus = permissionsGrantedState.toMutableMap()
        result.forEach { (permission, isGranted) ->
            logger.debug("updatedPermissionsStatus $permission $isGranted")
            updatedPermissionsStatus[permission] = isGranted
            if (!isGranted) {
                val activity = context as Activity
                shouldShowRationalState[permission] = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            } else {
                shouldShowRationalState[permission] = false
            }
            logger.info("shouldShowRationalState $permission $shouldShowRationalState")
        }
        permissionsGrantedState = updatedPermissionsStatus
        progress = false
    }
    val allPermissionsGranted = permissionsGrantedState.all { it.value }
    val anyShouldShowRational = shouldShowRationalState.any { it.value }


    LaunchedEffect(Unit) {
        logger.trace("LaunchedEffect START")
        // 3. Wait for the user to specific action
        // 4. Permission already granted to your app?
        if (allPermissionsGranted) {
            logger.trace("4. Permission already granted? YES")
            progress = false
        } else {
            logger.trace("4. Permission already granted? NO")
            // 5a. Show a rationale to the user?
            permissionsGrantedState.filter { it.value == false }.forEach {
                val activity = context as Activity
                shouldShowRationalState[it.key] = ActivityCompat.shouldShowRequestPermissionRationale(activity, it.key)
            }
            logger.info("shouldShowRationalState=$shouldShowRationalState")
            if (anyShouldShowRational) {
                logger.trace("5a. Show a rationale to the user? YES")
            } else {
                logger.trace("5a. Show a rationale to the user? NO")
                // 6. Request the permission to show the system dialog
                permissionLauncher.launch(permissionsToRequest)
            }
        }
        logger.trace("LaunchedEffect END")
    }
    DisposableEffect(lifecycleOwner) {
        logger.trace("DisposableEffect START")
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                logger.trace("ON_RESUME")
                val state = permissionsToRequest.associateWith {
                    // 4. Permission already granted to your app?
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                permissionsGrantedState = state.toMutableMap()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        logger.trace("DisposableEffect END")

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 4. Permission already granted to your app?
    if (allPermissionsGranted) {
        // 8a. Access the Info
        content()
    } else {
        Column {
            // 5a. Show a rationale to the user?
            if (anyShouldShowRational) {
                // 5b. Explain to the user why your app needs this permission
                rational?.let { rationalContent ->
                    val list = shouldShowRationalState.filterValues { it }.keys.toList()
                    rationalContent(list) {
                        // 6. Request the permission to show the system dialog
                        permissionLauncher.launch(permissionsToRequest)
                    }
                } ?: run {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("The following permissions are required")
                        Spacer(modifier = Modifier.height(8.dp))
                        shouldShowRationalState.forEach {
                            Text(it.key)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            // 6. Request the permission to show the system dialog
                            permissionLauncher.launch(permissionsToRequest)
                        }) {
                            Text("Get Permissions")
                        }
                    }
                }
            } else {
                if (progress) {
                    // 3. Wait for user to request specific action
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // 8b. Gracefully degrade your app's experience
                    degrade?.let { degradeContent ->
                        degradeContent()
                    } ?: run {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("This Screen needs Permissions")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                context.openAppSettings()
                            }) {
                                Text("Goto App Settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionContainerPreview() {
    MeerkatServiceTheme {
        PermissionContainer(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ) { Text("TEST CONTENT") }
    }
}
