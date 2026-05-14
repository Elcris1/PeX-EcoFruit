package com.example.ecofruit.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ecofruit.R

@Composable
fun CustomTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String,
    placeholder   : String,
    icon          : ImageVector,
    keyboardType  : KeyboardType = KeyboardType.Text,
    errorMessage  : String?      = null
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text          = label,
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Medium,
            color         = colors.onSurfaceVariant,
            letterSpacing = 0.4.sp
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    text  = placeholder,
                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint     = if (value.isNotEmpty()) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine      = true,
            isError         = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor        = colors.primary,
                unfocusedBorderColor      = colors.outline,
                errorBorderColor          = colors.error,
                focusedContainerColor     = colors.surfaceVariant,
                unfocusedContainerColor   = colors.surfaceVariant,
                errorContainerColor       = colors.errorContainer,
                cursorColor               = colors.primary,
                focusedTextColor          = colors.onSurface,
                unfocusedTextColor        = colors.onSurface,
                errorTextColor            = colors.onSurface,
                focusedLeadingIconColor   = colors.primary,
                unfocusedLeadingIconColor = colors.onSurfaceVariant,
                errorLeadingIconColor     = colors.error
            ),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn() + slideInVertically()) {
            errorMessage?.let {
                Text(text = it, color = colors.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PasswordTextField(
    label              : String,
    placeholder_text   : String,
    value              : String,
    onValueChange      : (String) -> Unit,
    passwordVisible    : Boolean,
    onToggleVisibility : () -> Unit,
    errorMessage       : String? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text          =  label,
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Medium,
            color         = colors.onSurfaceVariant,
            letterSpacing = 0.4.sp
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    text  = placeholder_text,
                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector        = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint     = if (value.isNotEmpty()) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector        = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_icon_description) else stringResource(R.string.show_icon_description),
                        tint     = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            singleLine           = true,
            isError              = errorMessage != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor        = colors.primary,
                unfocusedBorderColor      = colors.outline,
                errorBorderColor          = colors.error,
                focusedContainerColor     = colors.surfaceVariant,
                unfocusedContainerColor   = colors.surfaceVariant,
                errorContainerColor       = colors.errorContainer,
                cursorColor               = colors.primary,
                focusedTextColor          = colors.onSurface,
                unfocusedTextColor        = colors.onSurface,
                errorTextColor            = colors.onSurface,
                focusedLeadingIconColor   = colors.primary,
                unfocusedLeadingIconColor = colors.onSurfaceVariant,
                errorLeadingIconColor     = colors.error
            ),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn() + slideInVertically()) {
            errorMessage?.let {
                Text(text = it, color = colors.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


@Composable
fun AnimatedHeader(
    enterAnim: Animatable<Float, AnimationVector1D>,
    description: String
) {
    AnimatedVisibility(
        visible = enterAnim.value > 0.3f,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.app_name),
                style = typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = colorScheme.primary
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography  = MaterialTheme.typography
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btnScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary)
                )
            )
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text,
                style = typography.titleMedium.copy(
                    color = colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun AnimatedBubbleBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    // Pulso del blob decorativo
    val infiniteTransition = rememberInfiniteTransition(label = "blob")
    val blobScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "blobScale"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Blob superior (primaryContainer)
        Box(
            modifier = Modifier
                .size((300 * blobScale).dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors.primaryContainer.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )
        // Blob inferior (tertiaryContainer)
        Box(
            modifier = Modifier
                .size((220 * blobScale).dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 40.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors.tertiaryContainer.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )
        content()
    }
}


@Composable
fun AnimatedCard(
    enterAnim: Animatable<Float, AnimationVector1D>,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = enterAnim.value > 0.5f,
        enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 2 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.4f))
        ) {
            content()
        }
    }
}

@Composable
fun AnimatedRedirectionText(
    enterAnim: Animatable<Float, AnimationVector1D>,
    questionText: String,
    actionText: String,
    context: Context,
    dst: Class<*>

) {
    AnimatedVisibility(
        visible = enterAnim.value > 0.7f,
        enter = fadeIn(tween(600))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(28.dp))
            Row {
                Text(
                    questionText,
                    style = typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant)
                )
                Text(
                    actionText,
                    style = typography.bodyMedium.copy(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable {
                        val intent = Intent(context, dst)
                        context.startActivity(intent)

                        (context as? Activity)?.finish()
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}