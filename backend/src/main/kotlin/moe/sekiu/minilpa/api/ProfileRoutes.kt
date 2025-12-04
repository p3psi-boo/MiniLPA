package moe.sekiu.minilpa.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import moe.sekiu.minilpa.lpa.LPAManager
import moe.sekiu.minilpa.model.ActivationCode
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ProfileRoutes")

@Serializable
data class DownloadRequest(
    val activationCode: String,
    val confirmationCode: String? = null,
    val imei: String? = null
)

@Serializable
data class NicknameRequest(
    val nickname: String
)

fun Route.configureProfileRoutes() {
    route("/profiles") {
        /**
         * GET /api/profiles
         * 获取所有配置文件列表
         */
        get {
            try {
                logger.info("获取配置文件列表")
                val backend = LPAManager.getBackend()
                val profiles = backend.listProfile()

                call.respond(mapOf("profiles" to profiles))
            } catch (e: Exception) {
                logger.error("获取配置文件列表失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "获取配置文件列表失败"))
                )
            }
        }

        /**
         * POST /api/profiles
         * 下载新的配置文件
         */
        post {
            try {
                val request = call.receive<DownloadRequest>()
                logger.info("下载配置文件: ${request.activationCode}")

                val backend = LPAManager.getBackend()
                val code = ActivationCode.parse(request.activationCode)
                    ?: throw IllegalArgumentException("无效的激活码格式")

                // TODO: 实现异步下载和进度推送
                val profile = backend.downloadProfile(code, request.confirmationCode, request.imei)

                call.respond(HttpStatusCode.Created, profile)
            } catch (e: IllegalArgumentException) {
                logger.error("激活码格式错误", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to e.message)
                )
            } catch (e: Exception) {
                logger.error("下载配置文件失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "下载配置文件失败"))
                )
            }
        }

        /**
         * PUT /api/profiles/{iccid}/enable
         * 启用配置文件
         */
        put("/{iccid}/enable") {
            try {
                val iccid = call.parameters["iccid"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "缺少 ICCID"))

                logger.info("启用配置文件: $iccid")
                val backend = LPAManager.getBackend()
                backend.enableProfile(iccid)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("启用配置文件失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "启用配置文件失败"))
                )
            }
        }

        /**
         * PUT /api/profiles/{iccid}/disable
         * 禁用配置文件
         */
        put("/{iccid}/disable") {
            try {
                val iccid = call.parameters["iccid"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "缺少 ICCID"))

                logger.info("禁用配置文件: $iccid")
                val backend = LPAManager.getBackend()
                backend.disableProfile(iccid)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("禁用配置文件失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "禁用配置文件失败"))
                )
            }
        }

        /**
         * PUT /api/profiles/{iccid}/nickname
         * 设置配置文件昵称
         */
        put("/{iccid}/nickname") {
            try {
                val iccid = call.parameters["iccid"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "缺少 ICCID"))

                val request = call.receive<NicknameRequest>()
                logger.info("设置配置文件昵称: $iccid -> ${request.nickname}")

                val backend = LPAManager.getBackend()
                backend.setNickname(iccid, request.nickname)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("设置昵称失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "设置昵称失败"))
                )
            }
        }

        /**
         * DELETE /api/profiles/{iccid}
         * 删除配置文件
         */
        delete("/{iccid}") {
            try {
                val iccid = call.parameters["iccid"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "缺少 ICCID"))

                logger.info("删除配置文件: $iccid")
                val backend = LPAManager.getBackend()
                backend.deleteProfile(iccid)

                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                logger.error("删除配置文件失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "删除配置文件失败"))
                )
            }
        }
    }
}
