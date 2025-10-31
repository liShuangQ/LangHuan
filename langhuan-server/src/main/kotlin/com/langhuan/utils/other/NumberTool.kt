package com.langhuan.utils.other

import org.slf4j.LoggerFactory

object NumberTool {
    private val log = LoggerFactory.getLogger(NumberTool::class.java)
    
    /**
     * 安全地将Object转换为Double类型
     * 处理Integer、Float、Double等数值类型的转换
     *
     * @param obj          待转换的对象
     * @param defaultValue 默认值
     * @return 转换后的Double值
     */
    fun convertToDouble(obj: Any?, defaultValue: Double): Double {
        if (obj == null) {
            return defaultValue
        }

        return when (obj) {
            is Double -> obj
            is Int -> obj.toDouble()
            is Float -> obj.toDouble()
            is Long -> obj.toDouble()
            is Number -> obj.toDouble()
            else -> {
                log.warn("无法转换类型 {} 为Double，使用默认值 {}", obj::class.java.simpleName, defaultValue)
                defaultValue
            }
        }
    }

}
