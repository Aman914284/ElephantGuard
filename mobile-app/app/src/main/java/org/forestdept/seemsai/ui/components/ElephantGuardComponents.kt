package org.forestdept.seemsai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.ui.theme.AlertRed
import org.forestdept.seemsai.ui.theme.AlertRedBg
import org.forestdept.seemsai.ui.theme.AlertRedBorder
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark850
import org.forestdept.seemsai.ui.theme.ForestDark900
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenDeep
import org.forestdept.seemsai.ui.theme.ForestGreenHover
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.ForestGreenPrimary
import org.forestdept.seemsai.ui.theme.ForestInputBg
import org.forestdept.seemsai.ui.theme.ForestInputBorder
import org.forestdept.seemsai.ui.theme.ForestInputFocusBorder
import org.forestdept.seemsai.ui.theme.ForestSecondaryBtn
import org.forestdept.seemsai.ui.theme.ForestSecondaryBorder
import org.forestdept.seemsai.ui.theme.SuccessGreen
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextSubtle
import org.forestdept.seemsai.ui.theme.TextWhite

/**
 * Custom vector-drawn Shield + Elephant emblem for Elephant Guard.
 */
@Composable
fun ElephantGuardLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 80
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val w = size.width
            val h = size.height

            // 1. Shield Background Path
            val shieldPath = Path().apply {
                moveTo(w * 0.5f, h * 0.04f)
                // Top-right curve
                cubicTo(w * 0.82f, h * 0.04f, w * 0.94f, h * 0.18f, w * 0.94f, h * 0.40f)
                // Bottom point curve
                cubicTo(w * 0.94f, h * 0.72f, w * 0.65f, h * 0.90f, w * 0.5f, h * 0.98f)
                // Bottom-left curve
                cubicTo(w * 0.35f, h * 0.90f, w * 0.06f, h * 0.72f, w * 0.06f, h * 0.40f)
                // Top-left curve
                cubicTo(w * 0.06f, h * 0.18f, w * 0.18f, h * 0.04f, w * 0.5f, h * 0.04f)
                close()
            }

            // Fill Shield with dark forest gradient
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E4630), Color(0xFF0F261A))
                ),
                style = Fill
            )

            // Shield border
            drawPath(
                path = shieldPath,
                color = ForestGreenLight,
                style = Stroke(width = w * 0.035f)
            )

            // Inner subtle shield border
            val innerShieldPath = Path().apply {
                moveTo(w * 0.5f, h * 0.10f)
                cubicTo(w * 0.76f, h * 0.10f, w * 0.86f, h * 0.22f, w * 0.86f, h * 0.40f)
                cubicTo(w * 0.86f, h * 0.66f, w * 0.62f, h * 0.82f, w * 0.5f, h * 0.90f)
                cubicTo(w * 0.38f, h * 0.82f, w * 0.14f, h * 0.66f, w * 0.14f, h * 0.40f)
                cubicTo(w * 0.14f, h * 0.22f, w * 0.24f, h * 0.10f, w * 0.5f, h * 0.10f)
                close()
            }
            drawPath(
                path = innerShieldPath,
                color = ForestGreenPrimary.copy(alpha = 0.4f),
                style = Stroke(width = w * 0.015f)
            )

            // 2. Elephant Silhouette & Head/Trunk Vector
            val elephantPath = Path().apply {
                // Head dome
                moveTo(w * 0.34f, h * 0.44f)
                cubicTo(w * 0.34f, h * 0.30f, w * 0.66f, h * 0.30f, w * 0.66f, h * 0.44f)
                // Right ear curve
                cubicTo(w * 0.74f, h * 0.45f, w * 0.76f, h * 0.58f, w * 0.65f, h * 0.62f)
                // Down to trunk base
                lineTo(w * 0.56f, h * 0.54f)
                // Trunk down-curving
                cubicTo(w * 0.58f, h * 0.68f, w * 0.64f, h * 0.72f, w * 0.60f, h * 0.76f)
                cubicTo(w * 0.54f, h * 0.78f, w * 0.48f, h * 0.68f, w * 0.48f, h * 0.58f)
                // Left tusk area & ear
                lineTo(w * 0.42f, h * 0.54f)
                cubicTo(w * 0.30f, h * 0.58f, w * 0.26f, h * 0.45f, w * 0.34f, h * 0.44f)
                close()
            }

            drawPath(
                path = elephantPath,
                color = TextWhite,
                style = Fill
            )

            // Tusks
            val leftTusk = Path().apply {
                moveTo(w * 0.43f, h * 0.56f)
                quadraticTo(w * 0.38f, h * 0.64f, w * 0.41f, h * 0.68f)
                quadraticTo(w * 0.43f, h * 0.62f, w * 0.45f, h * 0.57f)
                close()
            }
            drawPath(leftTusk, color = ForestGreenLight, style = Fill)

            val rightTusk = Path().apply {
                moveTo(w * 0.55f, h * 0.56f)
                quadraticTo(w * 0.60f, h * 0.64f, w * 0.57f, h * 0.68f)
                quadraticTo(w * 0.55f, h * 0.62f, w * 0.53f, h * 0.57f)
                close()
            }
            drawPath(rightTusk, color = ForestGreenLight, style = Fill)

            // Eye accent dots
            drawCircle(color = ForestDark950, radius = w * 0.016f, center = Offset(w * 0.44f, h * 0.44f))
            drawCircle(color = ForestDark950, radius = w * 0.016f, center = Offset(w * 0.56f, h * 0.44f))
        }
    }
}

/**
 * Header with Elephant Guard title and mission statement.
 */
@Composable
fun ElephantGuardHeader(
    modifier: Modifier = Modifier,
    title: String = "ELEPHANT GUARD",
    subtitle: String = "Protect Wildlife. Protect Lives."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElephantGuardLogo(sizeDp = 84)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = TextMutedSage,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Subtle Powered by SEEMS-AI footer/badge.
 */
@Composable
fun PoweredBySeemsAiBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ForestDark850)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ForestGreenLight)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "POWERED BY SEEMS-AI",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = TextMutedSage
        )
    }
}

/**
 * Clean dark forest text field with custom leading and trailing icons.
 */
@Composable
fun ForestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) AlertRed else TextMutedSage,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(ForestInputBg, RoundedCornerShape(12.dp)),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            },
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError) AlertRed else ForestGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = if (isPassword && onPasswordToggle != null) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = TextMutedSage,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = singleLine,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = ForestInputBg,
                unfocusedContainerColor = ForestInputBg,
                focusedBorderColor = ForestInputFocusBorder,
                unfocusedBorderColor = ForestInputBorder,
                errorBorderColor = AlertRed,
                errorContainerColor = ForestInputBg,
                cursorColor = ForestGreenLight
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    fontSize = 11.sp,
                    color = AlertRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Primary action button in vibrant forest green.
 */
@Composable
fun ForestPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreenPrimary,
            contentColor = TextWhite,
            disabledContainerColor = ForestGreenDeep.copy(alpha = 0.5f),
            disabledContentColor = TextSubtle
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextWhite,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = TextWhite
                )
            }
        }
    }
}

/**
 * Secondary action button for Demo access and alternative flows.
 */
@Composable
fun ForestSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ForestSecondaryBtn,
            contentColor = TextWhite,
            disabledContainerColor = ForestDark950,
            disabledContentColor = TextSubtle
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestSecondaryBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ForestGreenLight
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
        }
    }
}

/**
 * Clickable text button for lightweight actions (Forgot Password, Back to Login).
 */
@Composable
fun ForestTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TextMutedSage,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

/**
 * Error alert message container.
 */
@Composable
fun ForestErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AlertRedBg)
            .border(1.dp, AlertRedBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = AlertRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = AlertRed,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Success notification message container.
 */
@Composable
fun ForestSuccessCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x2B22C55E))
            .border(1.dp, Color(0x6622C55E), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = SuccessGreen,
            fontWeight = FontWeight.Medium
        )
    }
}
