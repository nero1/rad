package com.malawi.radio.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val MARQUEE_EDGE_PAUSE_MS = 900
private const val MARQUEE_SCROLL_SPEED_PX_PER_SECOND = 36f

/**
 * Shows static single-line text when it fits. When the text is wider than its
 * container, it scrolls to the end, jumps back to the start, and repeats.
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    staticAlignment: Alignment = Alignment.CenterStart
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember(text) { mutableIntStateOf(0) }
    val offsetX = remember(text) { Animatable(0f) }
    val maxOffset = (textWidth - containerWidth).coerceAtLeast(0)

    LaunchedEffect(text, maxOffset) {
        offsetX.snapTo(0f)
        if (maxOffset > 0) {
            while (true) {
                delay(MARQUEE_EDGE_PAUSE_MS.toLong())
                offsetX.animateTo(
                    targetValue = maxOffset.toFloat(),
                    animationSpec = tween(
                        durationMillis = ((maxOffset / MARQUEE_SCROLL_SPEED_PX_PER_SECOND) * 1000).roundToInt().coerceAtLeast(1200),
                        easing = LinearEasing
                    )
                )
                delay(MARQUEE_EDGE_PAUSE_MS.toLong())
                offsetX.snapTo(0f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { containerWidth = it.width },
        contentAlignment = if (maxOffset > 0) Alignment.CenterStart else staticAlignment
    ) {
        Text(
            text = text,
            modifier = Modifier
                .marqueeOffset(offsetX.value)
                .wrapContentWidth(unbounded = true)
                .onSizeChanged { textWidth = it.width },
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun Modifier.marqueeOffset(offsetX: Float): Modifier = offset {
    IntOffset(x = -offsetX.roundToInt(), y = 0)
}
