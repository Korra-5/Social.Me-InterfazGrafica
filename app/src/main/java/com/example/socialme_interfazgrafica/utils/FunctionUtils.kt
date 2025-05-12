package com.example.socialme_interfazgrafica.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.socialme_interfazgrafica.R

object FunctionUtils {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ReportDialog(
        onDismiss: () -> Unit,
        onConfirm: (String, String) -> Unit,
        isLoading: Boolean,
        reportReason: MutableState<String>,
        reportBody: MutableState<String>
    ) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = { Text("Reportar contenido") },
            text = {
                Column {
                    Text(
                        "Por favor, indique el motivo de la denuncia y proporcione detalles adicionales.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = reportReason.value,
                        onValueChange = { reportReason.value = it },
                        label = { Text("Motivo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = reportBody.value,
                        onValueChange = { reportBody.value = it },
                        label = { Text("Descripción detallada") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(reportReason.value, reportBody.value) },
                    enabled = !isLoading && reportReason.value.isNotEmpty() && reportBody.value.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar denuncia")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}