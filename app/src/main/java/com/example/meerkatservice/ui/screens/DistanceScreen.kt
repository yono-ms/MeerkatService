package com.example.meerkatservice.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meerkatservice.DistanceService
import com.example.meerkatservice.extensions.openAppSettings
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun DistanceScreen() {
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

    PermissionContainer(
        permissionsToRequest = permissionsToRequest,
        rational = { permissions, onClick ->
            RationalContent(permissions = permissions, onClick = onClick)
        },
        degrade = {
            DegradeContent()
        }
    ) {
        DistanceContent()
    }
}

@Composable
fun DistanceContent() {
    val context = LocalContext.current
    val serviceIntent = Intent(context, DistanceService::class.java)
    var isRunning by remember { mutableStateOf(DistanceService.isRunning) }
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Distance service")
            Spacer(Modifier.weight(1F))
            Switch(checked = isRunning, onCheckedChange = { checked ->
                if (checked) {
                    context.startForegroundService(serviceIntent)
                    isRunning = true
                } else {
                    context.stopService(serviceIntent)
                    isRunning = false
                }
            })
        }
    }
}

@Composable
fun RationalContent(permissions: List<String>, onClick: () -> Unit) {
    val names = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mapOf<String, String>(
            android.Manifest.permission.ACCESS_COARSE_LOCATION to "Coarse Location",
            android.Manifest.permission.ACCESS_FINE_LOCATION to "Fine Location",
            android.Manifest.permission.POST_NOTIFICATIONS to "Notifications",
        )
    } else {
        mapOf<String, String>(
            android.Manifest.permission.ACCESS_COARSE_LOCATION to "Coarse Location",
            android.Manifest.permission.ACCESS_FINE_LOCATION to "Fine Location",
        )
    }
    Column {
        Text("The following permissions are required")
        Spacer(modifier = Modifier.height(8.dp))
        permissions.forEach {
            Text(names[it].toString())
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClick) {
            Text("Get Permission.")
        }
    }
}

@Composable
fun DegradeContent() {
    val context = LocalContext.current
    Column {
        Text("Location Screen needs Permissions")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            context.openAppSettings()
        }) {
            Text("Goto App Settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DistanceContentPreview() {
    MeerkatServiceTheme {
        DistanceContent()
    }
}

@Preview(showBackground = true)
@Composable
fun RationalContentPreview() {
    MeerkatServiceTheme {
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
        RationalContent(list) {}
    }
}

@Preview(showBackground = true)
@Composable
fun DegradeContentPreview() {
    MeerkatServiceTheme {
        DegradeContent()
    }
}
