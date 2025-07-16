import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.dosse.speedtest"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    api(compose.foundation)
    api(compose.animation)
    api("moe.tlaster:precompose:1.6.0")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.8.2")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.8.0")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.dosse.speedtest.res"
    generateResClass = auto
    customDirectory(
        sourceSetName = "main",
        directoryProvider = provider { layout.projectDirectory.dir("src/main/resources") },
    )
}

compose.desktop {
    application {
        mainClass = "LibreSpeedKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.sql")
            packageName = "LibreSpeed"
            packageVersion = "1.2.0"
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("files/icon_app.png"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("files/icon_app.ico"))
            }
            macOS {
                iconFile.set(iconsRoot.resolve("files/icon_app.ico"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(project.file("rules.pro").absolutePath)
        }
    }
}
