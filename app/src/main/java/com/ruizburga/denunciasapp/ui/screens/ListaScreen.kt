package com.ruizburga.denunciasapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.ui.components.*
import com.ruizburga.denunciasapp.ui.state.ContentState
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel

@Composable
fun ListaScreen(
    viewModel: DenunciasViewModel,
    onOpenDetail: (String) -> Unit,
    onOpenHome: () -> Unit = {}
) {
    val state by viewModel.listaState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val paginationError by viewModel.paginationError.collectAsState()
    val attentionState by viewModel.atencionState.collectAsState()
    val attendingId by viewModel.atendiendoId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var retainedRecords by remember(viewModel) { mutableStateOf<List<Denuncia>?>(null) }
    var observedInitialRequest by remember(viewModel) { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (val result = state) {
            ContentState.Loading -> observedInitialRequest = true
            ContentState.Empty -> if (observedInitialRequest) retainedRecords = null
            is ContentState.Data -> {
                retainedRecords = result.value
                observedInitialRequest = true
            }
            is ContentState.Error -> observedInitialRequest = true
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.limpiarEstadoAtencion()
        if (!viewModel.listaRequestCompleted.value) {
            viewModel.cargarDenuncias(offset = 0, limit = 5)
        }
    }
    LaunchedEffect(attentionState) {
        when (val result = attentionState) {
            is ContentState.Data -> {
                snackbarHostState.showSnackbar("La denuncia #${result.value.id} fue marcada como atendida.")
                viewModel.limpiarEstadoAtencion()
            }
            is ContentState.Error -> {
                snackbarHostState.showSnackbar("No se pudo marcar la denuncia: ${result.message}")
                viewModel.limpiarEstadoAtencion()
            }
            ContentState.Loading, ContentState.Empty -> Unit
        }
    }

    Box(Modifier.fillMaxSize()) {
        ScreenBody {
            TextButton(onClick = onOpenHome) {
                AppIcon(AppIconName.Home, Modifier.size(AppDimens.iconSmall), Teal, contentDescription = "Inicio")
                Spacer(Modifier.width(AppDimens.space2))
                Text("Inicio")
            }
            ScreenHeader("SEGURIDAD CIUDADANA", "Denuncias", "Bandeja de seguimiento y atención de incidencias.")
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Bandeja activa", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(3.dp))
                    Text("Registros recibidos desde el servicio", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                AppIcon(AppIconName.Document, Modifier.size(22.dp), Teal)
            }
            Spacer(Modifier.height(15.dp))

            val visibleRecords = (state as? ContentState.Data)?.value ?: retainedRecords
            val mode = when {
                visibleRecords != null -> "data"
                state == ContentState.Loading || !observedInitialRequest -> "loading"
                state == ContentState.Empty -> "empty"
                else -> "error"
            }

            when (mode) {
                "loading" -> StatePanel(
                    "Cargando denuncias",
                    "Conectando con el servicio ciudadano.",
                    Modifier.fillMaxWidth(),
                    action = { AppLoadingIndicator() }
                )
                "empty" -> StatePanel(
                    "Sin denuncias",
                    "Cuando existan registros en el servicio, aparecerán aquí.",
                    Modifier.fillMaxWidth(),
                    icon = AppIconName.Document,
                    iconDescription = "Bandeja de denuncias vacía",
                    action = {
                        OutlinedButton(
                            onClick = { viewModel.cargarDenuncias(0, 5) },
                            shape = RoundedCornerShape(AppDimens.controlRadius)
                        ) { Text("Volver a cargar") }
                    }
                )
                "error" -> StatePanel(
                    "No se pudo cargar",
                    (state as? ContentState.Error)?.message.orEmpty(),
                    Modifier.fillMaxWidth(),
                    icon = AppIconName.Document,
                    iconDescription = "Error al cargar denuncias",
                    action = {
                        OutlinedButton(
                            onClick = { viewModel.cargarDenuncias(0, 5) },
                            shape = RoundedCornerShape(AppDimens.controlRadius)
                        ) { Text("Reintentar") }
                    }
                )
                else -> {
                    val records = visibleRecords.orEmpty()
                    val refreshError = (state as? ContentState.Error)?.message
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.space6)
                    ) {
                        if (state == ContentState.Loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.space4)
                            ) {
                                AppLoadingIndicator(Modifier.size(18.dp), strokeWidth = 1.8.dp)
                                Text("Actualizando la bandeja…", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (refreshError != null) {
                            InlineNotice("No se pudo actualizar la bandeja: $refreshError", isError = true)
                            TextButton(onClick = { viewModel.cargarDenuncias(0, 5) }) { Text("Reintentar") }
                        }

                        Surface(
                            color = SurfaceRaised,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${records.size} registros cargados", color = Ink, style = MaterialTheme.typography.labelLarge)
                                Text("5 por página", color = Muted, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        records.forEach { denuncia ->
                            key(denuncia.id) {
                                AnimatedEntry {
                                    DenunciaCard(
                                        denuncia = denuncia,
                                        onMarkAttended = { viewModel.marcarComoAtendida(denuncia) },
                                        onOpenDetail = { onOpenDetail(denuncia.id) },
                                        isUpdating = attendingId == denuncia.id,
                                        actionsEnabled = attendingId == null
                                    )
                                }
                            }
                        }

                        // Pagination feedback is a separate column section after all record cards.
                        if (paginationError != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.space2)) {
                                InlineNotice("No se pudieron cargar los siguientes registros: $paginationError", isError = true)
                                TextButton(
                                    onClick = { viewModel.cargarSiguientesDenuncias(5) },
                                    enabled = !isLoadingMore
                                ) { Text("Reintentar carga") }
                            }
                        }
                        if (records.size > 5) {
                            OutlinedButton(
                                onClick = { viewModel.cargarDenuncias(0, 5) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.buttonMinHeight),
                                shape = RoundedCornerShape(AppDimens.controlRadius),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) { Text("Volver al inicio de la lista", color = Ink) }
                        }
                        if (hasMore) {
                            Button(
                                onClick = { viewModel.cargarSiguientesDenuncias(5) },
                                enabled = !isLoadingMore,
                                modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.buttonMinHeight),
                                shape = RoundedCornerShape(AppDimens.controlRadius)
                            ) {
                                if (isLoadingMore) {
                                    AppLoadingIndicator(
                                        Modifier.size(19.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(AppDimens.space4))
                                    Text("Cargando")
                                } else Text("Cargar cinco más")
                            }
                        } else {
                            Text("Has llegado al final de los registros disponibles.", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        ScreenSnackbar(
            snackbarHostState,
            Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
