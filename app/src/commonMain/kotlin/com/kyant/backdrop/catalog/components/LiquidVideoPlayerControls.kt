package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay

/** Playback state driven by [LiquidVideoPlayerControls]; [position] and [duration] are in seconds. */
@Stable
class VideoPlayerState(duration: Float, playing: Boolean = false) {
    var duration by mutableFloatStateOf(duration)
    var position by mutableFloatStateOf(0f)
    var playing by mutableStateOf(playing)
    var volume by mutableFloatStateOf(0.7f)
    var muted by mutableStateOf(false)
    var speed by mutableFloatStateOf(1f)
    var captions by mutableStateOf<String?>(null)
    var audioTrack by mutableStateOf("English")
    var pictureInPicture by mutableStateOf(false)
    var fullscreen by mutableStateOf(false)
    var scrubbing by mutableStateOf(false)

    fun seek(seconds: Float) {
        position = seconds.coerceIn(0f, duration)
    }

    fun skip(seconds: Float) = seek(position + seconds)
}

@Composable
fun rememberVideoPlayerState(duration: Float = 596f): VideoPlayerState {
    val state = remember { VideoPlayerState(duration) }
    LaunchedEffect(state) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L && state.playing && !state.scrubbing) {
                    state.position = (state.position + (now - last) / 1e9f * state.speed).coerceAtMost(state.duration)
                    if (state.position >= state.duration) state.playing = false
                }
                last = now
            }
        }
    }
    return state
}

/**
 * iOS 26 AVKit-style player chrome over video content: close / picture-in-picture / volume at the
 * top, skip-back / play-pause / skip-forward glass buttons in the center, and a glass platter at the
 * bottom with the scrubber, times and the AirPlay, captions, playback-options and fullscreen buttons.
 * A tap on the video toggles the chrome; it auto-hides after 3 s while playing.
 */
@Composable
fun BoxScope.LiquidVideoPlayerControls(
    state: VideoPlayerState,
    backdrop: Backdrop,
    title: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    skipInterval: Float = 10f,
) {
    var visible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableStateOf(0) }
    var optionsMenu by remember { mutableStateOf(false) }
    var captionsMenu by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f)
    val touch = { lastInteraction++; visible = true }

    LaunchedEffect(visible, state.playing, state.scrubbing, lastInteraction, optionsMenu, captionsMenu) {
        if (visible && state.playing && !state.scrubbing && !optionsMenu && !captionsMenu) {
            delay(3000)
            visible = false
        }
    }

    Box(
        Modifier
            .matchParentSize()
            .pointerInput(Unit) { detectTapGestures { visible = !visible } }
    )
    if (alpha == 0f) return

    Box(
        modifier
            .matchParentSize()
            .graphicsLayer { this.alpha = alpha }
    ) {
        Row(
            Modifier.align(Alignment.TopStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onClose != null) {
                LiquidIconButton(GlassIcons.Close, "Close", { touch(); onClose() }, backdrop, iconColor = Color.White)
            }
            LiquidIconButton(
                GlassIcons.PictureInPicture,
                if (state.pictureInPicture) "Exit picture in picture" else "Picture in picture",
                { touch(); state.pictureInPicture = !state.pictureInPicture },
                backdrop,
                iconColor = Color.White
            )
        }
        LiquidVolumeControl(state, backdrop, Modifier.align(Alignment.TopEnd).padding(16.dp), onInteraction = touch)

        Row(
            Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidIconButton(GlassIcons.GoBackward, "Skip back ${skipInterval.toInt()} seconds", { touch(); state.skip(-skipInterval) }, backdrop, size = 56.dp, iconSize = 26.dp, iconColor = Color.White)
            LiquidIconButton(
                if (state.playing) GlassIcons.Pause else GlassIcons.Play,
                if (state.playing) "Pause" else "Play",
                {
                    touch()
                    if (!state.playing && state.position >= state.duration) state.seek(0f)
                    state.playing = !state.playing
                },
                backdrop,
                size = 72.dp,
                iconSize = 34.dp,
                iconColor = Color.White
            )
            LiquidIconButton(GlassIcons.GoForward, "Skip forward ${skipInterval.toInt()} seconds", { touch(); state.skip(skipInterval) }, backdrop, size = 56.dp, iconSize = 26.dp, iconColor = Color.White)
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
                .liquidGlassPanel(backdrop, { Capsule() }, containerColor = Color.Black.copy(alpha = 0.12f))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(title, Modifier.weight(1f), style = TextStyle(Color.White, 15.sp, FontWeight.SemiBold), maxLines = 1)
                PlayerBarButton(GlassIcons.AirPlay, "AirPlay") { touch() }
                PlayerBarButton(GlassIcons.Captions, "Subtitles", if (state.captions != null) Color.White else Color.White.copy(alpha = 0.7f)) { touch(); captionsMenu = true }
                PlayerBarButton(GlassIcons.Ellipsis, "Playback options") { touch(); optionsMenu = true }
                PlayerBarButton(GlassIcons.Expand, if (state.fullscreen) "Exit full screen" else "Full screen") { touch(); state.fullscreen = !state.fullscreen }
            }
            LiquidScrubber(state, onInteraction = touch)
        }

        LiquidMenu(
            expanded = optionsMenu,
            onDismissRequest = { optionsMenu = false },
            backdrop = backdrop,
            entries = listOf(
                MenuEntry.Submenu(
                    "Playback Speed",
                    GlassIcons.Gauge,
                    listOf(0.5f, 1f, 1.25f, 1.5f, 2f).map { s ->
                        MenuEntry.Action(speedLabel(s), checked = state.speed == s, onClick = { state.speed = s; optionsMenu = false })
                    }
                ),
                MenuEntry.Submenu(
                    "Audio",
                    GlassIcons.Speaker,
                    listOf("English", "Japanese").map { t ->
                        MenuEntry.Action(t, checked = state.audioTrack == t, onClick = { state.audioTrack = t; optionsMenu = false })
                    }
                ),
                MenuEntry.Divider,
                MenuEntry.Action("Picture in Picture", GlassIcons.PictureInPicture, onClick = { state.pictureInPicture = true; optionsMenu = false }),
            ),
            alignment = Alignment.BottomEnd,
            offset = androidx.compose.ui.unit.DpOffset((-16).dp, (-104).dp),
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 1f),
        )
        LiquidMenu(
            expanded = captionsMenu,
            onDismissRequest = { captionsMenu = false },
            backdrop = backdrop,
            entries = listOf<String?>(null, "English", "Japanese", "English (SDH)").map { c ->
                MenuEntry.Action(c ?: "Off", checked = state.captions == c, onClick = { state.captions = c; captionsMenu = false })
            },
            alignment = Alignment.BottomEnd,
            offset = androidx.compose.ui.unit.DpOffset((-60).dp, (-104).dp),
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 1f),
            width = 220.dp,
        )
    }
}

@Composable
private fun PlayerBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    color: Color = Color.White,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .width(40.dp)
            .height(36.dp)
            .semantics { contentDescription = description }
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        contentAlignment = Alignment.Center
    ) {
        GlassIcon(icon, color, Modifier.width(22.dp).height(22.dp))
    }
}

/** Video scrubber: a thin track that thickens while dragged, with elapsed and remaining time. */
@Composable
fun LiquidScrubber(state: VideoPlayerState, modifier: Modifier = Modifier, onInteraction: () -> Unit = {}) {
    val thickness by animateFloatAsState(if (state.scrubbing) 10f else 5f)
    val currentOnInteraction by rememberUpdatedState(onInteraction)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .semantics {
                    contentDescription = "Playback position"
                    progressBarRangeInfo = ProgressBarRangeInfo(state.position, 0f..state.duration)
                    setProgress { state.seek(it); true }
                }
                .pointerInput(state) {
                    detectTapGestures { state.seek(it.x / size.width * state.duration); currentOnInteraction() }
                }
                .pointerInput(state) {
                    detectDragGestures(
                        onDragStart = { state.scrubbing = true; state.seek(it.x / size.width * state.duration) },
                        onDragEnd = { state.scrubbing = false },
                        onDragCancel = { state.scrubbing = false },
                    ) { change, _ ->
                        state.seek(change.position.x / size.width * state.duration)
                        currentOnInteraction()
                    }
                }
        ) {
            val h = thickness.dp.toPx()
            val top = (size.height - h) / 2f
            val fraction = if (state.duration > 0f) state.position / state.duration else 0f
            drawRoundRect(Color.White.copy(alpha = 0.3f), Offset(0f, top), Size(size.width, h), CornerRadius(h / 2f))
            drawRoundRect(Color.White, Offset(0f, top), Size(size.width * fraction, h), CornerRadius(h / 2f))
        }
        Row {
            BasicText(formatTime(state.position), Modifier.weight(1f), style = TextStyle(Color.White.copy(alpha = 0.8f), 12.sp, FontWeight.Medium))
            BasicText("-" + formatTime(state.duration - state.position), style = TextStyle(Color.White.copy(alpha = 0.8f), 12.sp, FontWeight.Medium))
        }
    }
}

/** Speaker glass button that expands into a volume slider capsule; a tap on the speaker mutes. */
@Composable
fun LiquidVolumeControl(state: VideoPlayerState, backdrop: Backdrop, modifier: Modifier = Modifier, onInteraction: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val width by animateFloatAsState(if (expanded) 180f else 44f)
    Row(
        modifier
            .width(width.dp)
            .height(44.dp)
            .liquidGlass(backdrop, { Capsule() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(44.dp)
                .fillMaxSize()
                .semantics { contentDescription = if (state.muted) "Unmute" else "Mute" }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onInteraction(); if (expanded) state.muted = !state.muted else expanded = true },
                        onLongPress = { onInteraction(); expanded = !expanded }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            GlassIcon(if (state.muted || state.volume == 0f) GlassIcons.SpeakerSlash else GlassIcons.Speaker, Color.White, Modifier.width(20.dp).height(20.dp))
        }
        if (expanded && width > 120f) {
            Canvas(
                Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
                    .height(24.dp)
                    .semantics {
                        contentDescription = "Volume"
                        progressBarRangeInfo = ProgressBarRangeInfo(state.volume, 0f..1f)
                        setProgress { state.volume = it.coerceIn(0f, 1f); true }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            state.volume = (change.position.x / size.width).coerceIn(0f, 1f)
                            state.muted = false
                            onInteraction()
                        }
                    }
            ) {
                val h = 5.dp.toPx()
                val top = (size.height - h) / 2f
                val v = if (state.muted) 0f else state.volume
                drawRoundRect(Color.White.copy(alpha = 0.3f), Offset(0f, top), Size(size.width, h), CornerRadius(h / 2f))
                drawRoundRect(Color.White, Offset(0f, top), Size(size.width * v, h), CornerRadius(h / 2f))
            }
        }
    }
}

private fun speedLabel(speed: Float): String =
    (if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()) + "×"

private fun formatTime(seconds: Float): String {
    val s = seconds.coerceAtLeast(0f).toInt()
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
