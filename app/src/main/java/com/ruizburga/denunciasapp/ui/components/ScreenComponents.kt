package com.ruizburga.denunciasapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import com.ruizburga.denunciasapp.ui.theme.AppDimens
import com.ruizburga.denunciasapp.ui.theme.*

@Composable
fun ScreenBody(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppDimens.pageHorizontal, vertical = AppDimens.pageVertical),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        content = content
    )
}

@Composable
fun <T> StateTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    val duration = motionDurationMillis(210)
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(duration)) + slideInVertically(tween(duration)) { it / 36 }) togetherWith
                fadeOut(tween(motionDurationMillis(160)))
        },
        contentAlignment = Alignment.TopStart,
        label = "screen-state",
        content = content
    )
}

@Composable
fun AnimatedEntry(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        // Fade only: measure each card at its natural height before it appears.
        enter = fadeIn(tween(motionDurationMillis(190))),
        exit = fadeOut(tween(motionDurationMillis(140)))
    ) { content() }
}

@Composable
fun AppLoadingIndicator(modifier: Modifier = Modifier.size(22.dp), strokeWidth: Dp = 2.dp, color: Color = Teal) {
    val accessibleModifier = modifier.semantics { contentDescription = "Cargando" }
    if (motionDurationMillis(1) > 0) {
        CircularProgressIndicator(modifier = accessibleModifier, strokeWidth = strokeWidth, color = color)
    } else {
        Canvas(accessibleModifier) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 245f,
                useCenter = false,
                topLeft = Offset(stroke.width, stroke.width),
                size = androidx.compose.ui.geometry.Size(size.width - stroke.width * 2, size.height - stroke.width * 2),
                style = stroke
            )
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLength: Int? = null,
    supportingText: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue -> onValueChange(if (maxLength == null) newValue else newValue.take(maxLength)) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.space2)) {
                    Text(label)
                    Text("*", color = Teal)
                }
            },
            placeholder = { Text(placeholder, color = Muted.copy(alpha = .75f)) },
            shape = RoundedCornerShape(AppDimens.controlRadius),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = {
                if (supportingText != null) Text(supportingText, color = Muted)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Border,
                focusedLabelColor = Teal,
                cursorColor = Teal,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedContainerColor = SurfaceInset,
                unfocusedContainerColor = SurfaceInset,
                focusedPlaceholderColor = Muted,
                unfocusedPlaceholderColor = Muted
            )
        )
    }
}

@Composable
fun InlineNotice(message: String, isError: Boolean = false) {
    val bg = if (isError) DangerContainer else SurfaceRaised
    val fg = if (isError) Danger else Muted
    Surface(color = bg, shape = RoundedCornerShape(15.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isError) AppIcon(AppIconName.Document, Modifier.size(18.dp), fg)
            Text(message, color = if (isError) Ink else fg, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ScreenSnackbar(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = SurfaceRaised,
            contentColor = Ink,
            actionColor = Teal,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
