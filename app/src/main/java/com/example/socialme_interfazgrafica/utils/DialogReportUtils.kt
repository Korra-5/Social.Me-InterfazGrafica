package com.example.socialme_interfazgrafica.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialme_interfazgrafica.R

object DialogReportUtils {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ReportDialog(
        onDismiss: () -> Unit,
        onConfirm: (motivo: String, cuerpo: String) -> Unit,
        isLoading: Boolean,
        reportReason: MutableState<String>,
        reportBody: MutableState<String>
    ) {
        val context = LocalContext.current

        // Verificar que los campos no estén vacíos
        val isFormValid = reportReason.value.isNotBlank() && reportBody.value.isNotBlank()

        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = {
                Text(
                    text = "Reportar contenido",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Por favor, indique el motivo de la denuncia y proporcione detalles adicionales.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    // Sección de motivo
                    Column {
                        Text(
                            text = "Motivo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = reportReason.value,
                            onValueChange = { reportReason.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            singleLine = true,
                            placeholder = { Text("Ej: Contenido inapropiado") },
                            enabled = !isLoading
                        )
                    }

                    // Sección de descripción
                    Column {
                        Text(
                            text = "Descripción detallada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = reportBody.value,
                            onValueChange = { reportBody.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            singleLine = false,
                            placeholder = { Text("Describa el problema con más detalle") },
                            enabled = !isLoading
                        )
                    }

                    // Indicador de validación
                    if (!isFormValid && (reportReason.value.isNotEmpty() || reportBody.value.isNotEmpty())) {
                        Text(
                            text = "Por favor, complete ambos campos",
                            color = colorResource(R.color.error),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(reportReason.value, reportBody.value) },
                    enabled = isFormValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Enviar denuncia")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Cancelar",
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}