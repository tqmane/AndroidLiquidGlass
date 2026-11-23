package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.utils.DampedDragAnimation
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SegmentSizing {
    Equal,
    ByContent,
}

data class SegmentItem(
    val title: String? = null,
    val image: Painter? = null,
    val enabled: Boolean = true,
    val contentDescription: String? = null,
    internal val content: (@Composable RowScope.() -> Unit)? = null,
)

object LiquidSegmentedControlDefaults {
    val TrackHeight = 32.dp
    val ThumbInset = 2.dp
    val ThumbHeight = 28.dp
    val TouchTargetHeight = 44.dp
    val LabelFontSize = 13.sp
    val IconSize = 17.dp
    val ContentSpacing = 4.dp
    val HorizontalContentPadding = 12.dp

    val DarkTrackColor = Color(0xFF232326)
    val DarkThumbColor = Color(0xFF606065)
    val DarkLabelColor = Color(0xFFF6F6F6)
    val DarkDisabledLabelColor = Color(0xFFF6F6F6)
    val LightTrackColor = Color(0xFFEEEEEF)
    val LightThumbColor = Color.White
    val LightLabelColor = Color.Black
    val LightDisabledLabelColor = Color.Black
}

internal fun apportionSegmentWidths(
    totalWidth: Float,
    contentWidths: List<Float>,
    sizing: SegmentSizing,
): List<Float> {
    if (contentWidths.isEmpty() || !totalWidth.isFinite() || totalWidth <= 0f) {
        return List(contentWidths.size) { 0f }
    }

    val weights = when (sizing) {
        SegmentSizing.Equal -> List(contentWidths.size) { 1f }
        SegmentSizing.ByContent -> contentWidths.map { width ->
            width.takeIf { it.isFinite() && it > 0f } ?: 1f
        }
    }
    val totalWeight = weights.sum()
    return weights.map { totalWidth * it / totalWeight }
}

internal fun segmentCenterFractions(widths: List<Float>): List<Float> {
    val totalWidth = widths.sum()
    if (!totalWidth.isFinite() || totalWidth <= 0f) return List(widths.size) { 0f }

    var position = 0f
    return widths.map { width ->
        val center = (position + width / 2f) / totalWidth
        position += width
        center
    }
}

internal fun nearestEnabledSegmentIndex(
    targetFraction: Float,
    centerFractions: List<Float>,
    enabled: List<Boolean>,
): Int? = centerFractions.indices
    .filter { enabled.getOrNull(it) == true }
    .minWithOrNull(
        compareBy<Int> { abs(centerFractions[it] - targetFraction.coerceIn(0f, 1f)) }
            .thenBy { it },
    )

internal fun segmentSelectionAfterActivation(
    activatedIndex: Int,
    momentary: Boolean,
): Int? = if (momentary) null else activatedIndex

@Composable
fun LiquidSegmentedControl(
    segments: List<SegmentItem>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    momentary: Boolean = false,
    sizing: SegmentSizing = SegmentSizing.Equal,
    backdrop: Backdrop? = null,
) {
    if (segments.isEmpty()) return

    val glassAvailable = backdrop != null
    val isLightTheme = !isSystemInDarkTheme()
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = LiquidSegmentedControlDefaults.LabelFontSize,
        fontWeight = FontWeight.Medium,
    )
    val contentWidths = remember(segments, textMeasurer, density.fontScale) {
        segments.map { segment ->
            val labelWidth = segment.title?.let { title ->
                textMeasurer.measure(AnnotatedString(title), style = textStyle).size.width.toFloat()
            } ?: 0f
            val imageWidth = if (segment.image != null) {
                with(density) { LiquidSegmentedControlDefaults.IconSize.toPx() }
            } else {
                0f
            }
            val spacing = if (labelWidth > 0f && imageWidth > 0f) {
                with(density) { LiquidSegmentedControlDefaults.ContentSpacing.toPx() }
            } else {
                0f
            }
            val padding = with(density) {
                LiquidSegmentedControlDefaults.HorizontalContentPadding.toPx() * 2f
            }
            (labelWidth + imageWidth + spacing + padding).coerceAtLeast(padding)
        }
    }
    val contentWidth = with(density) { contentWidths.sum().toDp() }
    val enabledSegments = remember(segments) { segments.map { it.enabled } }
    val selected = selectedIndex
        ?.takeIf { it in segments.indices && enabledSegments[it] }
    val firstEnabledIndex = enabledSegments.indexOfFirst { it }
    val initialIndex = selected ?: firstEnabledIndex.takeIf { it >= 0 } ?: 0
    var pressedIndex by remember { mutableIntStateOf(-1) }
    var isDragging by remember { mutableStateOf(false) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val touchSlop = androidx.compose.ui.platform.LocalViewConfiguration.current.touchSlop
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentMomentary by rememberUpdatedState(momentary)
    val currentIsLtr by rememberUpdatedState(isLtr)
    val currentEnabled by rememberUpdatedState(enabledSegments)
    val currentCenters = remember { mutableStateOf(emptyList<Float>()) }
    val currentWidth = remember { mutableFloatStateOf(0f) }
    val initialFraction = remember(initialIndex, contentWidths, sizing) {
        val widths = apportionSegmentWidths(1f, contentWidths, sizing)
        segmentCenterFractions(widths).getOrElse(initialIndex) { 0f }
    }
    val dragAnimation = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = initialFraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.55f,
            onDragStarted = {
                isDragging = true
                dragDistance = 0f
            },
            onDragStopped = {
                val wasDragged = dragDistance > touchSlop
                isDragging = false
                if (wasDragged) {
                    val targetIndex = nearestEnabledSegmentIndex(
                        targetFraction = targetValue,
                        centerFractions = currentCenters.value,
                        enabled = currentEnabled,
                    )
                    targetIndex?.let { index ->
                        updateValue(currentCenters.value[index])
                        currentOnSelect(index)
                    }
                }
            },
            onDrag = { _, dragAmount ->
                dragDistance += abs(dragAmount.x)
                val direction = if (currentIsLtr) 1f else -1f
                val width = currentWidth.floatValue
                if (width > 0f) {
                    updateValue(
                        (targetValue + dragAmount.x * direction / width).coerceIn(0f, 1f),
                    )
                }
            },
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .then(if (sizing == SegmentSizing.ByContent) Modifier.width(contentWidth) else Modifier)
            .heightIn(min = LiquidSegmentedControlDefaults.TouchTargetHeight)
    ) {
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val trackWidthPx = availableWidthPx.takeIf { it.isFinite() && it > 0f }
            ?: contentWidths.sum().coerceAtLeast(1f)
        val segmentWidths = apportionSegmentWidths(trackWidthPx, contentWidths, sizing)
        val centerFractions = segmentCenterFractions(segmentWidths)
        val widthWeights = apportionSegmentWidths(1f, contentWidths, sizing)
        val currentCenterIndex = nearestEnabledSegmentIndex(
            targetFraction = dragAnimation.value,
            centerFractions = centerFractions,
            enabled = enabledSegments,
        )
        val isActivelyDragged = isDragging && dragDistance > touchSlop
        val visualProgress = when {
            momentary && isActivelyDragged -> dragAnimation.value
            momentary && pressedIndex in segments.indices -> {
                centerFractions[pressedIndex]
            }
            else -> dragAnimation.value
        }.coerceIn(0f, 1f)
        val visualIndex = if (momentary) {
            if (isActivelyDragged) {
                currentCenterIndex
            } else {
                pressedIndex.takeIf { it in segments.indices } ?: currentCenterIndex
            }
        } else {
            selected ?: currentCenterIndex
        }
        val thumbVisible = if (momentary) {
            isDragging || pressedIndex in segments.indices
        } else {
            selected != null
        }
        val thumbSegmentWidth = segmentWidths.getOrElse(visualIndex ?: initialIndex) { trackWidthPx }
        val thumbWidth = with(density) {
            (thumbSegmentWidth.toDp() - LiquidSegmentedControlDefaults.ThumbInset * 2f)
                .coerceAtLeast(8.dp)
        }
        val thumbOffsetPx = if (isLtr) {
            visualProgress * trackWidthPx - with(density) { thumbWidth.toPx() } / 2f
        } else {
            with(density) { thumbWidth.toPx() } / 2f - visualProgress * trackWidthPx
        }
        val roundedThumbOffsetPx = thumbOffsetPx.roundToInt()
        LaunchedEffect(selected, centerFractions, momentary) {
            if (!momentary && selected != null) {
                val target = centerFractions[selected]
                if (abs(dragAnimation.targetValue - target) > 0.001f) {
                    dragAnimation.animateToValue(target)
                }
            }
        }

        SideEffect {
            currentCenters.value = centerFractions
            currentWidth.floatValue = trackWidthPx
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(dragAnimation.modifier),
        ) {
            val controlBackdrop = rememberLayerBackdrop()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(controlBackdrop),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(LiquidSegmentedControlDefaults.TrackHeight)
                        .clip(CircleShape)
                        .background(
                            if (isLightTheme) {
                                LiquidSegmentedControlDefaults.LightTrackColor
                            } else {
                                LiquidSegmentedControlDefaults.DarkTrackColor
                            },
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LiquidSegmentedControlDefaults.TouchTargetHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    segments.forEachIndexed { index, segment ->
                        val interactionSource = remember(index) { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        LaunchedEffect(pressed, index, momentary) {
                            if (momentary) {
                                if (pressed) {
                                    pressedIndex = index
                                } else if (pressedIndex == index) {
                                    pressedIndex = -1
                                }
                            }
                        }
                        val isSelected = !momentary && selected == index
                        val labelColor = when {
                            !segment.enabled && isLightTheme ->
                                LiquidSegmentedControlDefaults.LightDisabledLabelColor
                            !segment.enabled ->
                                LiquidSegmentedControlDefaults.DarkDisabledLabelColor
                            isLightTheme ->
                                LiquidSegmentedControlDefaults.LightLabelColor
                            else ->
                                LiquidSegmentedControlDefaults.DarkLabelColor
                        }
                        Box(
                            modifier = Modifier
                                .weight(widthWeights[index].coerceAtLeast(0.0001f))
                                .fillMaxHeight()
                                .selectable(
                                    selected = isSelected,
                                    enabled = segment.enabled,
                                    interactionSource = interactionSource,
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = {
                                        val nextSelection = segmentSelectionAfterActivation(
                                            activatedIndex = index,
                                            momentary = momentary,
                                        )
                                        if (nextSelection != null) {
                                            dragAnimation.animateToValue(centerFractions[index])
                                        }
                                        currentOnSelect(index)
                                    },
                                )
                                .then(
                                    (segment.title ?: segment.contentDescription)?.let { label ->
                                        Modifier.semantics {
                                            contentDescription = label
                                        }
                                    } ?: Modifier,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            SegmentLabelContent(segment, labelColor, isSelected)
                        }
                    }
                }
            }

            if (thumbVisible && visualIndex != null) {
                val thumbColor = if (isLightTheme) {
                    LiquidSegmentedControlDefaults.LightThumbColor
                } else {
                    LiquidSegmentedControlDefaults.DarkThumbColor
                }
                val thumbBackdrop = rememberCombinedBackdrop(
                    backdrop ?: controlBackdrop,
                    rememberBackdrop(controlBackdrop) { drawBackdrop -> drawBackdrop() },
                )
                val glassThumbModifier = if (glassAvailable && backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = thumbBackdrop,
                        shape = { Capsule() },
                        effects = {
                            val progress = dragAnimation.pressProgress
                            blur(8.dp.toPx() * (1f - progress))
                            lens(
                                5.dp.toPx() * progress,
                                10.dp.toPx() * progress,
                                chromaticAberration = true,
                            )
                        },
                        highlight = {
                            val progress = dragAnimation.pressProgress
                            Highlight.Ambient.copy(
                                width = 1.dp,
                                blurRadius = 1.dp,
                                alpha = progress * if (isLightTheme) 0.18f else 0.45f,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 8.dp,
                                color = Color.Black.copy(alpha = 0.12f),
                            )
                        },
                        innerShadow = {
                            val progress = dragAnimation.pressProgress
                            InnerShadow(
                                radius = 4.dp * progress,
                                alpha = progress,
                            )
                        },
                        layerBlock = {
                            val progress = dragAnimation.pressProgress
                            scaleX = dragAnimation.scaleX * lerp(1f, 1.2f / 1.55f, progress)
                            scaleY = dragAnimation.scaleY
                            val velocity = dragAnimation.velocity / 50f
                            scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            drawRect(
                                thumbColor,
                                alpha = 1f - dragAnimation.pressProgress,
                            )
                        },
                    ).then(
                        if (isLightTheme) {
                            Modifier.border(
                                0.5.dp,
                                Color.Black.copy(alpha = 0.04f),
                                Capsule(),
                            )
                        } else {
                            Modifier
                        },
                    )
                } else {
                    Modifier
                        .graphicsLayer {
                            val progress = dragAnimation.pressProgress
                            scaleX = dragAnimation.scaleX * lerp(1f, 1.2f / 1.55f, progress)
                            scaleY = dragAnimation.scaleY
                            val velocity = dragAnimation.velocity / 50f
                            scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.2f, 0.2f)
                        }
                        .shadow(
                            elevation = if (isLightTheme) 3.dp else 0.dp,
                            shape = Capsule(),
                            ambientColor = Color.Black.copy(alpha = 0.12f),
                            spotColor = Color.Black.copy(alpha = 0.12f),
                        )
                        .clip(CircleShape)
                        .background(thumbColor)
                        .then(
                            if (isLightTheme) {
                                Modifier.border(
                                    0.5.dp,
                                    Color.Black.copy(alpha = 0.04f),
                                    CircleShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .absoluteOffset { IntOffset(roundedThumbOffsetPx, 0) }
                        .width(thumbWidth)
                        .height(LiquidSegmentedControlDefaults.ThumbHeight)
                        .then(glassThumbModifier),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LiquidSegmentedControlDefaults.TouchTargetHeight)
                    .clearAndSetSemantics {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                segments.forEachIndexed { index, segment ->
                    val labelColor = when {
                        !segment.enabled && isLightTheme ->
                            LiquidSegmentedControlDefaults.LightDisabledLabelColor
                        !segment.enabled ->
                            LiquidSegmentedControlDefaults.DarkDisabledLabelColor
                        isLightTheme ->
                            LiquidSegmentedControlDefaults.LightLabelColor
                        else ->
                            LiquidSegmentedControlDefaults.DarkLabelColor
                    }
                    Box(
                        modifier = Modifier
                            .weight(widthWeights[index].coerceAtLeast(0.0001f))
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SegmentLabelContent(
                            segment = segment,
                            labelColor = labelColor,
                            selected = !momentary && selected == index,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentLabelContent(
    segment: SegmentItem,
    labelColor: Color,
    selected: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LiquidSegmentedControlDefaults.ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (segment.content != null) {
            segment.content.invoke(this)
        } else {
            segment.image?.let { image ->
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = Modifier.size(LiquidSegmentedControlDefaults.IconSize),
                    colorFilter = ColorFilter.tint(labelColor),
                )
            }
            segment.title?.let { title ->
                BasicText(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = LiquidSegmentedControlDefaults.LabelFontSize,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}
