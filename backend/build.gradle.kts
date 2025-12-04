plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

group = "moe.sekiu.minilpa"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:3.0.0-beta-2")
    implementation("io.ktor:ktor-server-cio:3.0.0-beta-2")
    implementation("io.ktor:ktor-server-websockets:3.0.0-beta-2")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.0-beta-2")
    implementation("io.ktor:ktor-server-cors:3.0.0-beta-2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0-beta-2")

    // Kotlin 核心库
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")

    // 日志
    implementation("ch.qos.logback:logback-classic:1.5.7")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")

    // 工具库
    implementation("org.apache.commons:commons-lang3:3.17.0")

    // 图像处理（用于二维码解析）
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("org.boofcv:boofcv-core:1.1.5")

    // YAML 配置
    implementation("com.charleskorn.kaml:kaml:0.61.0")

    // 测试
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:3.0.0-beta-2")
}

application {
    mainClass.set("moe.sekiu.minilpa.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
