plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "moe.sekiu.minilpa"
version = "2.0.0"

repositories {
    // 优先使用国内镜像
    maven {
        setUrl("https://maven.aliyun.com/repository/public/")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/google/")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/gradle-plugin/")
    }
    mavenCentral()
    google()
}

dependencies {
    // Ktor Server (使用稳定版本)
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-cio:2.3.12")
    implementation("io.ktor:ktor-server-websockets:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    // Kotlin 核心库
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")

    // 日志
    implementation("ch.qos.logback:logback-classic:1.5.7")
    implementation("org.slf4j:slf4j-api:2.0.16")

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
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
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
