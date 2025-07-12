package com.langhuan.utils.date;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供统一的时区处理方法
 *
 * @author system
 */
@Slf4j
public class DateTimeUtils {

    /**
     * 上海时区
     */
    public static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    
    /**
     * 默认日期时间格式
     */
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前上海时区的日期时间
     *
     * @return 上海时区的当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(SHANGHAI_ZONE);
    }

    /**
     * 获取当前上海时区的带时区信息的时间
     *
     * @return 上海时区的当前时间
     */
    public static ZonedDateTime nowWithZone() {
        return ZonedDateTime.now(SHANGHAI_ZONE);
    }

    /**
     * 将UTC时间转换为上海时区时间
     *
     * @param utcDateTime UTC时间
     * @return 上海时区时间
     */
    public static LocalDateTime utcToShanghai(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(SHANGHAI_ZONE)
                .toLocalDateTime();
    }

    /**
     * 将上海时区时间转换为UTC时间
     *
     * @param shanghaiDateTime 上海时区时间
     * @return UTC时间
     */
    public static LocalDateTime shanghaiToUtc(LocalDateTime shanghaiDateTime) {
        if (shanghaiDateTime == null) {
            return null;
        }
        return shanghaiDateTime.atZone(SHANGHAI_ZONE)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * 格式化日期时间为字符串
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化日期时间为字符串（指定格式）
     *
     * @param dateTime 日期时间
     * @param pattern  格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
} 