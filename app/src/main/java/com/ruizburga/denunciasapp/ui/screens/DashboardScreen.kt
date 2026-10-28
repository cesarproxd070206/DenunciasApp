package com.ruizburga.denunciasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.ui.components.*
import com.ruizburga.denunciasapp.ui.state.ContentState
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel

@Composable
fun DashboardScreen(
    viewModel: DenunciasViewModel,
    onOpenList: () -> Unit,
    onRegister: () -> Unit,
    onSearch: () -> Unit
) {
    val listState by viewModel.listaState.collectAsState()
    val requestCompleted by viewModel.listaRequestCompleted.collectAsState()

    LaunchedEffect(viewModel) {
        if (!viewModel.listaRequestCompleted.value) viewModel.cargarDenuncias(offset = 0, limit = 5)
    }

    val mode = when (listState) {
        ContentState.Loading -> "loading"
        ContentState.Empty -> if (requestCompleted) "empty" else "loading"
        is ContentState.Error -> "error"
        is ContentState.Data -> "data"
    }
    val records = (listState as? ContentState.Data<List<Denuncia>>)?.value.orEmpty()
    val pendingCount = records.count { it.estado == "1" }
    val attendedCount = records.count { it.estado == "0" }
    val knownStatusCount = pendingCount + attendedCount

    ScreenBody {
        ScreenHeader(
            "CENTRO DE ATENCIÓN",
            "Panel ciudadano",
            "Resumen de las denuncias consultadas en esta sesión."
        )

        UpdateStatus(mode, records.size)
        Spacer(Modifier.height(AppDimens.space8))

        when (mode) {
            "loading" -> StatePanel(
                "Actualizando el resumen",
                "Consultando la primera página de denuncias del servicio.",
                Modifier.fillMaxWidth(),
                action = { AppLoadingIndicator() }
            )
            "empty" -> StatePanel(
                "Aún no hay registros",
                "El servicio no devolvió denuncias para mostrar. Puedes registrar una nueva.",
                Modifier.fillMaxWidth(),
                icon = AppIconName.Document,
                iconDescription = "Resumen sin registros",
                action = {
                    Button(onClick = onRegister, shape = RoundedCornerShape(AppDimens.controlRadius)) {
                        Text("Registrar denuncia")
                    }
                }
            )
            "error" -> {
                val message = (listState as? ContentState.Error)?.message.orEmpty()
                StatePanel(
                    "No se pudo cargar el resumen",
                    message,
                    Modifier.fillMaxWidth(),
                    icon = AppIconName.Document,
                    iconDescription = "Error al actualizar el resumen",
                    action = {
                        OutlinedButton(
                            onClick = { viewModel.cargarDenuncias(offset = 0, limit = 5) },
                            shape = RoundedCornerShape(AppDimens.controlRadius)
                        ) { Text("Reintentar") }
                    }
                )
            }
            else -> {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resumen cargado", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("${records.size} en sesión", style = MaterialTheme.typography.labelSmall, color = Muted)
                }
                Spacer(Modifier.height(AppDimens.space4))
                AnimatedEntry {
                    MetricCard(
                        label = "Denuncias consultadas",
                        value = records.size,
                        detail = "registros cargados en esta sesión",
                        icon = AppIconName.Document,
                        accent = Teal,
                        modifier = Modifier.fillMaxWidth(),
                        featured = true
                    )
                }
                Spacer(Modifier.height(AppDimens.space6))
                AnimatedEntry {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.space6)) {
                        MetricCard(
                            label = "Pendientes",
                            value = pendingCount,
                            detail = "estado 1",
                            icon = AppIconName.List,
                            accent = Amber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Atendidas",
                            value = attendedCount,
                            detail = "estado 0",
                            icon = AppIconName.Check,
                            accent = Success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(AppDimens.space6))
                SampleScopeNote(records.size)

                if (knownStatusCount > 0) {
                    Spacer(Modifier.height(AppDimens.space8))
                    AnimatedEntry {
                        StatusDistributionCard(
                            pending = pendingCount,
                            attended = attendedCount,
                            totalLoaded = records.size,
                            knownTotal = knownStatusCount
                        )
                    }
                } else if (records.isNotEmpty()) {
                    Spacer(Modifier.height(AppDimens.space8))
                    InlineNotice(
                        "No se puede calcular la distribución: los registros cargados no incluyen estados 0 o 1 reconocibles."
                    )
                }
            }
        }

        Spacer(Modifier.height(AppDimens.space12))
        Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge, color = Ink)
        Spacer(Modifier.height(AppDimens.space6))
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.space4)) {
            QuickAction("Denuncias", AppIconName.List, onOpenList, Modifier.weight(1f))
            QuickAction("Registrar", AppIconName.Add, onRegister, Modifier.weight(1f))
            QuickAction("Buscar DNI", AppIconName.Search, onSearch, Modifier.weight(1f))
        }
        Spacer(Modifier.height(AppDimens.space4))
    }
}

@Composable
private fun UpdateStatus(mode: String, loadedCount: Int) {
    val (label, tint, isLoading) = when (mode) {
        "loading" -> Triple("Actualizando datos del servicio", Teal, true)
        "data" -> Triple("$loadedCount registros disponibles en esta sesión", Success, false)
        "empty" -> Triple("Consulta completada · sin registros", Muted, false)
        else -> Triple("No se pudo actualizar el resumen", Danger, false)
    }
    Surface(
        color = SurfaceRaised,
        shape = RoundedCornerShape(AppDimens.controlRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppDimens.space6, vertical = AppDimens.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.space4)
        ) {
            if (isLoading) AppLoadingIndicator(Modifier.size(16.dp), strokeWidth = 1.8.dp, color = tint)
            else Box(Modifier.size(7.dp).background(tint, CircleShape))
            Text(label, color = if (mode == "error") Danger else Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: Int,
    detail: String,
    icon: AppIconName,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    featured: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = SurfaceBase,
        shape = RoundedCornerShape(AppDimens.cardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(if (featured) AppDimens.space6 else AppDimens.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.space4)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppDimens.space2)) {
                Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                if (!featured) {
                    Text(value.toString(), color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(detail, color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Box(
                Modifier.size(if (featured) 44.dp else 34.dp)
                    .background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (featured) {
                    Text(value.toString(), color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                } else {
                    AppIcon(icon, Modifier.size(AppDimens.iconSmall), accent)
                }
            }
        }
    }
}

@Composable
private fun SampleScopeNote(loadedCount: Int) {
    Surface(
        color = SurfaceRaised,
        shape = RoundedCornerShape(AppDimens.controlRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(AppDimens.space6),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.space4),
            verticalAlignment = Alignment.Top
        ) {
            AppIcon(AppIconName.Document, Modifier.size(AppDimens.iconSmall), Teal)
            Text(
                "Cifras calculadas sobre $loadedCount registros cargados. La API entrega páginas de cinco y no informa el total general.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusDistributionCard(pending: Int, attended: Int, totalLoaded: Int, knownTotal: Int) {
    Surface(
        color = SurfaceBase,
        shape = RoundedCornerShape(AppDimens.cardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(AppDimens.space6),
            verticalArrangement = Arrangement.spacedBy(AppDimens.space4)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Por estado", color = Ink, style = MaterialTheme.typography.titleMedium)
                Text("$knownTotal registros", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                if (knownTotal == totalLoaded) "Distribución de los registros cargados"
                else "$knownTotal de $totalLoaded con estado reconocible",
                color = Muted,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(SurfaceRaised)
            ) {
                if (pending > 0) {
                    Box(Modifier.weight(pending.toFloat()).fillMaxHeight().background(Amber))
                }
                if (attended > 0) {
                    Box(Modifier.weight(attended.toFloat()).fillMaxHeight().background(Success))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimens.space4)) {
                StatusLegend("Pendientes", pending, Amber, Modifier.weight(1f))
                StatusLegend("Atendidas", attended, Success, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusLegend(label: String, count: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppDimens.space2)) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Text("$label", color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        Text(count.toString(), color = Ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickAction(label: String, icon: AppIconName, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 80.dp),
        shape = RoundedCornerShape(AppDimens.controlRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceBase),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppDimens.space2, vertical = AppDimens.space4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.space4)
        ) {
            Box(
                Modifier.size(34.dp).background(TealLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { AppIcon(icon, Modifier.size(AppDimens.iconMedium), Teal) }
            Text(
                label,
                color = Ink,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
