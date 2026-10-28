package com.ruizburga.denunciasapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruizburga.denunciasapp.ui.components.*
import com.ruizburga.denunciasapp.ui.state.ContentState
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel

@Composable
fun BuscarScreen(viewModel: DenunciasViewModel) {
    var dni by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var searchedDni by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    val searchState by viewModel.busquedaState.collectAsState()
    val isSearching = searchState == ContentState.Loading

    ScreenBody {
        ScreenHeader("CONSULTA CIUDADANA", "Buscar por DNI", "Consulta los registros asociados a un documento.")
        Surface(color = SurfaceBase, shape = RoundedCornerShape(23.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormField(
                    label = "Documento de identidad", value = dni,
                    onValueChange = { dni = it.filter(Char::isDigit).take(8); validationError = null; hasSearched = false; searchedDni = "" },
                    placeholder = "Ingresa 8 dígitos", keyboardType = KeyboardType.Number,
                    maxLength = 8, supportingText = "La consulta requiere exactamente ocho dígitos"
                )
                AnimatedVisibility(
                    visible = validationError != null,
                    enter = fadeIn(tween(motionDurationMillis(180))) + expandVertically(tween(motionDurationMillis(180))),
                    exit = fadeOut(tween(motionDurationMillis(140))) + shrinkVertically(tween(motionDurationMillis(140)))
                ) {
                    validationError?.let { InlineNotice(it, isError = true) }
                }
                Button(
                    onClick = {
                        val cleanDni = dni.trim()
                        if (!Regex("^[0-9]{8}$").matches(cleanDni)) {
                            validationError = "El DNI debe contener exactamente 8 dígitos."
                            hasSearched = false
                        } else {
                            validationError = null
                            searchedDni = cleanDni
                            hasSearched = true
                            viewModel.buscarPorDni(cleanDni)
                        }
                    },
                    enabled = !isSearching,
                    modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.buttonMinHeight),
                    shape = RoundedCornerShape(AppDimens.controlRadius)
                ) {
                    if (isSearching) {
                        AppLoadingIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(9.dp))
                        Text("Buscando")
                    } else {
                        AppIcon(AppIconName.Search, Modifier.size(18.dp), MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(9.dp))
                        Text("Buscar denuncias")
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        if (validationError == null) {
            val mode = if (!hasSearched || searchedDni != dni.trim()) "idle" else when (searchState) {
                ContentState.Loading -> "loading"
                ContentState.Empty -> "empty"
                is ContentState.Error -> "error"
                is ContentState.Data -> "results"
            }
            StateTransition(mode, Modifier.fillMaxWidth()) { screenMode ->
                when (screenMode) {
                    "idle" -> StatePanel(
                        "Consulta por DNI",
                        "Ingresa un documento para consultar las denuncias asociadas.",
                        Modifier.fillMaxWidth(), icon = AppIconName.Search,
                        iconDescription = "Búsqueda de denuncias por DNI"
                    )
                    "loading" -> StatePanel("Buscando registros", "Consultando las denuncias asociadas.", Modifier.fillMaxWidth()) {
                        AppLoadingIndicator()
                    }
                    "empty" -> StatePanel(
                        "Sin coincidencias",
                        "No se encontraron denuncias para el DNI consultado. Puedes probar con otro documento.",
                        Modifier.fillMaxWidth(),
                        action = {
                            OutlinedButton(
                                onClick = { dni = ""; searchedDni = ""; hasSearched = false; validationError = null },
                                shape = RoundedCornerShape(AppDimens.controlRadius)
                            ) { Text("Buscar otro DNI") }
                        },
                        icon = AppIconName.Search,
                        iconDescription = "Búsqueda sin resultados"
                    )
                    "error" -> {
                        val message = (searchState as? ContentState.Error)?.message.orEmpty()
                        StatePanel("No se pudo completar la búsqueda", message, Modifier.fillMaxWidth(), action = {
                            OutlinedButton(
                                onClick = { viewModel.buscarPorDni(searchedDni) },
                                enabled = !isSearching,
                                shape = RoundedCornerShape(AppDimens.controlRadius)
                            ) { Text("Reintentar") }
                        }, icon = AppIconName.Search, iconDescription = "Error en la búsqueda")
                    }
                    else -> {
                        val records = (searchState as? ContentState.Data)?.value.orEmpty()
                        Text("${records.size} resultados", color = Muted, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(11.dp))
                        records.forEach { denuncia ->
                            key(denuncia.id) {
                                AnimatedEntry { DenunciaCard(denuncia) }
                                Spacer(Modifier.height(AppDimens.space6))
                            }
                        }
                    }
                }
            }
        }
    }
}
