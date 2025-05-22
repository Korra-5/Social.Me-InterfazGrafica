// components/NotificacionIndicator.kt
package com.example.socialme_interfazgrafica.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificacionIndicator(
    count: Long,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    Badge {
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notificaciones"
            )
        }
    }
}