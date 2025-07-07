package com.example.meerkatservice.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.meerkatservice.database.LocationEntity
import com.example.meerkatservice.database.MyDatabase
import com.example.meerkatservice.extensions.openAppSettings
import com.example.meerkatservice.extensions.toBestString
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme
import java.util.Date

@Composable
fun DistanceScreen() {
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
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
    val dao = MyDatabase.getDatabase(context).locationDao()
    val locations by dao.getAllFlow().collectAsState(null)
    // 8a. Access the Info
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
        Spacer(Modifier.height(8.dp))
        locations?.let { items ->
            LazyColumn {
                itemsIndexed(items) { index, item ->
                    Column {
                        Text("ITEM $index : ")
                        LocationEntityContent(item)
                        HorizontalDivider()
                    }
                }
            }
        } ?: run {
            Text("Acquiring location...")
        }
    }
}

@Composable
fun LocationEntityContent(entity: LocationEntity) {
    Column {
        Text("Latitude: ${entity.latitude}")
        Text("Longitude: ${entity.longitude}")
        Text("Accuracy: ${entity.accuracy}m")
        Text("Provider: ${entity.provider}")
        Text("Distance: ${"%.2f".format(entity.distance)}m")
        Text("Timestamp: ${Date(entity.time).toBestString()}")
    }
}

@Composable
fun RationalContent(permissions: List<String>, onClick: () -> Unit) {
    val names = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mapOf<String, String>(
            Manifest.permission.ACCESS_COARSE_LOCATION to "Coarse Location",
            Manifest.permission.ACCESS_FINE_LOCATION to "Fine Location",
            Manifest.permission.POST_NOTIFICATIONS to "Notifications",
        )
    } else {
        mapOf<String, String>(
            Manifest.permission.ACCESS_COARSE_LOCATION to "Coarse Location",
            Manifest.permission.ACCESS_FINE_LOCATION to "Fine Location",
        )
    }
    // 5b. Explain to the user why your app needs this permission
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
    // 8b. Gracefully degrade your app's experience
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
fun LocationEntityContentPreview() {
    MeerkatServiceTheme {
        LocationEntityContent(
            LocationEntity(
                locationId = 1,
                latitude = 123.456,
                longitude = 789.012,
                altitude = 345.678,
                accuracy = 11.22F,
                verticalAccuracyMeters = 33.44F,
                speed = 55.66F,
                speedAccuracyMetersPerSecond = 77.88F,
                bearing = 90.12F,
                bearingAccuracyDegrees = 34.56F,
                time = 0,
                elapsedRealtimeNanos = 1_000_000,
                provider = "provider1",
                hasAccuracy = true,
                hasSpeed = true,
                hasAltitude = true,
                distance = 1.1F
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RationalContentPreview() {
    MeerkatServiceTheme {
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
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
