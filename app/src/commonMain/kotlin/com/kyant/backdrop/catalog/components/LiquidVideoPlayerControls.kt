package com.kyant.backdrop.catalog.components

import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import com.kyant.shapes.RoundedRectangle
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.background
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

/** An iOS 26 AVKit-style player: [frame] is the video surface, sampled as the backdrop of the glass chrome. */
@Composable
fun LiquidVideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier = Modifier,
    captionText: String? = null,
    onClose: (() -> Unit)? = null,
    frame: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    Box(modifier.clipToBounds()) {
        Box(Modifier.matchParentSize().layerBackdrop(backdrop), content = frame)
        LiquidVideoPlayerControls(state, backdrop, captionText = captionText, onClose = onClose)
    }
}

/**
 * iOS 26 AVKit-style player chrome over video content. Inline: a fullscreen + AirPlay capsule,
 * skip-back / play-pause / skip-forward glass buttons and a scrubber capsule with a "..." menu
 * (Playback Speed, Audio Track, Subtitles). Fullscreen ([VideoPlayerState.fullscreen]): close and
 * AirPlay buttons, a mute button, a speed / audio / subtitles capsule and a full-width scrubber.
 * While scrubbing only the scrubber stays; a tap on the video toggles the chrome, which auto-hides
 * after 3 s while playing.
 */
@Composable
fun BoxScope.LiquidVideoPlayerControls(
    state: VideoPlayerState,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    captionText: String? = null,
    onClose: (() -> Unit)? = null,
    skipInterval: Float = 10f,
) {
    var visible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableStateOf(0) }
    var menu by remember { mutableStateOf<PlayerMenu?>(null) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f)
    val chromeAlpha by animateFloatAsState(if (state.scrubbing) 0f else 1f)
    val touch = { lastInteraction++; visible = true }
    val full = state.fullscreen

    LaunchedEffect(visible, state.playing, state.scrubbing, lastInteraction, menu) {
        if (visible && state.playing && !state.scrubbing && menu == null) {
            delay(3000)
            visible = false
        }
    }

    Box(
        Modifier
            .matchParentSize()
            .pointerInput(Unit) { detectTapGestures { visible = !visible } }
    )
    if (captionText != null && state.captions != null) {
        BasicText(
            captionText,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (full) 76.dp else 56.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedRectangle(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            style = TextStyle(Color.White, if (full) 17.sp else 11.sp)
        )
    }
    if (alpha == 0f) return

    val edge = if (full) 16.dp else 8.dp
    Box(
        modifier
            .matchParentSize()
            .graphicsLayer { this.alpha = alpha }
    ) {
        Box(Modifier.matchParentSize().graphicsLayer { this.alpha = chromeAlpha }) {
            if (full) {
                Row(Modifier.align(Alignment.TopStart).padding(edge), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onClose != null) {
                        LiquidIconButton(GlassIcons.Close, "Close", { touch(); onClose() }, backdrop, iconColor = Color.White)
                    }
                    LiquidIconButton(GlassIcons.AirPlay, "AirPlay", { touch() }, backdrop, iconColor = Color.White)
                }
                LiquidIconButton(
                    if (state.muted) GlassIcons.SpeakerSlash else GlassIcons.Speaker,
                    if (state.muted) "Unmute" else "Mute",
                    { touch(); state.muted = !state.muted },
                    backdrop,
                    Modifier.align(Alignment.TopEnd).padding(edge),
                    iconColor = Color.White
                )
                LiquidButtonGroup(backdrop, Modifier.align(Alignment.BottomEnd).padding(end = edge, bottom = 76.dp)) {
                    LiquidGroupItem(GlassIcons.Gauge, "Playback speed", { touch(); menu = PlayerMenu.Speed }, color = Color.White)
                    LiquidGroupItem(GlassIcons.Waveform, "Audio track", { touch(); menu = PlayerMenu.Audio }, color = Color.White)
                    LiquidGroupItem(GlassIcons.Captions, "Subtitles", { touch(); menu = PlayerMenu.Subtitles }, color = Color.White)
                }
            } else {
                LiquidButtonGroup(backdrop, Modifier.align(Alignment.TopStart).padding(edge), height = 36.dp) {
                    LiquidGroupItem(GlassIcons.Expand, "Full screen", { touch(); state.fullscreen = true }, color = Color.White)
                    LiquidGroupItem(GlassIcons.AirPlay, "AirPlay", { touch() }, color = Color.White)
                }
            }
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(if (full) 64.dp else 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val small = if (full) 60.dp else 44.dp
                LiquidIconButton(GlassIcons.GoBackward, "Skip back ${skipInterval.toInt()} seconds", { touch(); state.skip(-skipInterval) }, backdrop, size = small, iconSize = small * 0.5f, iconColor = Color.White)
                LiquidIconButton(
                    if (state.playing) GlassIcons.Pause else GlassIcons.Play,
                    if (state.playing) "Pause" else "Play",
                    {
                        touch()
                        if (!state.playing && state.position >= state.duration) state.seek(0f)
                        state.playing = !state.playing
                    },
                    backdrop,
                    size = if (full) 88.dp else 60.dp,
                    iconSize = if (full) 38.dp else 28.dp,
                    iconColor = Color.White
                )
                LiquidIconButton(GlassIcons.GoForward, "Skip forward ${skipInterval.toInt()} seconds", { touch(); state.skip(skipInterval) }, backdrop, size = small, iconSize = small * 0.5f, iconColor = Color.White)
            }
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(edge)
                .height(if (full) 44.dp else 30.dp)
                .liquidGlassPanel(backdrop, { Capsule() }, containerColor = Color.Black.copy(alpha = 0.1f))
                .padding(horizontal = if (full) 16.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (full) 12.dp else 8.dp)
        ) {
            val timeStyle = TextStyle(Color.White.copy(alpha = 0.75f), if (full) 13.sp else 10.sp, FontWeight.Medium)
            BasicText(formatTime(state.position), style = timeStyle)
            LiquidScrubber(state, Modifier.weight(1f), onInteraction = touch)
            BasicText("-" + formatTime(state.duration - state.position), style = timeStyle)
            if (!full) {
                Box(
                    Modifier
                        .graphicsLayer { this.alpha = chromeAlpha }
                        .semantics { contentDescription = "More" }
                        .clickable(role = Role.Button) { touch(); menu = PlayerMenu.More }
                        .padding(horizontal = 2.dp)
                ) { GlassIcon(GlassIcons.Ellipsis, Color.White, Modifier.width(18.dp).height(18.dp)) }
            }
        }

        val close = { menu = null }
        val speedEntries = listOf(2f, 1.5f, 1.25f, 1f, 0.5f).map { s ->
            MenuEntry.Action(speedLabel(s), checked = state.speed == s, onClick = { state.speed = s; close() })
        }
        val audioEntries = listOf("English", "Japanese").map { t ->
            MenuEntry.Action(t, checked = state.audioTrack == t, onClick = { state.audioTrack = t; close() })
        }
        val subtitleEntries = listOf<String?>(null, "English", "Japanese").map { c ->
            MenuEntry.Action(c ?: "Off", checked = state.captions == c, onClick = { state.captions = c; close() })
        }
        val (entries, header) = when (menu) {
            PlayerMenu.Speed -> speedEntries to "Playback Speed"
            PlayerMenu.Audio -> audioEntries to "Audio Track"
            PlayerMenu.Subtitles -> subtitleEntries to "Subtitles"
            PlayerMenu.More, null -> listOf(
                MenuEntry.Submenu("Playback Speed", GlassIcons.Gauge, speedEntries),
                MenuEntry.Submenu("Audio Track", GlassIcons.Waveform, audioEntries),
                MenuEntry.Submenu("Subtitles", GlassIcons.Captions, subtitleEntries),
            ) to null
        }
        LiquidMenu(
            expanded = menu != null,
            onDismissRequest = close,
            backdrop = backdrop,
            entries = entries,
            alignment = if (full) Alignment.TopEnd else Alignment.BottomEnd,
            offset = if (full) DpOffset((-edge), 12.dp) else DpOffset((-edge), (-42).dp),
            transformOrigin = if (full) TransformOrigin(1f, 0f) else TransformOrigin(1f, 1f),
            width = if (menu == PlayerMenu.More || menu == null) 250.dp else 220.dp,
            header = header,
        )
    }
}

private enum class PlayerMenu { More, Speed, Audio, Subtitles }

/** Video scrubber track: thin capsule that thickens while dragged; tap or drag to seek. */
@Composable
fun LiquidScrubber(state: VideoPlayerState, modifier: Modifier = Modifier, onInteraction: () -> Unit = {}) {
    val thickness by animateFloatAsState(if (state.scrubbing) 9f else 6f)
    val currentOnInteraction by rememberUpdatedState(onInteraction)
    Canvas(
        modifier
            .height(24.dp)
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
                    onDragEnd = { state.scrubbing = false; currentOnInteraction() },
                    onDragCancel = { state.scrubbing = false },
                ) { change, _ ->
                    state.seek(change.position.x / size.width * state.duration)
                }
            }
    ) {
        val h = thickness.dp.toPx()
        val top = (size.height - h) / 2f
        val fraction = if (state.duration > 0f) state.position / state.duration else 0f
        drawRoundRect(Color.White.copy(alpha = 0.3f), Offset(0f, top), Size(size.width, h), CornerRadius(h / 2f))
        drawRoundRect(Color.White.copy(alpha = 0.92f), Offset(0f, top), Size(size.width * fraction, h), CornerRadius(h / 2f))
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
                .semantics {
                    contentDescription = if (state.muted) "Unmute" else "Mute"
                    role = Role.Button
                    onClick(if (expanded) "Toggle mute" else "Show volume") {
                        onInteraction(); if (expanded) state.muted = !state.muted else expanded = true; true
                    }
                }
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
