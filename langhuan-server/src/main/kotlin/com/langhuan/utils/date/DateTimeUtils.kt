package com.langhuan.utils.date

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 日期时间工具类
 * 提供统一的时区处理方法
 *
 * @author system
 */
object DateTimeUtils {

	private val log = LoggerFactory.getLogger(DateTimeUtils::class.java)

	/**
	 * 上海时区
	 */
	val SHANGHAI_ZONE: ZoneId = ZoneId.of("Asia/Shanghai")
	
	/**
	 * 默认日期时间格式
	 */
	val DEFAULT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

	/**
	 * 获取当前上海时区的日期时间
	 *
	 * @return 上海时区的当前时间
	 */
	fun now(): LocalDateTime {
		return LocalDateTime.now(SHANGHAI_ZONE)
	}

	/**
	 * 获取当前上海时区的带时区信息的时间
	 *
	 * @return 上海时区的当前时间
	 */
	fun nowWithZone(): ZonedDateTime {
		return ZonedDateTime.now(SHANGHAI_ZONE)
	}

	/**
	 * 将UTC时间转换为上海时区时间
	 *
	 * @param utcDateTime UTC时间
	 * @return 上海时区时间
	 */
	fun utcToShanghai(utcDateTime: LocalDateTime?): LocalDateTime? {
		if (utcDateTime == null) {
			return null
		}
		return utcDateTime.atZone(ZoneId.of("UTC"))
			.withZoneSameInstant(SHANGHAI_ZONE)
			.toLocalDateTime()
	}

	/**
	 * 将上海时区时间转换为UTC时间
	 *
	 * @param shanghaiDateTime 上海时区时间
	 * @return UTC时间
	 */
	fun shanghaiToUtc(shanghaiDateTime: LocalDateTime?): LocalDateTime? {
		if (shanghaiDateTime == null) {
			return null
		}
		return shanghaiDateTime.atZone(SHANGHAI_ZONE)
			.withZoneSameInstant(ZoneId.of("UTC"))
			.toLocalDateTime()
	}

	/**
	 * 格式化日期时间为字符串
	 *
	 * @param dateTime 日期时间
	 * @return 格式化后的字符串
	 */
	fun format(dateTime: LocalDateTime?): String? {
		if (dateTime == null) {
			return null
		}
		return dateTime.format(DEFAULT_FORMATTER)
	}

	/**
	 * 格式化日期时间为字符串（指定格式）
	 *
	 * @param dateTime 日期时间
	 * @param pattern  格式模式
	 * @return 格式化后的字符串
	 */
	fun format(dateTime: LocalDateTime?, pattern: String): String? {
		if (dateTime == null) {
			return null
		}
		return dateTime.format(DateTimeFormatter.ofPattern(pattern))
	}
}
