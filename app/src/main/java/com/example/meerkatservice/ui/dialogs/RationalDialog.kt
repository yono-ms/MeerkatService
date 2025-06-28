package com.example.meerkatservice.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

@Composable
fun RationalDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "OK")
            }
        },
        icon = {
            Icon(Icons.Filled.Info, contentDescription = "Information Icon")
        },
        title = {
            Text(text = "Permission")
        },
        text = {
            Text(text = "This App needs permissions.")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RationalDialogPreview() {
    MeerkatServiceTheme {
        RationalDialog {}
    }
}
