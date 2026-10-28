package com.ruizburga.denunciasapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.ui.components.*
import com.ruizburga.denunciasapp.ui.state.ContentState
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel

@Composable
fun DetalleScreen(viewModel: DenunciasViewModel, selectedId: String?) {
    val detailState by viewModel.detalleState.collectAsState()
    val attentionState by viewModel.atencionState.collectAsState()
    val attendingId by viewModel.atendiendoId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(selectedId) {
        if (selectedId != null) {
            viewModel.limpiarEstadoAtencion()
            viewModel.cargarDetalle(selectedId)
        }
    }
    LaunchedEffect(attentionState, selectedId) {
        if (selectedId != null) {
            when (val result = attentionState) {
                is ContentState.Data -> {
                    snackbarHostState.showSnackbar("La denuncia #${result.value.id} fue marcada como atendida.")
                    viewModel.limpiarEstadoAtencion()
                }
                is ContentState.Error -> {
                    snackbarHostState.showSnackbar("No se pudo actualizar la denuncia: ${result.message}")
                    viewModel.limpiarEstadoAtencion()
                }
                ContentState.Loading, ContentState.Empty -> Unit
            }
        }
    }
    val detailMode = when {
        selectedId == null -> "unselected"
        detailState == ContentState.Loading || detailState == ContentState.Empty -> "loading"
        detailState is ContentState.Error -> "error"
        (detailState as? ContentState.Data)?.value?.id != selectedId -> "loading"
        else -> "data"
    }

    Box(Modifier.fillMaxSize()) {
        ScreenBody {
            ScreenHeader("SEGUIMIENTO", "Detalle de denuncia", "Información completa del caso seleccionado.")
            StateTransition(detailMode, Modifier.fillMaxWidth()) { mode ->
                when (mode) {
                    "unselected" -> StatePanel(
                        "Ningún caso seleccionado",
                        "Abre un registro desde la bandeja para consultar su detalle.",
                        Modifier.fillMaxWidth(),
                        icon = AppIconName.Document,
                        iconDescription = "Detalle sin selección"
                    )
                    "loading" -> StatePanel("Cargando detalle", "Consultando el caso seleccionado.", Modifier.fillMaxWidth()) {
                        AppLoadingIndicator()
                    }
                    "error" -> {
                        val message = (detailState as? ContentState.Error)?.message.orEmpty()
                        StatePanel("No se pudo cargar", message, Modifier.fillMaxWidth(), action = {
                            selectedId?.let { id ->
                                OutlinedButton(onClick = { viewModel.cargarDetalle(id) }, shape = RoundedCornerShape(14.dp)) {
                                    Text("Reintentar")
                                }
                            }
                        }, icon = AppIconName.Document, iconDescription = "Error al cargar detalle")
                    }
                    else -> {
                        val denuncia = (detailState as? ContentState.Data<Denuncia>)?.value
                        if (denuncia != null) {
                            DetailHero(denuncia)
                            Spacer(Modifier.height(14.dp))
                            DenunciaCard(
                                denuncia = denuncia,
                                onMarkAttended = if (denuncia.estado == "1") ({ showConfirmation = true }) else null,
                                isUpdating = attendingId == denuncia.id,
                                actionsEnabled = attendingId == null
                            )
                        }
                    }
                }
            }
        }
        ScreenSnackbar(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 10.dp))
    }

    if (showConfirmation && detailState is ContentState.Data) {
        val denuncia = (detailState as ContentState.Data<Denuncia>).value
        AlertDialog(
            onDismissRequest = { if (attendingId == null) showConfirmation = false },
            title = { Text("Marcar como atendida") },
            text = { Text("¿Confirmas que la denuncia #${denuncia.id} fue atendida? Esta acción actualizará su estado en el servicio.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        viewModel.marcarComoAtendida(denuncia)
                    },
                    enabled = attendingId == null
                ) { Text("Confirmar", color = Teal) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }, enabled = attendingId == null) { Text("Cancelar", color = Muted) }
            },
            containerColor = SurfaceRaised,
            titleContentColor = Ink,
            textContentColor = Muted,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun DetailHero(denuncia: Denuncia) {
    Surface(color = SurfaceRaised, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("IDENTIFICADOR", color = Muted, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            Text("#${denuncia.id}", color = Ink, style = MaterialTheme.typography.displaySmall)
            Text("Registro individual", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
