import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.dosse.speedtest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs)
    api(compose.foundation)
    api(compose.animation)
    api("moe.tlaster:precompose:1.6.0")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.6.11")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.8.0")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
}

compose.desktop {
    application {
        mainClass = "LibreSpeedKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.sql")
            packageName = "LibreSpeed"
            packageVersion = "1.1.0"
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("icons/icon_app.png"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icons/icon_app.ico"))
            }
            macOS {
                iconFile.set(iconsRoot.resolve("icons/icon_app.ico"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(project.file("rules.pro").absolutePath)
        }
    }
}
