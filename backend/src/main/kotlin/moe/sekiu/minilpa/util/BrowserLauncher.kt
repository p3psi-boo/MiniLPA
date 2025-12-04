package moe.sekiu.minilpa.util

import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * 跨平台浏览器启动器
 */
object BrowserLauncher {
    private val logger = LoggerFactory.getLogger(BrowserLauncher::class.java)

    /**
     * 打开系统默认浏览器
     */
    fun openBrowser(url: String) {
        val os = System.getProperty("os.name").lowercase()
        val runtime = Runtime.getRuntime()

        try {
            when {
                os.contains("win") -> {
                    // Windows
                    runtime.exec("rundll32 url.dll,FileProtocolHandler $url")
                    logger.info("已在 Windows 上打开浏览器: $url")
                }
                os.contains("mac") -> {
                    // macOS
                    runtime.exec("open $url")
                    logger.info("已在 macOS 上打开浏览器: $url")
                }
                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    // Linux/Unix
                    runtime.exec("xdg-open $url")
                    logger.info("已在 Linux 上打开浏览器: $url")
                }
                else -> {
                    logger.warn("未知操作系统: $os，无法自动打开浏览器")
                }
            }
        } catch (e: IOException) {
            logger.error("打开浏览器失败: ${e.message}")
        }
    }
}
