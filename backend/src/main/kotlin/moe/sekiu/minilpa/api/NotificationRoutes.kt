package moe.sekiu.minilpa.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.sekiu.minilpa.lpa.LPAManager
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("NotificationRoutes")

fun Route.configureNotificationRoutes() {
    route("/notifications") {
        /**
         * GET /api/notifications
         * 获取所有通知列表
         */
        get {
            try {
                logger.info("获取通知列表")
                val backend = LPAManager.getBackend()
                val notifications = backend.getNotificationList()

                call.respond(mapOf("notifications" to notifications))
            } catch (e: Exception) {
                logger.error("获取通知列表失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "获取通知列表失败"))
                )
            }
        }

        /**
         * POST /api/notifications/{seqNumber}/process
         * 处理通知
         */
        post("/{seqNumber}/process") {
            try {
                val seqNumber = call.parameters["seqNumber"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的序列号"))

                logger.info("处理通知: $seqNumber")
                val backend = LPAManager.getBackend()
                backend.processNotification(seqNumber)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("处理通知失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "处理通知失败"))
                )
            }
        }

        /**
         * DELETE /api/notifications/{seqNumber}
         * 删除通知
         */
        delete("/{seqNumber}") {
            try {
                val seqNumber = call.parameters["seqNumber"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的序列号"))

                logger.info("删除通知: $seqNumber")
                val backend = LPAManager.getBackend()
                backend.removeNotification(seqNumber)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("删除通知失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "删除通知失败"))
                )
            }
        }
    }
}
