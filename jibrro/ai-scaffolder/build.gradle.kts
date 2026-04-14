plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    application
}

application {
    mainClass.set("com.windrr.ai_scaffolder.MainKt")
}

repositories {
    google()
    mavenCentral()
}
dependencies {
    // Koog 프레임워크 (AI 에이전트)
    implementation(libs.koog.agents)
    implementation(libs.koog.tools)

    // Koog - Google Gemini LLM 클라이언트
    implementation(libs.koog.google)

    // Koog - A2A (Agent-to-Agent) 프로토콜
    implementation(libs.koog.a2a.server)
    implementation(libs.koog.a2a.client)
    implementation(libs.a2a.transport.server)
    implementation(libs.a2a.transport.client)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // 로깅 (SLF4J 구현체 — Koog 내부에서 사용)
    implementation("org.slf4j:slf4j-simple:2.0.16")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

