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

## Demo

- [Backdrop Catalog](./androidApp/release/androidApp-release.apk)

![Screenshots of Backdrop Catalog](artworks/catalog_app.jpg)
