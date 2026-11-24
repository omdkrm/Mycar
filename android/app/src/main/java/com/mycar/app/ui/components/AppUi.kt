package com.mycar.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Single source of truth for corner radii used across the whole app,
 * so that every card, dialog and button looks identical on every screen.
 */
object AppShapes {
    val Card = RoundedCornerShape(14.dp)
    val Dialog = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(12.dp)
}

/** Outer spacing shared by every dialog (including the date picker). */
object AppDialogDefaults {
    val HorizontalMargin: Dp = 20.dp
    val VerticalMargin: Dp = 24.dp
    val MaxWidth: Dp = 480.dp
    val ContentPadding: Dp = 20.dp
    val Elevation: Dp = 6.dp
    val ShadowElevation: Dp = 8.dp

    fun frame(): Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = HorizontalMargin, vertical = VerticalMargin)
        .widthIn(max = MaxWidth)
}

/**
 * The one and only dialog used by the app: same size, shape, title style,
 * scrolling body and button row everywhere.
 *
 * @param confirmText when null, only the dismiss button is shown (e.g. pickers).
 */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    dismissText: String = "انصراف",
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = AppDialogDefaults.frame(),
                shape = AppShapes.Dialog,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = AppDialogDefaults.Elevation,
                shadowElevation = AppDialogDefaults.ShadowElevation
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDialogDefaults.ContentPadding)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        content = content
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = AppShapes.Button
                        ) {
                            Text(
                                text = dismissText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (confirmText != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onConfirm,
                                shape = AppShapes.Button,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = confirmColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = confirmText,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
