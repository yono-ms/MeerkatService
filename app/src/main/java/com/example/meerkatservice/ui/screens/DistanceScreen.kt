package com.example.meerkatservice.ui.screens

import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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

    PermissionContainer(permissionsToRequest) {
        DistanceContent()
    }
}

@Composable
fun DistanceContent() {
    Text("TEST")
}

@Preview(showBackground = true)
@Composable
fun DistanceScreenPreview() {
    MeerkatServiceTheme {
        DistanceScreen()
    }
}
