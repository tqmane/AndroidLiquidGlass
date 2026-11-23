# Liquid Glass (Backdrop)

![frontPhoto](artworks/banner.jpg)

A customizable Liquid Glass effect library for Compose Multiplatform.

## Docs

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kyant0/backdrop)](https://central.sonatype.com/artifact/io.github.kyant0/backdrop)

[Documentation](https://kyant.gitbook.io/backdrop)

## JitPack

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// build.gradle.kts
dependencies {
    // iOS 26 Liquid Glass components (also pulls in :backdrop)
    implementation("com.github.tqmane.AndroidLiquidGlass:components:<tag or commit>")
    // or only the low-level backdrop / glass effects
    implementation("com.github.tqmane.AndroidLiquidGlass:backdrop:<tag or commit>")
}
```

[![JitPack](https://jitpack.io/v/tqmane/AndroidLiquidGlass.svg)](https://jitpack.io/#tqmane/AndroidLiquidGlass)

## Components

See the [implementation guide](docs/IMPLEMENTATION_GUIDE.md) for the architecture, every iOS 26 component in the catalog (segmented control, sheets, alerts, menus, video player, ...), and common pitfalls.

The library does not include any high-level components; you will need to create your own.
Below are some example components:

- [LiquidButton](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidButton.kt)
- [LiquidToggle](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidToggle.kt)
- [LiquidSlider](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidSlider.kt)
- [LiquidBottomTabs](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidBottomTabs.kt)

## Comparison with iOS 26

Screenshots of the iOS 26 simulator next to the [catalog app](#demo) on an Android emulator. Each image shows iOS on the left and Android on the right, in light and dark mode.

### Buttons

![Buttons: iOS 26 vs Android](docs/images/compare/buttons.jpg)

### Segmented control

![Segmented control: iOS 26 vs Android](docs/images/compare/seg.jpg)

### Stepper

![Stepper: iOS 26 vs Android](docs/images/compare/stepper.jpg)

### Page control

![Page control: iOS 26 vs Android](docs/images/compare/pagecontrol.jpg)

### Progress and activity indicators

![Progress and activity indicators: iOS 26 vs Android](docs/images/compare/progress.jpg)

### Search bar

![Search bar: iOS 26 vs Android](docs/images/compare/search.jpg)

### Toolbar and navigation bar

![Toolbar and navigation bar: iOS 26 vs Android](docs/images/compare/toolbar.jpg)

### Menu

![Menu: iOS 26 vs Android](docs/images/compare/menu.jpg)

### Context menu

![Context menu: iOS 26 vs Android](docs/images/compare/context-menu.jpg)

### Popover

![Popover: iOS 26 vs Android](docs/images/compare/popover.jpg)

### Sheet (medium detent)

![Sheet (medium detent): iOS 26 vs Android](docs/images/compare/sheet-medium.jpg)

### Sheet (large detent)

![Sheet (large detent): iOS 26 vs Android](docs/images/compare/sheet-large.jpg)

### Alert (two buttons)

![Alert (two buttons): iOS 26 vs Android](docs/images/compare/alert-two.jpg)

### Alert (three buttons)

![Alert (three buttons): iOS 26 vs Android](docs/images/compare/alert-three.jpg)

### Materials

![Materials: iOS 26 vs Android](docs/images/compare/materials.jpg)

### Bottom tab bar

![Bottom tab bar: iOS 26 vs Android](docs/images/compare/bottom-tabs.jpg)

### iPad top tab bar

| iOS 26 (iPad) | Android (tablet) |
| --- | --- |
| ![iPad tab bar light](docs/images/compare/ios-ipad-floating-tab-bar-landscape-light.jpg) | ![Tablet tab bar light](docs/images/compare/android-ipad-tabs-light.jpg) |
| ![iPad tab bar dark](docs/images/compare/ios-ipad-floating-tab-bar-landscape-dark.jpg) | ![Tablet tab bar dark](docs/images/compare/android-ipad-tabs-dark.jpg) |

## Motion comparison with iOS 26

The animations were matched frame by frame against slow-motion iOS 26 simulator recordings. In each strip, the top row is iOS and the bottom row is Android, sampled at the same timestamps. Click a strip to view it at full size.

### Segmented control tap

![Segmented control tap: iOS 26 (top) vs Android (bottom)](docs/images/compare/segmented.jpg)

### Glass button press

![Glass button press: iOS 26 (top) vs Android (bottom)](docs/images/compare/glass-circle-press.jpg)

### Sheet present

![Sheet present: iOS 26 (top) vs Android (bottom)](docs/images/compare/sheet-present.jpg)

### Sheet detent expand

![Sheet detent expand: iOS 26 (top) vs Android (bottom)](docs/images/compare/sheet-expand.jpg)

### Sheet dismiss

![Sheet dismiss: iOS 26 (top) vs Android (bottom)](docs/images/compare/sheet-btn-dismiss.jpg)

### Alert present

![Alert present: iOS 26 (top) vs Android (bottom)](docs/images/compare/alert-present.jpg)

### Action sheet present

![Action sheet present: iOS 26 (top) vs Android (bottom)](docs/images/compare/action-sheet.jpg)

### Search open (tab bar morph)

![Search open (tab bar morph): iOS 26 (top) vs Android (bottom)](docs/images/compare/search-open.jpg)

### Search cancel

![Search cancel: iOS 26 (top) vs Android (bottom)](docs/images/compare/search-cancel.jpg)

### Search return to Home

![Search return to Home: iOS 26 (top) vs Android (bottom)](docs/images/compare/search-return.jpg)

### iPad popover dialog

![iPad popover dialog: iOS 26 (top) vs Android (bottom)](docs/images/compare/ipad-dialog-present.jpg)

### iPad sheet present

![iPad sheet present: iOS 26 (top) vs Android (bottom)](docs/images/compare/ipad-sheet-present.jpg)

## Demo

- [Backdrop Catalog](./androidApp/release/androidApp-release.apk)

![Screenshots of Backdrop Catalog](artworks/catalog_app.jpg)
