package moe.sekiu.minilpa

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import moe.sekiu.minilpa.api.configureChipRoutes
import moe.sekiu.minilpa.api.configureDeviceRoutes
import moe.sekiu.minilpa.api.configureNotificationRoutes
import moe.sekiu.minilpa.api.configureProfileRoutes
import moe.sekiu.minilpa.api.configureQRCodeRoutes
import moe.sekiu.minilpa.util.BrowserLauncher
import moe.sekiu.minilpa.ws.configureProgressWebSocket
import java.time.Duration

fun main() {
    // 启动服务器
    val server = embeddedServer(
        CIO,
        port = 8080,
        host = "127.0.0.1",
        module = Application::module
    )

    server.start(wait = false)

    println("✅ MiniLPA WebUI 服务器已启动")
    println("📡 监听地址: http://127.0.0.1:8080")
    println("🌐 正在打开浏览器...")

    // 延迟 1 秒后打开浏览器，确保服务器已完全启动
    Thread.sleep(1000)
    BrowserLauncher.openBrowser("http://localhost:8080")

    // 保持服务器运行
    Thread.currentThread().join()
}

fun Application.module() {
    // 配置 JSON 序列化
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 配置 CORS（仅允许本地访问）
    install(CORS) {
        allowHost("localhost:8080")
        allowHost("127.0.0.1:8080")
        allowHost("localhost:5173") // Vite 开发服务器
        allowHost("127.0.0.1:5173")
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
    }

    // 配置 WebSocket
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // 配置路由
    routing {
        // 健康检查
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "MiniLPA WebUI"))
        }

        // API 路由
        route("/api") {
            configureDeviceRoutes()
            configureChipRoutes()
            configureProfileRoutes()
            configureNotificationRoutes()
            configureQRCodeRoutes()
        }

        // WebSocket 路由
        configureProgressWebSocket()

        // 服务静态文件（前端）
        staticResources("/", "static") {
            default("index.html")
        }
    }
}
