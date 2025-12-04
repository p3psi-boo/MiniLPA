package moe.sekiu.minilpa

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.apache.commons.lang3.ArchUtils
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.File

/**
 * 全局 JSON 序列化器
 */
val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = false
}

/**
 * 应用数据目录
 */
val appDataFolder = File(System.getProperty("user.home"), ".minilpa")

/**
 * lpac 可执行文件路径（支持环境变量配置）
 */
val lpacExecutablePath: String? by lazy {
    // 1. 尝试从环境变量获取
    System.getenv("LPAC_PATH")?.let { return@lazy it }

    // 2. 尝试从 PATH 查找
    findExecutableInPath("lpac")?.let { return@lazy it }

    // 3. 尝试从 appDataFolder 查找
    val lpacFile = File(File(appDataFolder, platform), if (SystemUtils.IS_OS_WINDOWS) "lpac.exe" else "lpac")
    if (lpacFile.exists() && lpacFile.isFile) {
        return@lazy lpacFile.canonicalPath
    }

    null
}

/**
 * 在系统 PATH 中查找可执行文件
 */
fun findExecutableInPath(name: String): String? {
    val pathEnv = System.getenv("PATH") ?: return null
    val pathSeparator = if (SystemUtils.IS_OS_WINDOWS) ";" else ":"
    val executable = if (SystemUtils.IS_OS_WINDOWS) "$name.exe" else name

    pathEnv.split(pathSeparator).forEach { dir ->
        val file = File(dir, executable)
        if (file.exists() && file.canExecute()) {
            return file.canonicalPath
        }
    }

    return null
}

/**
 * 平台信息（如 linux_x86、macos_universal 等）
 */
val platform = getPlatformInfo()

/**
 * 类型转换工具函数
 */
inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.castOrNull() = this as? T?

/**
 * JSON 解码工具函数
 */
inline fun <reified T> decode(element: JsonElement): T = json.decodeFromJsonElement(element)

/**
 * 丢弃返回值（用于不关心返回值的操作）
 */
inline fun Any?.drop() = Unit

/**
 * 日志工具函数
 */
inline fun Any.logger() = LoggerFactory.getLogger(this::class.simpleName)

/**
 * 获取平台信息
 */
fun getPlatformInfo(): String {
    val os = if (SystemUtils.IS_OS_MAC_OSX) "macos"
    else if (SystemUtils.IS_OS_WINDOWS) "windows"
    else if (SystemUtils.IS_OS_LINUX) "linux"
    else throw UnsupportedOperationException("Unsupported os ${SystemUtils.OS_NAME}")

    val processor = ArchUtils.getProcessor()
    val arch = if (os == "macos") "universal"
    else if (processor.isX86) "x86"
    else if (processor.isAarch64) "aarch64"
    else throw UnsupportedOperationException("Unsupported arch ${processor.type.label}")
    return "${os}_${arch}"
}
