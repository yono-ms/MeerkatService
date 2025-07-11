package com.example.meerkatservice.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.meerkatservice.extensions.oldLocationScreenFlow
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabScreen() {
    val context = LocalContext.current
    val oldLocationScreen by context.oldLocationScreenFlow.collectAsState(false)

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        NavItem("Location", Icons.Filled.LocationOn, Icons.Outlined.LocationOn, "location"),
        NavItem("Counter", Icons.Filled.ThumbUp, Icons.Outlined.ThumbUp, "counter")
    )
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("TITLE") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedItemIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (navItems[selectedItemIndex].route) {
                "home" -> {
                    MainScreen()
                }

                "location" -> {
                    if (oldLocationScreen) {
                        LocationScreen()
                    } else {
                        DistanceScreen()
                    }
                }

                "counter" -> {
                    CounterScreen()
                }

                else -> {
                    MainScreen()
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabScreenPreview() {
    MeerkatServiceTheme {
        TabScreen()
    }
}
