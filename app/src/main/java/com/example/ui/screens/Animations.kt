package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// 1. SCREEN ENTER (all screens): fades in + slides up 15dp in 180ms
@Composable
fun Modifier.animateScreenEntrance(): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val density = LocalDensity.current
    val shiftPx = remember { with(density) { 15.dp.toPx() } }
    
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else shiftPx,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "screen_y"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "screen_alpha"
    )
    return this.graphicsLayer {
        translationY = translateY
        this.alpha = alpha
    }
}

// 2. CARDS STAGGER: appears 25ms after previous (capped at 5 items). Fade + slight scale from 0.95 to 1.0 in 180ms
@Composable
fun Modifier.animateCardStagger(index: Int): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val cappedIndex = index.coerceAtMost(5)
        kotlinx.coroutines.delay(cappedIndex.toLong() * 25)
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "card_alpha_$index"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "card_scale_$index"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        this.scaleX = scale
        this.scaleY = scale
    }
}

// 3. NUMBERS COUNT UP Component: ₹ amounts count from 0 to final, 350ms duration, ease out curve
@Composable
fun AnimatedNumberCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    prefix: String = "₹",
    suffix: String = "",
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    var hasStarted by remember { mutableStateOf(false) }
    LaunchedEffect(targetValue) {
        hasStarted = true
    }
    val animateValue by animateFloatAsState(
        targetValue = if (hasStarted) targetValue.toFloat() else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "number_counter"
    )
    Text(
        text = "$prefix${animateValue.toInt()}$suffix",
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

// 4. BOTTOM NAV BOUNCE: Selected tab icon bounces 1.0 -> 1.2 -> 1.0
@Composable
fun Modifier.animateBottomNavBounce(isSelected: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_bounce"
    )
    return this.graphicsLayer {
        scaleX = if (isSelected) scale else 1.0f
        scaleY = if (isSelected) scale else 1.0f
    }
}

// 7. MOOD SELECTION: Selected card scales up slightly with a spring animation
@Composable
fun Modifier.animateMoodSelection(isSelected: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "mood_selection_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// 8. BUTTON PRESS: Scale down 0.95 on press, spring back on release
@Composable
fun Modifier.animateButtonPress(): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_press_scale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
}
