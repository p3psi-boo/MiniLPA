package moe.sekiu.minilpa.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.sekiu.minilpa.lpa.LPAManager
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ChipRoutes")

fun Route.configureChipRoutes() {
    route("/chip") {
        /**
         * GET /api/chip/info
         * 获取芯片信息（EID、固件版本、内存等）
         */
        get("/info") {
            try {
                logger.info("获取芯片信息")
                val backend = LPAManager.getBackend()
                val chipInfo = backend.chipInfo()

                call.respond(chipInfo)
            } catch (e: Exception) {
                logger.error("获取芯片信息失败", e)
                call.respond(
                    io.ktor.http.HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "获取芯片信息失败"))
                )
            }
        }
    }
}
