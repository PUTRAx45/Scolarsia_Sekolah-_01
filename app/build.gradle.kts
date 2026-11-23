import java.util.Properties
import java.net.NetworkInterface
import java.net.Inet4Address

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun detectActiveWifiIp(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val ni = interfaces.nextElement()
            if (!ni.isUp || ni.isLoopback || ni.isPointToPoint) continue
            val name = ni.name.lowercase()
            val displayName = ni.displayName.lowercase()

            if (name.contains("docker") || displayName.contains("docker") ||
                name.contains("wsl") || displayName.contains("wsl") ||
                name.contains("vbox") || displayName.contains("virtualbox") ||
                name.contains("vmnet") || displayName.contains("vmware") ||
                name.contains("veth") || displayName.contains("vethernet") ||
                name.contains("hyper-v") || displayName.contains("hyper-v") ||
                name.contains("pdanet") || displayName.contains("pdanet") ||
                name.contains("bluetooth") || displayName.contains("bluetooth")
            ) {
                continue
            }

            val addresses = ni.inetAddresses
            while (addresses.hasMoreElements()) {
                val addr = addresses.nextElement()
                if (addr is Inet4Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                    val ip = addr.hostAddress
                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip
                    }
                }
            }
        }
    } catch (_: Exception) {}
    return ""
}

val autoDetectedWifiIp = detectActiveWifiIp()
val devHostProp = localProperties.getProperty("dev.host")?.trim() ?: ""
val devServerHostProp = if (autoDetectedWifiIp.isNotBlank()) autoDetectedWifiIp else (localProperties.getProperty("DEV_SERVER_HOST")?.trim() ?: devHostProp)
val devModeProp = localProperties.getProperty("dev.mode")?.trim() ?: ""

if (autoDetectedWifiIp.isBlank()) {
    logger.warn("⚠️ WARNING: IP Wi-Fi aktif tidak terdeteksi pada adapter jaringan. BuildConfig.DEV_SERVER_HOST dibiarkan kosong.")
} else {
    logger.lifecycle("🌐 IP Wi-Fi Otomatis Terdeteksi: $autoDetectedWifiIp")
}

android {
    namespace = "com.example.kawansekolah"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.kawansekolah"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "2.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_EMULATOR", "true")
            buildConfigField("String", "DEV_HOST", "\"$devServerHostProp\"")
            buildConfigField("String", "DEV_SERVER_HOST", "\"$devServerHostProp\"")
            buildConfigField("String", "DEV_MODE", "\"$devModeProp\"")
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
        }

        create("physicalDebug") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            buildConfigField("boolean", "USE_EMULATOR", "false")
            buildConfigField("String", "DEV_HOST", "\"$devServerHostProp\"")
            buildConfigField("String", "DEV_MODE", "\"$devModeProp\"")
            buildConfigField("String", "BASE_URL", "\"https://GANTI-DENGAN-URL-BACKEND/\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "USE_EMULATOR", "false")
            buildConfigField("String", "DEV_HOST", "\"\"")
            buildConfigField("String", "DEV_MODE", "\"\"")
            buildConfigField("String", "BASE_URL", "\"https://GANTI-DENGAN-URL-BACKEND/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Gradle Task Hook untuk Otomasi Layanan Lokal
val isWindowsOS = org.gradle.internal.os.OperatingSystem.current().isWindows
val isAutoStartEnabled = localProperties.getProperty("scolarsia.autostart")?.trim()?.lowercase() == "true"
val projectRootDir = rootProject.projectDir.absolutePath
val scriptFile = File(projectRootDir, "siapkan-lokal.bat").absolutePath

val requestedTasksList = gradle.startParameter.taskNames.map { it.lowercase() }
val isExcludedTask = requestedTasksList.any { name ->
    name.contains("test") || name.contains("release") || name.contains("bundle") || name.contains("sync")
}
val shouldRunAutoStart = isWindowsOS && isAutoStartEnabled && !isExcludedTask

abstract class StartLocalServicesTask : DefaultTask() {
    @get:Input
    abstract val projectDir: Property<String>

    @get:Input
    abstract val autoStartEnabled: Property<Boolean>

    @TaskAction
    fun run() {
        if (!autoStartEnabled.get()) {
            logger.lifecycle("⏭️ startLocalServices dilewati (autostart disabled / test / release / sync / non-Windows).")
            return
        }
        val script = File(projectDir.get(), "siapkan-lokal.bat").absolutePath
        val process = ProcessBuilder("cmd.exe", "/c", script)
            .directory(File(projectDir.get()))
            .inheritIO()
            .start()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            logger.warn("⚠️ WARNING: Beberapa layanan lokal GAGAL disiapkan (Exit code: $exitCode). Build tetap dilanjutkan.")
        }
    }
}

val startLocalServices = tasks.register<StartLocalServicesTask>("startLocalServices") {
    group = "scolarsia"
    description = "Menyiapkan layanan lokal (Docker, Firebase Emulator, Seed Data, Backend API) sebelum installDebug."
    projectDir.set(rootProject.projectDir.absolutePath)
    autoStartEnabled.set(shouldRunAutoStart)
}

tasks.matching { it.name == "installDebug" || it.name == "installPhysicalDebug" }.configureEach {
    dependsOn(startLocalServices)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
