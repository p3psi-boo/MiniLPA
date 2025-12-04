package moe.sekiu.minilpa.lpa

import moe.sekiu.minilpa.model.LocalDevice
import org.slf4j.LoggerFactory

/**
 * LPA 后端管理器（单例）
 * 管理 LPACExecutor 实例的生命周期
 */
object LPAManager {
    private val logger = LoggerFactory.getLogger(LPAManager::class.java)
    private var backend: LPABackend<LocalDevice>? = null

    /**
     * 获取或创建 LPA 后端实例
     */
    fun getBackend(): LPABackend<LocalDevice> {
        if (backend == null) {
            logger.info("初始化 LPACExecutor")
            backend = LPACExecutor()
        }
        return backend!!
    }

    /**
     * 设置自定义后端（用于测试）
     */
    fun setBackend(customBackend: LPABackend<LocalDevice>) {
        backend = customBackend
    }

    /**
     * 重置后端
     */
    fun reset() {
        backend = null
    }
}
