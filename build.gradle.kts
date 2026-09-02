import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.time.LocalDate

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

val sqliteJdbcVersion = "3.50.2.0"

//the Maven jar bundles a native library for 24 platforms (~13 MB); a package
//is built for exactly one, so only that platform's copy is worth shipping
val sqliteJdbc: Configuration by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

val sqliteNativeDir: String = run {
    val os = System.getProperty("os.name").lowercase()
    val arch = when (val a = System.getProperty("os.arch")) {
        "aarch64", "arm64" -> "aarch64"
        "amd64", "x86_64" -> "x86_64"
        "x86", "i386", "i686" -> "x86"
        else -> a
    }
    val family = when {
        os.contains("mac") -> "Mac"
        os.contains("win") -> "Windows"
        else -> "Linux"
    }
    "org/sqlite/native/$family/$arch/"
}

val slimSqliteJdbc by tasks.registering(Jar::class) {
    description = "sqlite-jdbc with only this platform's native library"
    archiveBaseName.set("sqlite-jdbc-slim")
    archiveVersion.set(sqliteJdbcVersion)
    from(sqliteJdbc.elements.map { it.map { file -> zipTree(file.asFile) } })
    exclude { el ->
        val path = el.relativePath.pathString
        path.startsWith("org/sqlite/native/") && !path.startsWith(sqliteNativeDir) && !el.isDirectory
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    doLast {
        //an unmapped os.name or os.arch would otherwise ship a jar with no native library at all
        val shipped = zipTree(archiveFile.get().asFile).matching { include("$sqliteNativeDir*") }.files
        if (shipped.isEmpty()) throw GradleException("sqlite-jdbc has no native library for $sqliteNativeDir")
    }
}

dependencies {
    //compile against the Maven artifact; ship the slim copy built below
    compileOnly("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
    runtimeOnly(files(slimSqliteJdbc))
    sqliteJdbc("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    api(compose.foundation)
    api(compose.animation)
    api("moe.tlaster:precompose:1.6.0")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.8.2")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.8.0")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
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
        jvmArgs += listOf(
            "-Dapp.version=${project.version}",
            "-Dapp.build.date=${LocalDate.now()}",
            //follow the OS address ordering (IPv6 preferred when available), like browsers do
            "-Djava.net.preferIPv6Addresses=system"
        )
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
                iconFile.set(iconsRoot.resolve("files/icon_app.icns"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(project.file("rules.pro").absolutePath)
        }
    }
}
