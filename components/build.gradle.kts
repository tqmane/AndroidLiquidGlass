import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    id("com.vanniktech.maven.publish")
}

kotlin {
    android {
        minSdk = 23 // androidx.activity:activity-compose (BackHandler) requires API 23
        compileSdk = 37
        buildToolsVersion = "37.0.0"
        namespace = "com.kyant.backdrop.components"
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    applyDefaultHierarchyTemplate()

    jvm("desktop")

    js {
        browser()
    }
    wasmJs {
        browser()
    }

    macosArm64()
    iosArm64("iosArm64")
    iosSimulatorArm64("iosSimulatorArm64")

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.graphics)
                api(project(":backdrop"))
                implementation(libs.compose.foundation)
                implementation(libs.kyant.shapes)
            }
        }

        getByName("androidMain") {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val skikoMain = create("skikoMain") {
            dependsOn(commonMain)
        }

        val desktopMain = getByName("desktopMain") {
            dependsOn(skikoMain)
        }

        val macosArm64Main = getByName("macosArm64Main") {
            dependsOn(skikoMain)
        }

        val iosMain = getByName("iosMain") {
            dependsOn(skikoMain)
        }

        val iosArm64Main = getByName("iosArm64Main") {
        }

        val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main") {
        }

        val jsMain = getByName("jsMain") {
            dependsOn(skikoMain)
        }

        val wasmJsMain = getByName("wasmJsMain") {
            dependsOn(skikoMain)
        }
    }
}

// JitPack sets JITPACK/GROUP/VERSION; it builds without signing keys and publishes to its own repository.
val jitpack = System.getenv("JITPACK") == "true"
val publishGroup = if (jitpack) System.getenv("GROUP") else "io.github.kyant0"
val publishVersion = if (jitpack) System.getenv("VERSION") else "2.0.8"

mavenPublishing {
    if (!jitpack) {
        publishToMavenCentral()
        signAllPublications()
    }

    coordinates(publishGroup, "components", publishVersion)

    pom {
        name.set("Backdrop Components")
        description.set("iOS 26 Liquid Glass components for Compose Multiplatform")
        inceptionYear.set("2025")
        url.set("https://github.com/Kyant0/AndroidLiquidGlass")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Kyant0")
                name.set("Kyant")
                url.set("https://github.com/Kyant0")
            }
        }
        scm {
            url.set("https://github.com/Kyant0/AndroidLiquidGlass")
            connection.set("scm:git:git://github.com/Kyant0/AndroidLiquidGlass.git")
            developerConnection.set("scm:git:ssh://git@github.com/Kyant0/AndroidLiquidGlass.git")
        }
    }
}
