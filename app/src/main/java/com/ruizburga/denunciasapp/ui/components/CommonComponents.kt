package com.ruizburga.denunciasapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.theme.AppDimens

@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(bottom = AppDimens.space12)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(7.dp).background(Teal, CircleShape))
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = Teal, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Ink)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun StatePanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: AppIconName = AppIconName.Document,
    iconDescription: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.animateContentSize(tween(motionDurationMillis(200))),
        shape = RoundedCornerShape(AppDimens.cardRadius),
        color = SurfaceBase,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppDimens.space12, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(54.dp).background(SurfaceRaised, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(icon, Modifier.size(25.dp), Teal, iconDescription)
            }
            Spacer(Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(7.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Muted)
            if (action != null) {
                Spacer(Modifier.height(18.dp))
                action()
            }
        }
    }
}

@Composable
fun FieldLabel(text: String, required: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Ink)
        if (required) Text("*", color = Teal, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StatusPill(text: String) {
    val attended = text == "0" || text.equals("ATENDIDA", ignoreCase = true) || text.equals("ATENDIDO", ignoreCase = true)
    val targetContainer = if (attended) SuccessContainer else AmberLight
    val targetContent = if (attended) Success else Amber
    val container by animateColorAsState(targetContainer, tween(motionDurationMillis(200)), label = "status-container")
    val content by animateColorAsState(targetContent, tween(motionDurationMillis(200)), label = "status-content")
    Surface(color = container, shape = CircleShape) {
        Row(
            Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(6.dp).background(content, CircleShape))
            Text(
                if (attended) "Atendida" else "Pendiente",
                color = content,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DenunciaCard(
    denuncia: Denuncia,
    modifier: Modifier = Modifier,
    onMarkAttended: (() -> Unit)? = null,
    onOpenDetail: (() -> Unit)? = null,
    isUpdating: Boolean = false,
    actionsEnabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceBase,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(AppDimens.space8), verticalArrangement = Arrangement.spacedBy(AppDimens.space6)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("CASO #${denuncia.id}", color = Muted, style = MaterialTheme.typography.labelSmall, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(denuncia.tipo, color = Ink, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                StatusPill(denuncia.estado)
            }

            HorizontalDivider(color = Border)
            DetailValue("Fecha", denuncia.fecha, AppIconName.Calendar)
            DetailValue("Dirección", denuncia.direccion, AppIconName.Location)
            DetailValue("DNI", denuncia.dni, AppIconName.Person)
            Text(denuncia.descripcion, color = Muted, style = MaterialTheme.typography.bodyMedium)

            if (onMarkAttended != null && denuncia.estado == "1") {
                Button(
                    onClick = onMarkAttended,
                    enabled = actionsEnabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(AppDimens.controlRadius)
                ) {
                    AnimatedContent(
                        targetState = isUpdating,
                        transitionSpec = { fadeIn(tween(motionDurationMillis(150))) togetherWith fadeOut(tween(motionDurationMillis(120))) },
                        label = "attendance-button-content"
                    ) { updating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (updating) {
                                AppLoadingIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(9.dp))
                                Text("Actualizando")
                            } else {
                                AppIcon(AppIconName.Check, Modifier.size(18.dp), MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(9.dp))
                                Text("Marcar como atendida")
                            }
                        }
                    }
                }
            }
            if (onOpenDetail != null) {
                OutlinedButton(
                    onClick = onOpenDetail,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(AppDimens.controlRadius),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text("Ver detalle", color = Ink)
                    Spacer(Modifier.width(8.dp))
                    AppIcon(AppIconName.ArrowRight, Modifier.size(18.dp), Teal)
                }
            }
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String, icon: AppIconName) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AppIcon(icon, Modifier.padding(top = 2.dp).size(16.dp), Muted)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
