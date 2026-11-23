# Liquid Glass implementation guide

This guide is for developers and AI coding agents who add or use Liquid Glass components in this repository. It covers the architecture, the steps for each kind of component, and the mistakes that already broke the catalog once.

- `:backdrop` is the published library (`io.github.kyant0:backdrop`). It only provides low-level primitives: backdrops, `drawBackdrop` and effects (blur, lens, vibrancy, highlight, shadow).
- `:app` is the Compose Multiplatform catalog. The reusable iOS 26 style components live in `app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/`, and each catalog page lives in `.../catalog/destinations/`.
- `:androidApp` is the Android launcher for the catalog. `iosApp` is the iOS launcher.

## 1. Core model: who samples what

Liquid Glass is a surface that refracts and blurs **what is drawn behind it**. In this library, "behind" is a `Backdrop` that you record yourself.

```kotlin
val backdrop = rememberLayerBackdrop()
Box(Modifier.fillMaxSize()) {
    // 1. Content that should show through the glass is recorded into the backdrop.
    Box(Modifier.matchParentSize().layerBackdrop(backdrop)) { Wallpaper() }

    // 2. Glass is drawn as a SIBLING above the recorded content and samples it.
    LiquidButton(onClick = {}, backdrop = backdrop) { BasicText("Done") }
}
```

Rules:

1. **Glass must never be inside the layer it samples.** Put `layerBackdrop(backdrop)` on a sibling, or on a parent whose glass children use a different backdrop. Otherwise the glass records itself.
2. **Glass on top of glass samples the glass below it.** A panel (sheet, alert, menu, popover) draws its own backdrop and exports it:

   ```kotlin
   val panelBackdrop = rememberLayerBackdrop()
   Column(Modifier.liquidGlassPanel(pageBackdrop, { RoundedRectangle(34.dp) }, exportedBackdrop = panelBackdrop)) {
       LiquidIconButton(GlassIcons.Close, "Close", onClose, panelBackdrop) // NOT pageBackdrop
   }
   ```

   If a control inside a panel uses the page backdrop, it shows the wallpaper **behind** the panel, so a button inside a white sheet looks tinted by the wallpaper. This was a real bug in the sheet's X button. `LiquidSheet` solves it for you: inside its content lambda, `backdrop` (from `LiquidSheetScope`) is the sheet's own exported backdrop, so always pass that one to controls in a sheet.
3. **Video and image surfaces are backdrops too.** `LiquidVideoPlayer` records its `frame` into a private backdrop, and the transport controls sample the video, not the page.
4. `BackdropDemoScaffold { backdrop -> ... }` (catalog only) supplies a wallpaper backdrop and a picker button for the wallpaper image. Use it for demo pages whose glass floats over a wallpaper. For pages whose content is the backdrop (video, lists), build the backdrop yourself.

## 2. Glass building blocks (`GlassSurface.kt`)

| API | Use for | Notes |
| --- | --- | --- |
| `Modifier.liquidGlass(backdrop, shape, tint, surfaceColor, blurRadius = 2.dp, refractionHeight = 12.dp, refractionAmount = 24.dp, shadow = true)` | Controls: buttons, capsules, thumbs | Light blur and strong lens. `tint` colors the glass (prominent buttons). |
| `Modifier.liquidGlassPanel(backdrop, shape, containerColor, blurRadius = 12.dp, refractionHeight = 16.dp, refractionAmount = 32.dp, exportedBackdrop = null)` | Panels: sheets, alerts, menus, popovers, scrubber bars | Heavy blur keeps text readable. Pass `exportedBackdrop` when controls sit on the panel. |
| `GlassColors.label() / secondaryLabel() / accent() / destructive() / separator() / fill()` | iOS semantic colors | `@Composable`, they follow `isSystemInDarkTheme()`. Never hard-code black or white text on glass. |
| `GlassIcons.*` | SF Symbols look-alikes as `ImageVector` | Add new icons here. Use `curveTo(...)`, because `cubicTo` does not exist in the path builder. |

Write new modifiers by calling `drawBackdrop(...)` from `:backdrop` directly, following `liquidGlass`. Read global settings (section 4) **inside** the `effects` / `onDrawSurface` lambdas so changes apply live without recomposition.

## 3. Component catalog and how to use each one

All overlays (`LiquidSheet`, `LiquidAlert`, `LiquidActionSheet`, `LiquidMenu`, `LiquidPopover`, `LiquidGlassPopup`, `LiquidContextMenu`) are `BoxScope` extensions. Call them as the **last children of a full-size `Box`** that covers the screen. They draw a scrim with `zIndex`, consume outside taps and register `BackHandler(visible)`, so system Back dismisses the overlay instead of leaving the page.

| Component | File | Key API / behavior |
| --- | --- | --- |
| Segmented control | `LiquidSegmentedControl.kt` | `LiquidSegmentedControl(segments: List<SegmentItem>, selectedIndex, onSelect, modifier, momentary, sizing = Equal / ByContent, backdrop)`. Drag the thumb to switch, with lens and snapping. Height is fixed to the iOS touch target. |
| Bottom tabs / toolbar | `LiquidBottomTabs.kt`, `LiquidBottomTab.kt` | `selectedTabIndex: () -> Int`. Pass a **state-reading lambda** `{ selected }`, not a captured `Int`, or the selection stops updating. |
| Buttons | `LiquidButton.kt`, `LiquidIconButton.kt`, `LiquidBorderedButton.kt` | `LiquidIconButton(icon, contentDescription, onClick, backdrop, size = 44.dp, tint)`; `LiquidButtonGroup { LiquidGroupItem(...) }` makes capsule groups; `LiquidBorderedButton(prominent, destructive, size)` makes iOS bordered and filled buttons. |
| Toggle, slider, stepper | `LiquidToggle.kt`, `LiquidSlider.kt`, `LiquidStepper.kt` | Toggle and slider take value **lambdas**. The stepper changes its value on release and supports `range`, `step` and `wraps`. |
| Page control | `LiquidPageControl.kt` | Dots with a glass platter while touched. The hit target is larger than the dots, and it exposes accessibility semantics. |
| Progress | `LiquidProgressView.kt` | `LiquidProgressView(progress)`, `LiquidActivityIndicator(size)` and `LiquidCircularGauge(progress)`. |
| Search | `LiquidSearchBar.kt` | `LiquidSearchBar` is the glass capsule. `SearchFieldInline` is the inline field used in lists and sheets. |
| Sheet | `LiquidSheet.kt` | `LiquidSheet(visible, onDismissRequest, backdrop, detents = listOf(Medium, Large), showGrabber, title) { ... }`. See section 3.1. |
| Alert, action sheet | `LiquidAlert.kt` | `LiquidAlert(title, message, actions: List<AlertAction>)`, where `AlertActionStyle` is Default, Cancel, Destructive or Preferred. `LiquidActionSheet` is the bottom variant. |
| Menu, submenu, popover | `LiquidMenu.kt` | `LiquidMenu(expanded, onDismissRequest, backdrop, entries, alignment, offset, transformOrigin, width, header)`. `MenuEntry.Action` supports checked, destructive and subtitle. `MenuEntry.Submenu` replaces the list in place. Content scrolls when the menu is taller than its container. |
| Context menu | `LiquidContextMenu.kt` | `LiquidContextMenu(expanded, ..., anchorBounds: Rect, entries, preview)`. The preview and menu are clamped to the viewport. |
| Global glass settings | `LiquidGlassStyle.kt` | See section 4. |
| Video player | `LiquidVideoPlayerControls.kt` | See section 3.2. |

### 3.1 Sheets (match iOS 26 exactly)

iOS 26 has two sheet header styles. Choose the one the native screen uses, and do not add buttons that are not in the reference:

- **Title-only** (for example Browse or Library pickers): grabber, centered title, **no X or check buttons**. Use `LiquidSheetHeader("Browse")`.
- **Toolbar** (for example Edit collection): X (cancel) at the leading edge and check (done, accent tint) at the trailing edge.

  ```kotlin
  LiquidSheet(visible, { visible = false }, pageBackdrop, title = "Edit collection") {
      val sheet = this
      LiquidSheetHeader(
          "Edit collection",
          leading = { LiquidIconButton(GlassIcons.Close, "Cancel", onClose, backdrop) },   // sheet backdrop
          trailing = { LiquidIconButton(GlassIcons.Check, "Done", onClose, backdrop, tint = GlassColors.accent()) }
      )
      Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
          sheet.SheetSection("Details") {
              GroupedCell("Name", value = "Weekend plans")
              GroupedCell("Location", value = "On this iPhone", showSeparator = true)
          }
      }
  }
  ```

Sheet pitfalls:

- Inside the sheet lambda, `backdrop` is the **sheet's** backdrop. The page backdrop is the one you passed to `LiquidSheet(...)`. Use the sheet's backdrop for everything inside the sheet.
- Calling `SheetSection` from a nested lambda can fail with "cannot be called in this context with an implicit receiver". Capture `val sheet = this` and call `sheet.SheetSection(...)`.
- Scrollable content must be `Modifier.weight(1f, fill = false).verticalScroll(...)`. The sheet hands drag gestures to the list once the list scrolls, and resizes the sheet when the list is at the top.
- `expansion` (0 = medium, 1 = large) is exposed so cell colors can move from translucent (medium) to opaque (large) like iOS. `SheetSection` already does this.
- Detents are fractions (`SheetDetent.Fraction(f)`; `Medium` = 0.5, `Large` = 1). Pass a single detent for medium-only or large-only sheets.

### 3.2 Video player

```kotlin
val state = rememberVideoPlayerState(duration = 60f)
BackHandler(state.fullscreen) { state.fullscreen = false }
LiquidVideoPlayer(
    state,
    Modifier.fillMaxWidth().aspectRatio(16f / 9f),
    captionText = { language -> captionsFor(language) },
    onClose = { state.fullscreen = false },
) { VideoSurface() } // your ExoPlayer / AVPlayer / image frame
```

- `VideoPlayerState` holds position, duration, playing, volume, muted, speed, captions (language or `null`), audioTrack, fullscreen and scrubbing. Connect it to your real player in both directions, because the controls only change this state.
- Inline layout: a fullscreen and AirPlay capsule at the top left, skip back, play/pause and skip forward in the center, and a scrubber capsule with elapsed time, remaining time and a "…" menu (Playback Speed, Audio Track, Subtitles).
- Fullscreen layout (`state.fullscreen = true`): close and AirPlay at the top left, mute at the top right, a speed / audio / subtitles capsule above a full-width scrubber. The chrome is inset with `safeDrawingPadding()`.
- The chrome auto-hides after 3 s while playing, and a tap on the video toggles it. If a menu is open, the tap closes the menu first. While scrubbing, only the scrubber stays visible.

## 4. Global glass intensity (iOS 27 style settings)

```kotlin
val glassStyle = remember { LiquidGlassStyle() }   // refraction, blur, opacity multipliers (1 = default)
CompositionLocalProvider(LocalLiquidGlassStyle provides glassStyle) { App() }

// Anywhere below:
LocalLiquidGlassStyle.current.refraction = 1.2f
glassStyle.reset()
```

Presets: `LiquidGlassStyle.Clear` (stronger lens, low blur, low opacity) and `LiquidGlassStyle.Tinted` (more blur and opacity). The multipliers scale the lens height and amount, the blur radius, and the surface and tint alpha in `liquidGlass` / `liquidGlassPanel`. New glass modifiers must read `LocalLiquidGlassStyle.current` and apply the same scaling, or they will ignore the setting. The catalog's `GlassSettingsContent` is the reference UI.

## 5. Tablets and iPad (regular width)

iPadOS 26 presents several components differently from iPhone. The catalog switches at a window width of **600 dp** or more, measured with `BoxWithConstraints` on the component's own container (not the screen).

| Component | Phone (compact) | Tablet (regular, >= 600 dp) |
|---|---|---|
| `LiquidSheet` | Bottom sheet, full width, medium/large detents | Centered form sheet, 580 dp wide, all four corners rounded; large is 635 dp tall and vertically centered, medium is 366 dp tall and keeps the same bottom edge (it sits below the center, like iPadOS) |
| Toolbar sheet header | X (left) and check (right) glass icon buttons | Grey `Cancel` capsule (`LiquidBorderedButton(neutral = true)`) and prominent blue `Done` capsule |
| `LiquidActionSheet` | Bottom panel, includes the Cancel action | 280 dp popover anchored to its source with an arrow (pass `anchorBounds` from `Modifier.contextMenuAnchor`), above the source when it fits, otherwise below; centered when no source is given; Cancel action hidden (tap outside or Back dismisses) |
| Toolbar / tab bar demo | Full-width bottom bar | Bottom bar capped at 560 dp wide |
| `LiquidAdaptiveTabScaffold` | `LiquidBottomTabs` at the bottom, search tab as a separate glass circle | `LiquidTopTabBar` floating at the top center (sidebar toggle inside the capsule, text tabs, icon-only search tab); the toggle slides in a 270 dp `LiquidSidebar` and fades the top bar out |
| `LiquidSplitView` | Sidebar closed; glass toggle at the top start opens it over the content | 320 dp sidebar with a centered title; open by default when the window is >= 1000 dp |
| Open sidebar | Floats over dimmed content | Beside the content when the window is >= 1000 dp (iPad landscape), over dimmed content below that (iPad portrait); tapping the dimmed area closes it |
| Fullscreen video | Locks to landscape | Keeps the current orientation (locking a large screen letterboxes the app) |

Rules:
- Read `LiquidSheetScope.isFormSheet` inside sheet content to pick header controls; do not check the screen size yourself.
- A form sheet is still a backdrop exporter: controls inside it keep using the sheet scope's `backdrop`.
- Measure width from the layout (`BoxWithConstraints`), never from the display metrics: split screen and freeform windows on Android tablets are narrower than the screen.
- Check both orientations. In portrait a 13-inch iPad is still regular width, so it keeps the form sheet.
- Use `LiquidAdaptiveTabScaffold` (SwiftUI `TabView` + `.sidebarAdaptable`) for app-level tabs: pass `tabs` for the bar and `sidebarSections` for the sidebar (the first section with a null title is normally the same tabs; later sections are extra destinations that only the sidebar shows). Draw page content inside its `content` lambda and apply the `PaddingValues` it gives you: it already clears the top bar, the sidebar and the system bars. Do not wrap the scaffold in another backdrop scaffold; it records `content` into its own `LayerBackdrop`.
- Use `LiquidSplitView` (SwiftUI `NavigationSplitView`) for a two-column app; its sections are not collapsible, while the tab scaffold's titled sidebar sections collapse with their chevron.
- On regular width the top bar and the sidebar are alternatives: showing the sidebar fades the top bar out, like iPadOS. Selecting a sidebar-only destination leaves no pill selected in the top bar.

## 6. Adding a new component (checklist)

1. Get native iOS 26 references in light and dark (iPhone, and iPad when the layout differs). Write down sizes, corner radii, fonts and which buttons exist.
2. Add the reusable composable in `catalog/components/`. Take `backdrop: Backdrop` as a parameter, use `GlassColors` for colors, use `liquidGlass` / `liquidGlassPanel` for surfaces, and use `LiquidSheetScope.backdrop` / exported backdrops for nested controls.
3. Add a page in `catalog/destinations/`, an entry in `CatalogDestination`, a route in `MainContent.kt` and a row in `HomeContent.kt`.
4. Build with `./gradlew :app:compileAndroidMain` for a fast check, or `./gradlew :androidApp:assembleDebug` for the APK.
5. On an emulator, capture light and dark screenshots on a phone and a tablet, and compare them side by side with the iOS references. Check gestures, Back, TalkBack and rotation.

## 7. Pitfalls that already caused bugs

| Symptom | Cause | Fix |
| --- | --- | --- |
| Crash `IllegalArgumentException: Padding must be non-negative` | `Modifier.padding(horizontal = (-16).dp)` used to cancel a parent's padding | Never use negative padding. Add a parameter instead (for example `GroupedSection(horizontalPadding = 0.dp)`) or use `Modifier.offset` / `layout`. |
| Button inside a sheet shows the wallpaper color | Control used the page backdrop | Use the panel's exported backdrop (`LiquidSheetScope.backdrop`). |
| Segmented control or other component fills the rest of a `Column` and pushes everything off screen | Component used `fillMaxSize()` inside unbounded `heightIn(min = …)` | Give the component a fixed or capped height (`heightIn(min = h, max = h)`). |
| Tab selection does not change | Passed a snapshot `Int` where `() -> Int` was expected | Pass `{ selectedTab }`. |
| Menu items cut off in a small container (inline video) | Overlay clipped by `clipToBounds` parent, no scrolling | Cap the menu height to the container (`Modifier.heightIn(max = …)`), and the menu scrolls. Pointer input outside a parent's bounds is never delivered, so an overflowing menu is not a fix. |
| Fullscreen controls under the navigation bar | No window insets | `safeDrawingPadding()` on the chrome only. The video itself stays edge to edge. |
| Controls look light inside a video in light mode | Glass colors follow the system theme | Wrap player chrome in `CompositionLocalProvider(LocalGlassDarkAppearance provides true)`; `LiquidVideoPlayer` does this. New components must read `isGlassDark()`, never `isSystemInDarkTheme()`. |
| Rotating the device closes an open sheet or exits fullscreen video | Activity recreated on rotation; component state is in `remember` | The catalog's `MainActivity` declares `android:configChanges="orientation|screenSize|…"`. Do the same, or hoist state into `rememberSaveable`/a ViewModel. `LockLandscape(enabled)` rotates for fullscreen video. |
| Screen reader cannot activate a button | Only `pointerInput { detectTapGestures }` | Use `clickable(role = Role.Button)` or `semantics { onClick(...) }`. Add `contentDescription` to icon-only buttons. |
| Back closes the page instead of the overlay | Overlay did not register a handler | `BackHandler(visible) { onDismissRequest() }` (`catalog/utils/BackHandler.kt`). |
| Drag on a control also triggers a tap action | Tap and drag detectors both fire | Commit on release, and ignore the tap when a drag happened (see `LiquidSegmentedControl` and `LiquidStepper`). |
| Global glass setting has no effect on a new component | Hard-coded blur, lens or alpha | Multiply by `LocalLiquidGlassStyle.current.*` inside the draw lambdas. |

## 8. Build and environment notes

- Kotlin Multiplatform, Compose Multiplatform. Dependency versions are in `gradle/libs.versions.toml`; min SDK is 21 for `:app` and 23 for `:androidApp` (module `build.gradle.kts`).
- If Maven Central returns HTTP 429, use the Google mirror `https://maven-central.storage-download.googleapis.com/maven2/`.
- Install the debug APK with `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Screenshots for PRs: light and dark, phone and tablet, each placed next to the matching iOS 26 reference.
