package moe.sekiu.minilpa.ws

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ProgressWebSocket")

/**
 * 进度消息
 */
@Serializable
data class ProgressMessage(
    val type: String,  // "progress", "complete", "error"
    val message: String,
    val percent: Int? = null
)

/**
 * 全局进度广播器
 */
object ProgressBroadcaster {
    private val sessions = mutableSetOf<WebSocketSession>()
    private val json = Json { prettyPrint = false }

    /**
     * 添加会话
     */
    fun addSession(session: WebSocketSession) {
        sessions.add(session)
        logger.info("新的 WebSocket 连接，当前连接数: ${sessions.size}")
    }

    /**
     * 移除会话
     */
    fun removeSession(session: WebSocketSession) {
        sessions.remove(session)
        logger.info("WebSocket 断开连接，当前连接数: ${sessions.size}")
    }

    /**
     * 广播进度消息到所有连接的客户端
     */
    suspend fun broadcast(message: ProgressMessage) {
        val text = json.encodeToString(message)
        sessions.toList().forEach { session ->
            try {
                session.send(Frame.Text(text))
            } catch (e: Exception) {
                logger.error("发送消息到客户端失败", e)
                sessions.remove(session)
            }
        }
    }

    /**
     * 发送进度消息
     */
    suspend fun sendProgress(message: String, percent: Int) {
        broadcast(ProgressMessage(type = "progress", message = message, percent = percent))
    }

    /**
     * 发送完成消息
     */
    suspend fun sendComplete(message: String) {
        broadcast(ProgressMessage(type = "complete", message = message))
    }

    /**
     * 发送错误消息
     */
    suspend fun sendError(message: String) {
        broadcast(ProgressMessage(type = "error", message = message))
    }
}

fun Route.configureProgressWebSocket() {
    webSocket("/ws/progress") {
        ProgressBroadcaster.addSession(this)

        try {
            // 发送欢迎消息
            send(Frame.Text(Json.encodeToString(
                ProgressMessage(type = "info", message = "WebSocket 连接已建立")
            )))

            // 保持连接，监听客户端消息（如果需要）
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    logger.debug("收到客户端消息: $text")
                }
            }
        } catch (e: Exception) {
            logger.error("WebSocket 错误", e)
        } finally {
            ProgressBroadcaster.removeSession(this)
        }
    }
}
