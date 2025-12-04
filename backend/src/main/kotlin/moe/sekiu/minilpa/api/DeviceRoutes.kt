package moe.sekiu.minilpa.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.sekiu.minilpa.lpa.LPACExecutor
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("DeviceRoutes")

fun Route.configureDeviceRoutes() {
    route("/devices") {
        /**
         * GET /api/devices
         * 获取可用的设备列表
         */
        get {
            try {
                logger.info("获取设备列表")
                val backend = LPACExecutor()
                val devices = backend.listDevice()

                call.respond(mapOf("devices" to devices))
            } catch (e: Exception) {
                logger.error("获取设备列表失败", e)
                call.respond(
                    io.ktor.http.HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "获取设备列表失败"))
                )
            }
        }
    }
}
