import com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
}
fun apiKey(name: String): String = providers.environmentVariable(name).orNull?.trim()
    ?: localProperties.getProperty(name).orEmpty().trim()

// Release signing: environment variables (CI) or a gitignored keystore.properties (local).
val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
}
fun signingValue(env: String, property: String): String? =
    providers.environmentVariable(env).orNull?.takeIf(String::isNotBlank)
        ?: keystoreProperties.getProperty(property)?.takeIf(String::isNotBlank)

val releaseStoreFile = signingValue("JIBRO_KEYSTORE_PATH", "storeFile")
val releaseStorePassword = signingValue("JIBRO_KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("JIBRO_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("JIBRO_KEY_PASSWORD", "keyPassword")
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { it != null }

// CI passes the version from the git tag; local builds keep the defaults below.
val appVersionCode = providers.environmentVariable("JIBRO_VERSION_CODE").orNull?.toIntOrNull() ?: 6
val appVersionName = providers.environmentVariable("JIBRO_VERSION_NAME").orNull?.takeIf(String::isNotBlank) ?: "1.0.1"

val subwayApiKey = apiKey("SUBWAY_API_KEY")
val hasSubwayApiKey = subwayApiKey.isNotBlank()
val hasSharedFirebaseConfig = file("google-services.json").isFile
val hasReleaseFirebaseConfig = hasSharedFirebaseConfig || file("src/release/google-services.json").isFile
val hasDebugFirebaseConfig = hasSharedFirebaseConfig || file("src/debug/google-services.json").isFile

if (hasDebugFirebaseConfig || hasReleaseFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
    extensions.configure<GoogleServicesPluginConfig> {
        missingGoogleServicesStrategy = MissingGoogleServicesStrategy.IGNORE
    }
}

val validateReleaseConfiguration by tasks.registering {
    doLast {
        check(hasSubwayApiKey) {
            "Set SUBWAY_API_KEY in local.properties or the environment before building release."
        }
        check(hasReleaseFirebaseConfig) {
            "Add app/google-services.json or app/src/release/google-services.json before building release."
        }
        if (providers.environmentVariable("CI").orNull == "true") {
            check(hasReleaseSigning) {
                "Set JIBRO_KEYSTORE_PATH, JIBRO_KEYSTORE_PASSWORD, JIBRO_KEY_ALIAS and JIBRO_KEY_PASSWORD for CI releases."
            }
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(validateReleaseConfiguration)
}

configurations.all {
    resolutionStrategy {
        force(libs.kotlin.metadata.jvm)
    }
}

android {
    namespace = "com.windrr.jibrro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.windrr.jibrro"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        listOf("SUBWAY_API_KEY", "ODSAY_API_KEY", "SEOUL_BUS_API_KEY", "GYEONGGI_BUS_API_KEY").forEach { name ->
            val escapedKey = apiKey(name).replace("\\", "\\\\").replace("\"", "\\\"")
            buildConfigField("String", name, "\"$escapedKey\"")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.appwidget)
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.room:room-runtime:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.location)
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-android-compiler:2.57")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    testImplementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-agent-jvm:1.13.8")
    
    // AndroidX Test
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    
    // Robolectric
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}
