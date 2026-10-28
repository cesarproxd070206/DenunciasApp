package com.ruizburga.denunciasapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruizburga.denunciasapp.data.model.CrearDenunciaRequest
import com.ruizburga.denunciasapp.ui.components.*
import com.ruizburga.denunciasapp.ui.state.ContentState
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel
import java.time.LocalDate

@Composable
fun RegistrarScreen(viewModel: DenunciasViewModel) {
    var dni by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val operationState by viewModel.operacionState.collectAsState()
    val isSubmitting = operationState == ContentState.Loading

    LaunchedEffect(viewModel) {
        viewModel.limpiarEstadoOperacion()
        if (!viewModel.listaRequestCompleted.value) viewModel.cargarDenuncias(0, 5)
    }
    LaunchedEffect(operationState) {
        if (hasSubmitted) {
            when (val result = operationState) {
                is ContentState.Data -> {
                    dni = ""; fecha = ""; tipo = ""; direccion = ""; descripcion = ""
                    tipoExpanded = false
                    formError = null
                    snackbarHostState.showSnackbar("Denuncia registrada correctamente")
                    viewModel.limpiarEstadoOperacion()
                }
                is ContentState.Error -> {
                    snackbarHostState.showSnackbar("No se pudo registrar la denuncia: ${result.message}")
                    viewModel.limpiarEstadoOperacion()
                }
                ContentState.Loading, ContentState.Empty -> Unit
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ScreenBody {
            ScreenHeader("NUEVO REGISTRO", "Registrar denuncia", "Comparte la información de la incidencia para iniciar su seguimiento.")
            Surface(color = SurfaceBase, shape = RoundedCornerShape(23.dp), border = BorderStroke(1.dp, Border)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(AppDimens.space8)) {
                    FormField(
                        label = "DNI del denunciante", value = dni,
                        onValueChange = { dni = it.filter(Char::isDigit); formError = null },
                        placeholder = "8 dígitos", keyboardType = KeyboardType.Number, maxLength = 8,
                        supportingText = "Documento de identidad de ocho dígitos"
                    )
                    FormField(
                        label = "Fecha", value = fecha,
                        onValueChange = { fecha = it; formError = null },
                        placeholder = "AAAA-MM-DD", supportingText = "Formato requerido por el servicio"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        FieldLabel("Tipo de incidencia", required = true)
                        Box {
                            OutlinedButton(
                                onClick = { tipoExpanded = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = RoundedCornerShape(15.dp),
                                border = BorderStroke(1.dp, Border)
                            ) {
                                Text(if (tipo.isBlank()) "Selecciona una categoría" else tipo, color = if (tipo.isBlank()) Muted else Ink)
                                Spacer(Modifier.weight(1f))
                                AppIcon(AppIconName.ChevronDown, Modifier.size(18.dp), Teal)
                            }
                            DropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                                listOf("ROBO", "ASALTO", "VANDALISMO").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = { tipo = option; tipoExpanded = false; formError = null }
                                    )
                                }
                            }
                        }
                    }

                    FormField(
                        label = "Dirección", value = direccion,
                        onValueChange = { direccion = it; formError = null },
                        placeholder = "Lugar de la incidencia"
                    )
                    FormField(
                        label = "Descripción", value = descripcion,
                        onValueChange = { descripcion = it; formError = null },
                        placeholder = "Describe brevemente lo ocurrido", singleLine = false,
                        supportingText = "Máximo 255 caracteres"
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = formError != null,
                        enter = fadeIn(tween(motionDurationMillis(180))) + androidx.compose.animation.expandVertically(tween(motionDurationMillis(180))),
                        exit = fadeOut(tween(motionDurationMillis(140))) + androidx.compose.animation.shrinkVertically(tween(motionDurationMillis(140)))
                    ) {
                        formError?.let { InlineNotice(it, isError = true) }
                    }

                    Button(
                        onClick = {
                            val cleanDni = dni.trim()
                            val cleanDate = fecha.trim()
                            val cleanAddress = direccion.trim()
                            val cleanDescription = descripcion.trim()
                            formError = when {
                                !Regex("^[0-9]{8}$").matches(cleanDni) -> "El DNI debe contener exactamente 8 dígitos."
                                cleanDate.isBlank() -> "Ingresa la fecha de la incidencia."
                                runCatching { LocalDate.parse(cleanDate) }.isFailure -> "Usa el formato de fecha AAAA-MM-DD."
                                tipo.isBlank() -> "Selecciona un tipo de incidencia."
                                cleanAddress.isBlank() -> "Ingresa la dirección."
                                cleanAddress.length > 200 -> "La dirección admite hasta 200 caracteres."
                                cleanDescription.isBlank() -> "Ingresa una descripción."
                                cleanDescription.length > 255 -> "La descripción admite hasta 255 caracteres."
                                else -> null
                            }
                            if (formError == null) {
                                hasSubmitted = true
                                viewModel.crearDenuncia(
                                    CrearDenunciaRequest(
                                        dni = cleanDni,
                                        fecha = LocalDate.parse(cleanDate).toString(),
                                        tipo = tipo,
                                        direccion = cleanAddress,
                                        descripcion = cleanDescription
                                    )
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.buttonMinHeight),
                        shape = RoundedCornerShape(AppDimens.controlRadius)
                    ) {
                        AnimatedContent(
                            targetState = isSubmitting,
                            transitionSpec = { fadeIn(tween(motionDurationMillis(150))) togetherWith fadeOut(tween(motionDurationMillis(120))) },
                            label = "submit-button-content"
                        ) { submitting ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (submitting) {
                                    AppLoadingIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Enviando denuncia")
                                } else Text("Enviar denuncia")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        ScreenSnackbar(snackbarHostState, Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 10.dp))
    }
}
