package moe.sekiu.minilpa.lpa

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import java.io.File
import kotlinx.serialization.json.jsonPrimitive
import moe.sekiu.minilpa.cast
import moe.sekiu.minilpa.decode
import moe.sekiu.minilpa.drop
import moe.sekiu.minilpa.exception.OperationFailureException
import moe.sekiu.minilpa.json
import moe.sekiu.minilpa.logger
import moe.sekiu.minilpa.lpacExecutablePath
import moe.sekiu.minilpa.model.ChipInfo
import moe.sekiu.minilpa.model.DownloadInfo
import moe.sekiu.minilpa.model.Driver
import moe.sekiu.minilpa.model.LPACIO
import moe.sekiu.minilpa.model.LocalDevice
import moe.sekiu.minilpa.model.Notification
import moe.sekiu.minilpa.model.Profile
import org.apache.commons.lang3.SystemUtils

/**
 * lpac 执行器 - 负责调用 lpac 命令行工具
 * 已移除 Swing UI 依赖，使用回调函数进行进度报告
 */
class LPACExecutor : LPABackend<LocalDevice> {
    private val log = logger()

    /**
     * 当前选中的设备（用于执行命令时设置环境变量）
     */
    var selectedDevice: Driver? = null

    /**
     * 获取设备列表
     */
    suspend fun listDevice(): List<Driver> = decode(execute("driver", "apdu", "list").data)

    override suspend fun getChipInfo(): ChipInfo = decode(execute("chip", "info").data)

    override suspend fun getProfileList(): List<Profile> = decode(execute("profile", "list").data)

    override suspend fun downloadProfile(downloadInfo: DownloadInfo) = execute(*downloadInfo.toCommand()).drop()

    /**
     * 下载配置文件（支持进度回调）
     */
    suspend fun downloadProfile(downloadInfo: DownloadInfo, onProgress: suspend (String, Int?) -> Unit) =
        execute(*downloadInfo.toCommand(), onProgress = onProgress).drop()

    override suspend fun enableProfile(iccid: String) = execute("profile", "enable", iccid).drop()

    /**
     * 启用配置文件（支持进度回调）
     */
    suspend fun enableProfile(iccid: String, onProgress: suspend (String, Int?) -> Unit) =
        execute("profile", "enable", iccid, onProgress = onProgress).drop()

    override suspend fun disableProfile(iccid: String) = execute("profile", "disable", iccid).drop()

    /**
     * 禁用配置文件（支持进度回调）
     */
    suspend fun disableProfile(iccid: String, onProgress: suspend (String, Int?) -> Unit) =
        execute("profile", "disable", iccid, onProgress = onProgress).drop()

    override suspend fun deleteProfile(iccid: String) = execute("profile", "delete", iccid).drop()

    /**
     * 删除配置文件（支持进度回调）
     */
    suspend fun deleteProfile(iccid: String, onProgress: suspend (String, Int?) -> Unit) =
        execute("profile", "delete", iccid, onProgress = onProgress).drop()

    override suspend fun setProfileNickname(iccid: String, nickname: String) = execute(
        "profile",
        "nickname",
        iccid,
        nickname
    ).drop()

    override suspend fun getNotificationList(): List<Notification> = decode(execute("notification", "list").data)

    override suspend fun processNotification(vararg seq: Int, remove: Boolean) {
        val commands = mutableListOf("notification", "process")
        if (remove) commands.add("-r")
        commands.addAll(seq.map { "$it" })
        execute(*commands.toTypedArray())
    }

    override suspend fun removeNotification(vararg seq: Int) = execute(
        "notification",
        "remove",
        *seq.map { "$it" }.toTypedArray()
    ).drop()

    override suspend fun setDefaultSMDPAddress(address: String) = execute("chip", "defaultsmdp", address).drop()

    override suspend fun getVersion(): String = decode(execute("version").data)

    /**
     * 执行 lpac 命令
     *
     * @param commands lpac 命令参数
     * @param onProgress 进度回调函数 (message: String, percent: Int?) -> Unit
     * @return lpac 输出数据
     */
    suspend fun execute(
        vararg commands: String,
        onProgress: (suspend (String, Int?) -> Unit)? = null
    ): LPACIO.Payload.DataPayload {
        log.info("lpac command input -> ${commands.joinToString(" ")}")

        // 使用全局的 lpac 路径
        val lpacPath = lpacExecutablePath
            ?: throw OperationFailureException("lpac not found. Please install lpac or set LPAC_PATH environment variable")

        log.info("Using lpac at: $lpacPath")

        var initialized = false
        var progressCount = 0
        val env = mutableMapOf<String, String>()

        // 设置调试环境变量（可选）
        // env["LIBEUICC_DEBUG_APDU"] = "true"
        // env["LIBEUICC_DEBUG_HTTP"] = "true"

        // 设置设备环境变量
        selectedDevice?.let { env["DRIVER_IFID"] = it.env }

        var lpacout: LPACIO? = null

        process(
            *(arrayOf(lpacPath) + commands),
            env = env,
            stdout = Redirect.Capture { line ->
                if (line.isBlank()) return@Capture
                log.info("lpac stdout output -> $line")

                try {
                    val lpacio = json.decodeFromString<LPACIO>(line)
                    when (lpacio.type) {
                        LPACIO.Type.PROGRESS -> {
                            val message = lpacio.payload.lpa.message
                            val displayMessage = if (message in listOf("es10b_retrieve_notifications_list", "es9p_handle_notification")) {
                                "#${lpacio.payload.lpa.data.jsonPrimitive.content} $message"
                            } else {
                                message
                            }

                            // 调用进度回调
                            onProgress?.let { callback ->
                                if (!initialized) {
                                    callback(displayMessage, 0)
                                    initialized = true
                                } else {
                                    progressCount++
                                    // 简单的进度估算：每个进度消息增加 10%，最多 90%
                                    val percent = minOf(progressCount * 10, 90)
                                    callback(displayMessage, percent)
                                }
                            }
                        }
                        LPACIO.Type.LPA -> {
                            if (!initialized && onProgress != null) {
                                initialized = true
                            }
                            // 完成
                            onProgress?.invoke("Operation completed", 100)
                            lpacout = lpacio
                        }
                        LPACIO.Type.DRIVER -> {
                            // 驱动操作完成
                            onProgress?.invoke("Driver operation completed", 100)
                            lpacout = lpacio
                        }
                        else -> throw UnsupportedOperationException("Unsupported LPACIO type: ${lpacio.type}")
                    }
                } catch (e: Exception) {
                    log.error("Failed to parse lpac output: $line", e)
                }
            },
            stderr = Redirect.Capture { line ->
                if (line.isBlank()) return@Capture
                log.info("lpac stderr output -> $line")
            }
        )

        val payload = lpacout?.payload ?: throw OperationFailureException("lpac output not captured")
        if (payload is LPACIO.Payload.LPA) payload.assertSuccess()
        return payload.cast()
    }
}
