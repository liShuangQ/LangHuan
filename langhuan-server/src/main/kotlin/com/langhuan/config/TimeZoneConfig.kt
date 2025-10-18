package com.langhuan.config

import com.langhuan.common.GlobalExceptionHandler
import com.langhuan.utils.date.DateTimeUtils
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * 时区配置类
 * 在应用程序启动时设置全局默认时区为上海时区
 *
 * @author system
 */
@Configuration
class TimeZoneConfig : CommandLineRunner {
    companion object {
        private val log = LoggerFactory.getLogger(TimeZoneConfig::class.java)
    }
    /**
     * 应用程序启动时执行，设置全局默认时区
     *
     * @param args 启动参数
     */
    override fun run(vararg args: String) {
        // 设置系统默认时区为上海时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
        
        // 设置ZoneId默认时区
        ZoneId.systemDefault()
        
        log.info("全局时区已设置为: {}", TimeZone.getDefault().id)
        log.info("当前系统时间: {}", DateTimeUtils.now())
        log.info("当前带时区时间: {}", ZonedDateTime.now(DateTimeUtils.SHANGHAI_ZONE))
        log.info("系统默认时区: {}", ZoneId.systemDefault())
    }
}
